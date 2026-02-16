package org.eclipse.slm.resource_management.common.adapters;

import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.client.auth.ConsulJwtAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Factory to create RemoteAccessConsulClient instances. RemoteAccessConsulClient instances created by this factory can authenticate with the Consul server
 * using JWT access tokens or an admin client can be created.
 */
@Component
public class RemoteAccessConsulClientFactory {
    private final static Logger LOG = LoggerFactory.getLogger(RemoteAccessConsulClientFactory.class);

    private final ConsulClientFactory consulClientFactory;

    /**
     * Constructor for RemoteAccessConsulClientFactory.
     * @param consulClientFactory The ConsulClientFactory to create Consul clients for RemoteAccessConsulClient instances. It is expected that the provided
     *                            ConsulClientFactory is configured to create clients that can authenticate with the Consul server using JWT access tokens.
     */
    public RemoteAccessConsulClientFactory(ConsulClientFactory consulClientFactory) {
        this.consulClientFactory = consulClientFactory;
    }

    /**
     * Creates a RemoteAccessConsulClient using the provided JWT access token for authentication.
     * @param jwtAccessToken The JWT access token to authenticate with the Consul server.
     * @return RemoteAccessConsulClient instance authenticated with the provided JWT access token
     */
    public RemoteAccessConsulClient create(String jwtAccessToken) {
        return new RemoteAccessConsulClient(this.consulClientFactory.create(
                new ConsulJwtAuthentication(this.consulClientFactory.getConsulUrl(), jwtAccessToken)
        ));
    }


    /**
     * Creates a RemoteAccessConsulClient with admin privileges using the configured ACL token from application properties.
     * @return RemoteAccessConsulClient instance with admin privileges
     */
    public RemoteAccessConsulClient createAdminClient() {
        return new RemoteAccessConsulClient(consulClientFactory.createAdminClient());
    }
}
