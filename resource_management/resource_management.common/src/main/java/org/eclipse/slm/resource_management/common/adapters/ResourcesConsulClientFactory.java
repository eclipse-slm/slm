package org.eclipse.slm.resource_management.common.adapters;

import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.client.auth.ConsulJwtAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Factory to create ResourcesConsulClientFactory instances. ResourcesConsulClientFactory instances created by this factory can authenticate with the Consul server
 * using JWT access tokens or an admin client can be created.
 */
@Component
public class ResourcesConsulClientFactory {
    private final static Logger LOG = LoggerFactory.getLogger(ResourcesConsulClientFactory.class);

    private final ConsulClientFactory consulClientFactory;

    /**
     * Constructor for ResourcesConsulClientFactory.
     * @param consulClientFactory The ConsulClientFactory to create Consul clients for ResourcesConsulClientFactory instances. It is expected that the provided
     *                            ConsulClientFactory is configured to create clients that can authenticate with the Consul server using JWT access tokens.
     */
    public ResourcesConsulClientFactory(ConsulClientFactory consulClientFactory) {
        this.consulClientFactory = consulClientFactory;
    }

    /**
     * Creates a ResourcesConsulClient using the provided JWT access token for authentication.
     * @param jwtAccessToken The JWT access token to authenticate with the Consul server.
     * @return ResourcesConsulClient instance authenticated with the provided JWT access token
     */
    public ResourcesConsulClient create(String jwtAccessToken) {
        return new ResourcesConsulClient(this.consulClientFactory.create(
                new ConsulJwtAuthentication(this.consulClientFactory.getConsulUrl(), jwtAccessToken)
        ));
    }


    /**
     * Creates a ResourcesConsulClient with admin privileges using the configured ACL token from application properties.
     * @return ResourcesConsulClient instance with admin privileges
     */
    public ResourcesConsulClient createAdminClient() {
        return new ResourcesConsulClient(consulClientFactory.createAdminClient());
    }
}
