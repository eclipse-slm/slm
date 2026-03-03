package org.eclipse.slm.common.consul.client.apis;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.eclipse.slm.common.consul.model.acl.roles.Role;
import org.eclipse.slm.common.consul.model.acl.roles.RoleCreateRequest;
import org.eclipse.slm.common.consul.model.acl.roles.RoleUpdateRequest;

import java.util.List;

/**
 * Feign-based Consul HTTP API client for ACL Roles.
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/roles">Consul API docs</a>.
 */
public interface ConsulAclRolesApiClient {

    /**
     * Create a new ACL Role
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/roles#create-a-role">Consul API docs</a>.
     * @param roleCreateRequest The role to create
     * @param ns <ENTERPRISE FEATURE> The namespace in which the role should be created
     * @return The created role
     */
    @RequestLine("PUT /acl/role?ns={ns}")
    @Headers("Content-Type: application/json")
    Role createRole(
            @Param("ns") String ns,
            RoleCreateRequest roleCreateRequest
    );

    /**
     * Read an ACL Role by ID
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/roles#read-a-role">Consul API docs</a>.
     * @param id The ID of the role to read
     * @param ns <ENTERPRISE FEATURE> The namespace in which the role exists
     * @param partition <ENTERPRISE FEATURE> The partition in which the role exists
     * @return The requested role
     */
    @RequestLine("GET /acl/role/{id}?ns={ns}&partition={partition}")
    Role readRole(
            @Param("id") String id,
            @Param("ns") String ns,
            @Param("partition") String partition
    );

    /**
     * Read an ACL Role by Name
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/roles#read-a-role-by-name">Consul API docs</a>.
     * @param name The name of the role to read
     * @param ns <ENTERPRISE FEATURE> The namespace in which the role exists
     * @param partition <ENTERPRISE FEATURE> The partition in which the role exists
     * @return The requested role
     */
    @RequestLine("GET /acl/role/name/{name}?ns={ns}&partition={partition}")
    Role readRoleByName(
            @Param("name") String name,
            @Param("ns") String ns,
            @Param("partition") String partition
    );

    /**
     * Update an existing ACL Role
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/roles#update-a-role">Consul API docs</a>.
     * @param id The ID of the role to update
     * @param ns <ENTERPRISE FEATURE> The namespace in which the role exists
     * @param roleUpdateRequest The updated role
     * @return The updated role
     */
    @RequestLine("PUT /acl/role/{id}?ns={ns}")
    @Headers("Content-Type: application/json")
    Role updateRole(
            @Param("id") String id,
            @Param("ns") String ns,
            RoleUpdateRequest roleUpdateRequest
    );

    /**
     * Delete an ACL Role by ID
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/roles#delete-a-role">Consul API docs</a>.
     * @param id The ID of the role to delete
     * @param ns <ENTERPRISE FEATURE> The namespace in which the role exists
     * @param partition <ENTERPRISE FEATURE> The partition in which the role exists
     * @return True if the role was deleted, false otherwise
     */
    @RequestLine("DELETE /acl/role/{id}?ns={ns}&partition={partition}")
    Boolean deleteRole(
            @Param("id") String id,
            @Param("ns") String ns,
            @Param("partition") String partition
    );

    /**
     * List all ACL Roles
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/roles#list-roles">Consul API docs</a>.
     * @param policy The policy to filter the roles
     * @param ns <ENTERPRISE FEATURE> The namespace in which the roles exist
     * @param partition <ENTERPRISE FEATURE> The partition in which the roles exist
     * @return List of all ACL roles
     */
    @RequestLine("GET /acl/roles?policy={policy}&ns={ns}&partition={partition}")
    List<Role> listRoles(
            @Param("policy") String policy,
            @Param("ns") String ns,
            @Param("partition") String partition
    );
}
