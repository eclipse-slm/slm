package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.auth.ConsulAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Consul Client to access all Consul API parts.
 */
public class ConsulClient {
    private final Logger LOG = LoggerFactory.getLogger(ConsulClient.class);

    private final ConsulAclClient consulAclClient;
    private final ConsulHealthClient consulHealthClient;
    private final ConsulNodesClient consulNodesClient;
    private final ConsulServicesClient consulServicesClient;
    private final ConsulKvClient consulKvClient;

    /**
     * Creates a new Consul Client instanceü.
     *
     * @param consulUrl The base URL of the Consul API (e.g. http://localhost:8500/v1/). If the URL does not end with /v1/, it will be appended automatically.
     * @param consulAuthentication The authentication method to use for the Consul server
     */
    public ConsulClient(String consulUrl, ConsulAuthentication consulAuthentication) {
        if (!consulUrl.endsWith("/v1/")) {
            consulUrl += "/v1/";
        }
        this.consulAclClient = new ConsulAclClient(consulUrl, consulAuthentication);
        this.consulNodesClient = new ConsulNodesClient(consulUrl, consulAuthentication);
        this.consulServicesClient = new ConsulServicesClient(consulUrl, consulAuthentication, this.consulNodesClient);
        this.consulHealthClient = new ConsulHealthClient(consulUrl, consulAuthentication, this.consulNodesClient);
        this.consulKvClient = new ConsulKvClient(consulUrl, consulAuthentication);
    }

    public ConsulAclClient acl() {
        return this.consulAclClient;
    }
    public ConsulHealthClient health() {
        return this.consulHealthClient;
    }
    public ConsulNodesClient nodes() {
        return this.consulNodesClient;
    }
    public ConsulServicesClient services() {
        return this.consulServicesClient;
    }
    public ConsulKvClient kv() {
        return this.consulKvClient;
    }
}
