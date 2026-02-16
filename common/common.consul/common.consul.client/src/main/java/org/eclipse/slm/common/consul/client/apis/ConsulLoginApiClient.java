package org.eclipse.slm.common.consul.client.apis;

import feign.Body;
import feign.RequestLine;
import org.eclipse.slm.common.consul.client.auth.ConsulLoginRequest;
import org.eclipse.slm.common.consul.client.auth.ConsulLoginResponse;

/**
 * Feign-based Consul HTTP API client for login operations.
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl#acl-http-api">Consul API docs</a>.
 */
public interface ConsulLoginApiClient {

    /**
     * Login to an ACL Auth Method
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl#login-to-auth-method">Consul API docs</a>.
     * @param request The login request
     * @return The login response containing the token
     */
    @RequestLine("POST /acl/login")
    @Body("{request}")
    ConsulLoginResponse login(ConsulLoginRequest request);

    /**
     * Logout from an ACL Auth Method. The token deleted is specified with the X-Consul-Token header (automatically injected via RequestInterceptor).
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl#logout-from-auth-method">Consul API docs</a>.
     */
    @RequestLine("POST /acl/logout")
    void logout();

}
