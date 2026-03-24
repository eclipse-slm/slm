package org.eclipse.slm.common.vault.client;

import org.eclipse.slm.common.restclient.feign.FeignResponseException;
import org.eclipse.slm.common.vault.client.auth.VaultAuthentication;
import org.eclipse.slm.common.vault.client.exceptions.VaultGroupNotFoundException;
import org.eclipse.slm.common.vault.client.exceptions.VaultPolicyNotFoundException;
import org.eclipse.slm.common.vault.client.exceptions.VaultRuntimeException;
import org.eclipse.slm.common.vault.model.acl.Group;
import org.eclipse.slm.common.vault.model.acl.Policy;
import org.eclipse.slm.common.vault.model.acl.GroupType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VaultClientAcl extends AbstractVaultClient {
    private final Logger LOG = LoggerFactory.getLogger(VaultClientAcl.class);

    protected VaultClientAcl(String vaultUrl, VaultAuthentication vaultAuthentication) throws VaultRuntimeException {
        super(vaultUrl, vaultAuthentication);
    }

    //region Policies
    // https://developer.hashicorp.com/vault/api-docs/system/policy
    public void createOrUpdatePolicy(String policyName, String rules) {
        try {
            if (policyName.startsWith("/")) {
                policyName = policyName.replaceFirst("/", "");
            }

            Map<String, String> body = new HashMap<>();
            body.put("policy", rules);
            vaultApiClientSys.createPolicy(policyName, body);
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Could not add policy '" + policyName + "'", e);
        }
    }

    public Policy getPolicy(String policyName) {
        try {
            if (policyName.startsWith("/")) {
                policyName = policyName.replaceFirst("/", "");
            }

            var policyResponse = vaultApiClientSys.getPolicy(policyName);
            return policyResponse.getData();
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new VaultPolicyNotFoundException(policyName, e);
            }
            throw new VaultRuntimeException("Could not get policy '" + policyName + "'", e);
        }
    }

    public void deletePolicy(String policyName) {
        try {
            vaultApiClientSys.deletePolicy(policyName);

            // Remove policy from all groups (Vault does not do this automatically)
            var groupNames = this.getAllGroupNames();
            for (var groupName : groupNames) {
                this.removePolicyFromGroup(groupName, policyName);
            }
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new VaultPolicyNotFoundException(policyName, e);
            }
            throw new VaultRuntimeException("Could not remove policy '" + policyName + "'", e);
        }
    }

    public void addRuleToPolicy(String policyName, String rule) {
        // Setup regex matcher
        String re = "path\\s+\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(re);
        // Parse rule to add
        String ruleName = null;
        Matcher matcher = pattern.matcher(rule);
        if (matcher.find()) {
            ruleName = matcher.group(1);
        }
        // Get policy to which the rule should be added
        var policy = this.getPolicy(policyName);
        HashMap<String, String> parsedRules = new HashMap<String, String>();
        // Parse rules of policy with regex
        var ruleLines = Arrays.asList(policy.getRules().split("\n"));
        for (String ruleLine : ruleLines) {
            matcher = pattern.matcher(ruleLine);
            String ruleNameItem = null;
            if (matcher.find()) {
                ruleNameItem = matcher.group(1);
                parsedRules.put(ruleNameItem, ruleLine);
            }
        }
        // Append rule if not already in policy
        if (ruleName != null){
            if (parsedRules.containsKey(ruleName)) {
                LOG.warn("A rule for path '" + ruleName + "' is already in policy. Overwriting...");
            }
            parsedRules.put(ruleName, rule);
        }
        if (!parsedRules.isEmpty()){
            // Join rules and add/update policy
            String joinedRules = String.join("\n", parsedRules.values());
            createOrUpdatePolicy(policyName, joinedRules);
        } else {
            throw new VaultRuntimeException("No rules could be parsed in order to add to the policy. Will not add/update policy '"
                    + policyName + "'(input: rule '" + rule + "')");
        }
    }

    public void removeRuleFromPolicy(String policyName, String ruleName) {
        // Get policy from which the rule should be removed
        var policy = this.getPolicy(policyName);
        // Parse rules with regex
        var ruleLines = Arrays.asList(policy.getRules().split("\n"));
        HashMap<String, String> parsedRules = new HashMap<>();
        String re = "\"((.*\\/+\\*?))\"";
        Pattern pattern = Pattern.compile(re);
        for (String ruleLine : ruleLines) {
            Matcher matcher = pattern.matcher(ruleLine);
            String ruleLineName = null;
            if (matcher.find()) {
                ruleLineName = matcher.group(1);
                parsedRules.put(ruleLineName, ruleLine);
            }
        }
        // Remove rule if in policy
        if (!parsedRules.containsKey(ruleName)){
            LOG.warn("A rule for path '"+ ruleName + "' is not in policy.");
        } else {
            parsedRules.remove(ruleName);
        }
        // Join rules and update policy
        String joinedRules = String.join("\n", parsedRules.values());
        createOrUpdatePolicy(policyName, joinedRules);
    }
    //endregion Policies

    //region Groups
    // https://developer.hashicorp.com/vault/api-docs/secret/identity/group
    public List<String> getAllGroupNames() {
        try {
            var groupNamesResponse = this.vaultApiClientIdentity.getAllGroupNames();
            return groupNamesResponse.getData().get("keys");
        }
        catch (FeignResponseException e) {
                throw new VaultRuntimeException("Could not get all group names", e);
            }
    }

    public void createOrUpdateGroup(String groupName, GroupType groupType, List<String> policies) {
        var group = new Group();
        group.setName(groupName);
        group.setType(groupType.toString());
        group.setPolicies(policies);

        this.createOrUpdateGroup(group);
    }

    public void createOrUpdateGroup(Group group) {
        try {
            if (group.getName().startsWith("/")) {
                group.setName(group.getName().replaceFirst("/", ""));
            }

            this.vaultApiClientIdentity.createOrUpdateGroupByName(group.getName(), group);
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Could not create or update group " + group.getName(), e);
        }
    }

    public Group getGroupByName(String groupName) {
        try {
            if (groupName.startsWith("/")) {
                groupName = groupName.replaceFirst("/", "");
            }

            var groupResponse = this.vaultApiClientIdentity.getGroupByName(groupName);
            return groupResponse.getData();
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new VaultGroupNotFoundException(groupName, e);
            }
            throw new VaultRuntimeException("Could not get group by name " + groupName, e);
        }
    }

    public void deleteGroupByName(String groupName) {
        try {
            if (groupName.startsWith("/")) {
                groupName = groupName.replaceFirst("/", "");
            }
            vaultApiClientIdentity.deleteGroupByName(groupName);
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Could not delete group by name " + groupName, e);
        }
    }

    public void addPolicyToGroup(String groupName, String policyName) {
        try {
            var group = this.getGroupByName(groupName);
            if (!group.getPolicies().contains(policyName)) {
                group.getPolicies().add(policyName);
                this.createOrUpdateGroup(group);
            }
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Could not add policy '" + policyName + "' to group '" + groupName + "'", e);
        }
    }

    public void removePolicyFromGroup(String groupName, String policyName) {
        try {
            var group = this.getGroupByName(groupName);
            if (group.getPolicies().contains(policyName)) {
                group.getPolicies().remove(policyName);
                this.createOrUpdateGroup(group);
            }
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Could not remove policy '" + policyName + "' from group '" + groupName + "'", e);
        }
    }
    //endregion Groups
}
