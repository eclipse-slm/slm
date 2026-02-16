package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.auth.ConsulAuthentication;
import org.eclipse.slm.common.consul.client.auth.ConsulTokenAuthentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *  Factory to create ConsulClient instances. Consul admin client can be created with the configured admin token from application properties.
 */
@Component
public class ConsulClientFactory {

    private final String consulUrl;
    private final String consulToken;
    private final String consulDatacenter;

    /**
     * Creates a new ConsulClientFactory instance using configuration from application properties.
     *
     * @param consulUrl                The URL of the Consul server.
     * @param consulApplicationToken   The ACL token for admin access to the Consul server.
     * @param consulDatacenter         The datacenter of the Consul server.
     */
    public ConsulClientFactory(@Value("${consul.url}")          String consulUrl,
                               @Value("${consul.acl-token}")    String consulApplicationToken,
                               @Value("${consul.datacenter}")                  String consulDatacenter) {
        this.consulUrl = consulUrl;
        this.consulToken = consulApplicationToken;
        this.consulDatacenter = consulDatacenter;
    }

    /**
     * Gets the Consul server URL.
     *
     * @return The URL of the Consul server.
     */
    public String getConsulUrl() {
        return consulUrl;
    }

    /**
     * Creates a ConsulClient with the given ConsulAuthentication.
     * @param consulAuthentication The Consul authentication details.
     * @return ConsulClient instance
     */
    public ConsulClient create(ConsulAuthentication consulAuthentication) {
        var consulClient = new ConsulClient(this.consulUrl, consulAuthentication);
        return consulClient;
    }

    /**
     * Creates a ConsulClient with admin privileges using the configured ACL token from application properties.
     * @return ConsulClient instance with admin privileges
     */
    public ConsulClient createAdminClient() {
        var consulClient =  this.create(new ConsulTokenAuthentication(consulToken));
        return consulClient;
    }

}
