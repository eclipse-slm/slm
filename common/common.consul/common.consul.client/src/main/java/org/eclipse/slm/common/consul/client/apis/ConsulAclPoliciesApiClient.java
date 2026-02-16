package org.eclipse.slm.common.consul.client.apis;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.eclipse.slm.common.consul.model.acl.Policy;
import org.eclipse.slm.common.consul.model.acl.PolicyCreateRequest;
import org.eclipse.slm.common.consul.model.acl.PolicyUpdateRequest;

import java.util.List;

/**
 * Feign-based Consul HTTP API client for ACL Policies.
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/policies">Consul API docs</a>.
 */
public interface ConsulAclPoliciesApiClient {

    /**
     * Create a new ACL Policy
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/policies#create-a-policy">Consul API docs</a>.
     * @param ns <ENTERPRISE FEATURE> The namespace in which the policy should be created
     * @param policyCreateRequest The policy to create
     * @return The created policy
     */
    @RequestLine("PUT /acl/policy?ns={ns}")
    Policy createPolicy(
            @Param("ns") String ns,
            PolicyCreateRequest policyCreateRequest);

    /**
     * Read an ACL Policy by ID
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/policies#read-a-policy">Consul API docs</a>.
     * @param id The ID of the policy to read
     * @param ns <ENTERPRISE FEATURE> The namespace in which the policy exists
     * @return The requested policy
     */
    @RequestLine("GET /acl/policy/{id}?ns={ns}")
    Policy readPolicy(
            @Param("id") String id,
            @Param("ns") String ns
    );

    /**
     * Read an ACL Policy by Name
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/policies#read-a-policy-by-name">Consul API docs</a>.
     * @param name The name of the policy to read
     * @param ns <ENTERPRISE FEATURE> The namespace in which the policy exists
     * @param partition <ENTERPRISE FEATURE> The partition in which the policy exists
     * @return The requested policy
     */
    @RequestLine("GET /acl/policy/name/{name}?ns={ns}&partition={partition}")
    Policy readPolicyByName(
            @Param("name") String name,
            @Param("ns") String ns,
            @Param("partition") String partition
    );

    /**
     * Update an existing ACL Policy
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/policies#update-a-policy">Consul API docs</a>.
     * @param id The ID of the policy to update
     * @param ns <ENTERPRISE FEATURE> The namespace in which the policy exists
     * @param policyUpdateRequest The updated policy
     * @return The updated policy
     */
    @RequestLine("PUT /acl/policy/{id}?ns={ns}")
    @Headers("Content-Type: application/json")
    Policy updatePolicy(
            @Param("id") String id,
            @Param("ns") String ns,
            PolicyUpdateRequest policyUpdateRequest
    );

    /**
     * Delete an ACL Policy by ID
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/policies#delete-a-policy">Consul API docs</a>.
     * @param id The ID of the policy to delete
     * @param ns <ENTERPRISE FEATURE> The namespace in which the policy exists
     * @param partition <ENTERPRISE FEATURE> The partition in which the policy exists
     * @return True if the policy was deleted, false otherwise
     */
    @RequestLine("DELETE /acl/policy/{id}?ns={ns}&partition={partition}")
    Boolean deletePolicy(
            @Param("id") String id,
            @Param("ns") String ns,
            @Param("partition") String partition
    );

    /**
     * List all ACL Policies
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/policies#list-policies">Consul API docs</a>.
     * @param ns <ENTERPRISE FEATURE> The namespace in which the policies exist
     * @param partition <ENTERPRISE FEATURE> The partition in which the policies exist
     * @return List of all ACL policies
     */
    @RequestLine("GET /acl/policies?ns={ns}&partition={partition}")
    List<Policy> listPolicies(
            @Param("ns") String ns,
            @Param("partition") String partition
    );

}
