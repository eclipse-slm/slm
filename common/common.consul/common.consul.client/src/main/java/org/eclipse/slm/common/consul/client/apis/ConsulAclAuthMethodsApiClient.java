package org.eclipse.slm.common.consul.client.apis;

import feign.RequestLine;
import org.eclipse.slm.common.consul.model.acl.authmethods.AuthMethodRequest;

/**
 * Feign-based Consul HTTP API client for ACL Auth Methods.
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/auth-methods">Consul API docs</a>.
 */
public interface ConsulAclAuthMethodsApiClient {

    /**
     * Update the authentication method configuration
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/auth-methods#create-an-auth-method">Consul API docs</a>.
     * @param request The auth method configuration to update
     */
    @RequestLine("PUT /acl/auth-method")
    void putAuthMethod(AuthMethodRequest request);

}
