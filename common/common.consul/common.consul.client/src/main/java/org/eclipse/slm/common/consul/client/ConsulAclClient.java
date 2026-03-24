package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.auth.ConsulAuthentication;
import org.eclipse.slm.common.consul.client.utils.ConsulMapper;
import org.eclipse.slm.common.consul.model.acl.authmethods.AuthMethodRequest;
import org.eclipse.slm.common.consul.model.acl.bindingrules.BindingRule;
import org.eclipse.slm.common.consul.model.acl.policies.Policy;
import org.eclipse.slm.common.consul.model.acl.roles.PolicyLink;
import org.eclipse.slm.common.consul.model.acl.roles.Role;
import org.eclipse.slm.common.consul.model.acl.roles.RoleCreateRequest;
import org.eclipse.slm.common.consul.model.exceptions.ConsulRoleNotFoundException;
import org.eclipse.slm.common.restclient.feign.FeignResponseException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulPolicyNotFoundException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Consul ACL Client which provides functionality to manage Consul ACLs, including policies, roles, auth methods, and binding rules.
 */
public class ConsulAclClient extends AbstractConsulClient {
    private final static Logger LOG = LoggerFactory.getLogger(ConsulAclClient.class);

    /**
     * Creates a new ConsulAclClient instance.
     * @param consulUrl             The URL of the Consul server.
     * @param consulAuthentication  The Consul authentication details.
     */
    public ConsulAclClient(String consulUrl, ConsulAuthentication consulAuthentication) {
        super(consulUrl, consulAuthentication);
    }

    //region Auth Methods
    public void createAuthMethod(AuthMethodRequest request) {
        try {
            this.consulAclAuthMethodsApiClient.putAuthMethod(request);
        } catch (FeignResponseException e) {
            throw new ConsulRuntimeException("Error creating auth method with name '" + request.getName() + "'", e);
        }
    }
    //endregion Auth Methods

    //region Policies
    public List<Policy> getPolicies() {
        var policies = this.consulAclPoliciesRulesApiClient.listPolicies(null, null);
        return policies;
    }

