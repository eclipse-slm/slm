package org.eclipse.slm.common.consul.client.apis;

import feign.Body;
import feign.Param;
import feign.RequestLine;
import org.eclipse.slm.common.consul.model.acl.bindingrules.BindingRule;

import java.util.List;

/**
 * Feign-based Consul HTTP API client for ACL Binding Rules.
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/binding-rules">Consul API docs</a>.
 */
public interface ConsulAclBindingRulesApiClient {

    /**
     * List all ACL Binding Rules
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/binding-rules#list-binding-rules">Consul API docs</a>.
     * @param authMethod The auth method to filter the binding rules
     * @param ns <ENTERPRISE FEATURE> The namespace in which the binding rules exist
     * @param partition <ENTERPRISE FEATURE> The partition in which the binding rules exist
     * @return List of all ACL binding rules
     */
    @RequestLine("GET /acl/binding-rules?authmethod={authmethod}&ns={ns}&partition={partition}")
    List<BindingRule> listBindingRules(
            @Param("authmethod") String authMethod,
            @Param("ns") String ns,
            @Param("partition") String partition
    );

    /**
     * Create a new ACL Binding Rule
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/binding-rules#create-a-binding-rule">Consul API docs</a>.
     * @param bindingRule The binding rule to create
     */
    @RequestLine("PUT /acl/binding-rule?ns={ns}&partition={partition}")
    @Body("{bindingRule}")
    void createBindingRule(BindingRule bindingRule);

    /**
     * Delete an ACL Binding Rule by ID
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/binding-rules#delete-a-binding-rule">Consul API docs</a>.
     * @param id The ID of the binding rule to delete
     * @param ns <ENTERPRISE FEATURE> The namespace in which the binding rule exists
     * @param partition <ENTERPRISE FEATURE> The partition in which the binding rule exists
     * @return True if the binding rule was deleted, false otherwise
     */
    @RequestLine("DELETE /acl/binding-rule/{id}?ns={ns}&partition={partition}")
    Boolean deleteBindingRule(
            @Param("id") String id,
            @Param("ns") String ns,
            @Param("partition") String partition
    );
}
