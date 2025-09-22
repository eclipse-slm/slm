package org.eclipse.slm.common.consul.client.apis;

import com.orbitz.consul.ConsulException;
import com.orbitz.consul.model.acl.ImmutableRole;
import com.orbitz.consul.model.acl.ImmutableRolePolicyLink;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.utils.ConsulObjectMapper;
import org.eclipse.slm.common.consul.model.acl.BindingRule;
import org.eclipse.slm.common.consul.model.acl.Policy;
import org.eclipse.slm.common.consul.model.acl.Role;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class ConsulAclApiClient extends AbstractConsulApiClient {

    public final static Logger LOG = LoggerFactory.getLogger(ConsulAclApiClient.class);


    public ConsulAclApiClient(
                              @Value("${consul.scheme}")       String consulScheme,
                              @Value("${consul.host}")         String consulHost,
                              @Value("${consul.port}")         int consulPort,
                              @Value("${consul.acl-token}")    String consulToken,
                              @Value("${consul.datacenter}")   String consulDatacenter) {
        super(consulScheme, consulHost, consulPort, consulToken, consulDatacenter);
    }

    public void addReadAccessViaKeycloakRole(UUID id, String name, String type)
    {
        var rules = "";
        var policyName = "";
        var roleName = "";
        if (type == "node")
        {
            rules = "node \"" + name + "\" { policy = \"read\" }";
            policyName = "resource_" + id;
            roleName = "resource_" + id;
        }
        else if (type == "service")
        {
            rules = "service \"" + name + "\" { policy = \"read\" }";
            policyName = "service_" + id;
            roleName = "service_" + id;
        }
        else
        {
            throw new IllegalArgumentException("Unknown type '"  + type +"' to create read access via Keycloak role in Consul");
        }

        Policy readPolicy = new Policy(
                policyName,
                "Read access policy for " + type + " '" + name + "' with id '" + id + "'",
                rules,
                Arrays.asList(this.consulDatacenter)
        );

        createPolicy(
                new ConsulCredential(),
                readPolicy
        );

        createRole(
                new ConsulCredential(),
                roleName,
                "Read access role for " + type + " '" + name + "' with id '" + id + "'",
                readPolicy.getName()
        );

        createBindingRule(
                new ConsulCredential(),
                new BindingRule(
                        "Read access binding of role '" + roleName + "' for " + type + " '" + name + "' with id '" + id + "' to auth method 'keycloak'",
                        "keycloak",
                        "\"" + roleName +  "\" in list.resources",
                        "role",
                        roleName
                )
        );
    }

    //region Policies
    public List<Policy> getPolicies(ConsulCredential consulCredential)
            throws ConsulLoginFailedException {
        // Credentials use
        var policies = this.getConsulClient(consulCredential).aclClient().listPolicies();

        return ConsulObjectMapper.mapAll(policies, Policy.class);
    }

    public Policy getPolicyByName(ConsulCredential consulCredential, String policyName)
            throws ConsulLoginFailedException {
        Policy response = null;
        try {
            var policy = this.getConsulClient(consulCredential).aclClient().readPolicyByName(policyName);
            response = ConsulObjectMapper.map(policy, Policy.class);
        } catch(ConsulException e) {
            LOG.error("Unable to find policy with name = '" + policyName + "' (Error Msg: " + e.getMessage() + ")");
            return null;
        }
        return response;
    }

    public void createPolicy(ConsulCredential consulCredential, Policy policy) {
        var consulPolicy = ConsulObjectMapper.map(policy, com.orbitz.consul.model.acl.Policy.class);
        try {
            this.getConsulClient(consulCredential).aclClient().createPolicy( consulPolicy);
        } catch (ConsulException e){
            LOG.debug("A policy with name '" + policy.getName() + "' already exists");
        } catch (ConsulLoginFailedException e){
            LOG.warn("ACL disabled => Not able to create policy with name '"+policy.getName()+"'");
        } catch(Exception e){
            LOG.warn(e.getMessage());
            throw  e;
        }
    }


    public void updatePolicy(ConsulCredential consulCredential, Policy policy) throws ConsulLoginFailedException {
        var consulPolicy = ConsulObjectMapper.map(policy, com.orbitz.consul.model.acl.Policy.class);
        var result = this.getConsulClient(consulCredential).aclClient().updatePolicy(policy.getId(), consulPolicy);
    }

    public void deletePolicyById(ConsulCredential consulCredential, String policyId)
            throws ConsulLoginFailedException {
        this.getConsulClient(consulCredential).aclClient().deletePolicy(policyId);
    }

    public void addReadRuleToPolicy(ConsulCredential consulCredential, String policyName, String ruleType, String name)
            throws ConsulLoginFailedException {
        var existingPolicy = this.getPolicyByName(consulCredential, policyName);
        if (existingPolicy != null)
        {
            var readRule = ruleType + " \"" + name + "\" { policy = \"read\" }";


            var rules = existingPolicy.getRules();
            rules = rules + "\n\n" + readRule;
            existingPolicy.setRules(rules);

            this.updatePolicy(consulCredential, existingPolicy);
        }
        else
        {
            LOG.error("Unable to get policy with name '" + policyName + "'");
        }
    }

//    public void removeReadRuleFromPolicy(ConsulCredential consulCredential, UUID resourceID, NodeService nodeService)
//            throws ConsulLoginFailedException {
//        removeReadRuleFromPolicy(
//                consulCredential,
//                "resource_" + resourceID,
//                "service",
//                nodeService.getService()
//        );
//    }

    public void removeReadRuleFromPolicy(ConsulCredential consulCredential, String policyName, String ruleType, String name)
            throws ConsulLoginFailedException {
        var existingPolicy = this.getPolicyByName(consulCredential, policyName);

        if (existingPolicy != null) {
            var readRule = ruleType + " \"" + name + "\" { policy = \"read\" }";

            var rules = existingPolicy.getRules();
            rules = rules.replace("\n\n" + readRule, "");
            existingPolicy.setRules(rules);

            this.updatePolicy(consulCredential, existingPolicy);
        }
        else {
            LOG.error("Unable to get policy with name '" + policyName + "'");
        }
    }
    //endregion Policies

    //region Roles
    public List<Role> getRoles(ConsulCredential consulCredential) throws ConsulLoginFailedException {
        var roles = this.getConsulClient(consulCredential).aclClient().listRoles();
        return ConsulObjectMapper.mapAll(roles, Role.class);
    }

    public void createRole(ConsulCredential consulCredential, String roleName, String roleDescription, String policyName) {

        var role = ImmutableRole.builder().setName(roleName).setDescription(roleDescription)
                .setPolicies(List.of(ImmutableRolePolicyLink.builder().setName(policyName).build()))
                .build();

        try {
            this.getConsulClient(consulCredential).aclClient().createRole(role);
        } catch(ConsulException e){
            LOG.debug("A Role with Name '" + role.getName() + "' already exists: ", e);
        } catch (ConsulLoginFailedException e) {
            LOG.warn("ACL disabled => Not able to create role with name '"+role.getName()+"'");
        }
    }

    public Role getRoleByName(ConsulCredential consulCredential, String roleName)
            throws ConsulLoginFailedException {
        try{
            var role = this.getConsulClient(consulCredential).aclClient().readRoleByName(roleName);
            return ConsulObjectMapper.map(role, Role.class);
        }catch (ConsulException exception){
            LOG.info("A Role with Name '" + roleName + "' not found");
            return null;
        }
    }

    public void deleteRoleById(ConsulCredential consulCredential, String roleId)
            throws ConsulLoginFailedException {
        this.getConsulClient(consulCredential).aclClient().deleteRole(roleId);
    }
    //endregion roles

    //region Binding Rules
    public List<BindingRule> getBindingRules(ConsulCredential consulCredential)
            throws ConsulLoginFailedException {

        var responseBindingRules = this.getConsulClient(consulCredential).aclClient().listBindingRules();
        return ConsulObjectMapper.mapAll(responseBindingRules, BindingRule.class);
    }

    public void createBindingRule(
            ConsulCredential consulCredential,
            BindingRule bindingRule
    ) {
        try {
            var rule = ConsulObjectMapper.map(bindingRule, com.orbitz.consul.model.acl.BindingRule.class);
            this.getConsulClient(consulCredential).aclClient().createBindingRule(rule);

        }catch (ConsulException e){
            LOG.error("Not able to create binding rule '" + bindingRule.getBindName() + "'" + e.getMessage());
        } catch (ConsulLoginFailedException e) {
            LOG.error("Consul login failed. Not able to create binding rule '" + bindingRule.getBindName() + "'");
        }
    }

    public void deleteBindingRuleById(ConsulCredential consulCredential, String bindingRuleId)
            throws ConsulLoginFailedException {
        this.getConsulClient(consulCredential).aclClient().deleteBindingRule(bindingRuleId);
    }
    //endregion Binding Rules
}