    public Policy getPolicyByNameOrThrow(String policyName) throws ConsulPolicyNotFoundException {
        try {
            var policy = this.consulAclPoliciesRulesApiClient.readPolicyByName(policyName, null, null);

            return policy;
        } catch(FeignResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new ConsulPolicyNotFoundException(policyName);
            }
            else {
                throw new ConsulRuntimeException("Error getting policy with name '" + policyName + "'", e);
            }
        }
    }

    public Policy createPolicy(Policy policy) {
        var policyCreateRequest = ConsulMapper.INSTANCE.toCreateRequest(policy);
        try {
            var createdPolicy = this.consulAclPoliciesRulesApiClient.createPolicy(null, policyCreateRequest);
            return createdPolicy;
        } catch (FeignResponseException e){
            if (e.getStatusCode() == 500 && e.getMessage().contains("A Policy with Name") && e.getMessage().contains("already exists")) {
                LOG.debug("Policy with name '{}' already exists, nothing to do. Trying to get existing policy.", policy.getName());
                var existingPolicy = this.consulAclPoliciesRulesApiClient.readPolicyByName(policy.getName(), null, null);
                return existingPolicy;
            }
            else {
                throw new ConsulRuntimeException("Error creating policy " + policy.getName(), e);
            }
        }
    }


    public Policy updatePolicy(Policy policy) {
        var policyUpdateRequest = ConsulMapper.INSTANCE.tuUpdateRequest(policy);
        try {
            var updatedPolicy = this.consulAclPoliciesRulesApiClient.updatePolicy(policy.getId(), null, policyUpdateRequest);
            return updatedPolicy;
        } catch (FeignResponseException e) {
            throw new ConsulRuntimeException("Error updating policy " + policy.getName(), e);
        }
    }

    public void deletePolicyById(String policyId) {
        try {
            this.consulAclPoliciesRulesApiClient.deletePolicy(policyId, null, null);
        } catch (FeignResponseException e) {
            throw new ConsulRuntimeException("Error deleting policy with id '" + policyId + "'", e);
        }
    }

    public void addReadRuleToPolicy(String policyName, String ruleType, String name) throws ConsulPolicyNotFoundException {
        var existingPolicy = this.getPolicyByNameOrThrow(policyName);

        var readRule = ruleType + " \"" + name + "\" { policy = \"read\" }";
        var rules = existingPolicy.getRules();
        rules = rules + "\n\n" + readRule;
        existingPolicy.setRules(rules);

        this.updatePolicy(existingPolicy);
    }

    public void removeReadRuleFromPolicy(String policyName, String ruleType, String name) {
        var existingPolicy = this.getPolicyByNameOrThrow(policyName);

        var readRule = ruleType + " \"" + name + "\" { policy = \"read\" }";
        var rules = existingPolicy.getRules();
        rules = rules.replace("\n\n" + readRule, "");
        existingPolicy.setRules(rules);

        this.updatePolicy(existingPolicy);
    }
    //endregion Policies

    //region Roles
    public List<Role> getRoles() {
        try {
            var roles = this.consulAclRolesApiClient.listRoles(null, null, null);
            return roles;
        }
        catch (FeignResponseException e){
            throw new ConsulRuntimeException("Error getting roles", e);
        }
    }

    public List<Role> getRolesLinkedToPolicy(String policyId) {
        try {
            var roles = this.consulAclRolesApiClient.listRoles(policyId, null, null);
            return roles;
        }
        catch (FeignResponseException e){
            throw new ConsulRuntimeException("Error getting roles linked to policy with id '" + policyId + "'", e);
        }
    }

    public void createRole(String roleName, String roleDescription, List<PolicyLink> policyLinks) {
        var cleanedRoleName = this.cleanRoleName(roleName);

        var roleCreateRequest = new RoleCreateRequest.Builder()
                .name(cleanedRoleName)
                .description(roleDescription)
                .policies(policyLinks)
                .build();
        try {
            this.consulAclRolesApiClient.createRole(null, roleCreateRequest);
        } catch(FeignResponseException e){
            throw new ConsulRuntimeException("Error creating role " + cleanedRoleName, e);
        }
    }

    public Role getRoleById(String roleId) {
        try{
            var role = this.consulAclRolesApiClient.readRole(roleId, null, null);
            return role;
        }catch (FeignResponseException e){
            throw new ConsulRuntimeException("Error getting role with id '" + roleId + "': " + e.getMessage(), e);
        }
    }

    public Role getRoleByName(String roleName) throws ConsulRoleNotFoundException {
        var cleanedRoleName = cleanRoleName(roleName);
        try{
            var role = this.consulAclRolesApiClient.readRoleByName(cleanedRoleName, null, null);
            return role;
        }catch (FeignResponseException e){
            if (e.getStatusCode() == 404) {
                throw new ConsulRoleNotFoundException(cleanedRoleName);
            }
            throw new ConsulRuntimeException("Error getting role with name '" + cleanedRoleName + "': " + e.getMessage(), e);
        }
    }

    public void deleteRoleById(String roleId) {
        try {
            this.consulAclRolesApiClient.deleteRole(roleId, null, null);
        } catch (FeignResponseException e){
            throw new ConsulRuntimeException("Error deleting role with id '" + roleId + "': " + e.getMessage(), e);
        }
    }

    public void addPolicyToRole(String roleName, String policyId) {
        var cleanedRoleName = cleanRoleName(roleName);
        try {
            var existingRole = this.getRoleByName(cleanedRoleName);
            var rolePolicyLink = new PolicyLink.Builder()
                    .id(policyId)
                    .build();
            var newPolicyLinks = new ArrayList<>(existingRole.getPolicies());
            newPolicyLinks.add(rolePolicyLink);
            existingRole.setPolicies(newPolicyLinks);
            var roleUpdateRequest = ConsulMapper.INSTANCE.toUpdateRequest(existingRole);

            this.consulAclRolesApiClient.updateRole(existingRole.getId(), null, roleUpdateRequest);
        } catch (FeignResponseException e){
            throw new ConsulRuntimeException("Error adding policy with id '" + policyId + "' to role '" + cleanedRoleName + "': " + e.getMessage(), e);
        }
    }

    private String cleanRoleName(String roleName) {
        if (roleName.startsWith("/")) {
            roleName = roleName.replaceFirst("/", "");
        }
        roleName = roleName.replace("/", "_");
        return roleName;
    }
    //endregion roles

    //region Binding Rules
    public String cleanUserGroupBindingRuleName(String userGroupId) {
        var bindingRuleName = userGroupId;
        if (bindingRuleName.startsWith("/")) {
            bindingRuleName = bindingRuleName.replaceFirst("/", "");
        }
        bindingRuleName = bindingRuleName.replace("/", "_");
        return bindingRuleName;
    }

    public List<BindingRule> getBindingRules() {
        try {
            var bindingRules = this.consulAclBindingRulesApiClient.listBindingRules("keycloak", null, null);
            return bindingRules;
        } catch (FeignResponseException e){
            throw new ConsulRuntimeException("Error getting binding rules: " + e.getMessage(), e);
        }
    }

    public void createBindingRule(BindingRule bindingRule) {
        var bindingRuleName = this.cleanUserGroupBindingRuleName(bindingRule.getBindName());
        bindingRule.setBindName(bindingRuleName);

        try {
            this.consulAclBindingRulesApiClient.createBindingRule(bindingRule);

        } catch (FeignResponseException e){
            throw new ConsulRuntimeException("Error creating binding rule: " + e.getMessage(), e);
        }
    }

    public void deleteBindingRuleById(String bindingRuleId) {
        try {
            var result = this.consulAclBindingRulesApiClient.deleteBindingRule(bindingRuleId, null, null);
        } catch (FeignResponseException e) {
            throw new ConsulRuntimeException("Error deleting binding rule with id '" + bindingRuleId + "': " + e.getMessage(), e);
        }
    }
    //endregion Binding Rules
}
