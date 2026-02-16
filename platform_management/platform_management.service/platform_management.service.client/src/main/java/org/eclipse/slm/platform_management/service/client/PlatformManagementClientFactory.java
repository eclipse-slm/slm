package org.eclipse.slm.platform_management.service.client;

import org.eclipse.slm.common.restclient.feign.auth.BearerTokenAuthRequestInterceptor;
import org.eclipse.slm.platform_management.service.api.PlatformManagementApiConfig;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

@Component
public class PlatformManagementClientFactory {

    private final LoadBalancerClient loadBalancerClient;
    private final ObjectFactory<HttpMessageConverters> messageConverters;

    private final String platformManagementConsulServiceName = "platform-management";

    public PlatformManagementClientFactory(LoadBalancerClient loadBalancerClient, ObjectFactory<HttpMessageConverters> messageConverters) {
        this.loadBalancerClient = loadBalancerClient;
        this.messageConverters = messageConverters;
    }

    /**
     * Create a PlatformManagementClient with an additional request interceptor (e.g. for authentication). The base URL is discovered via the
     * LoadBalancerClient which queries the service registry (e.g. Consul).
     *
     * @param bearerToken Bearer token for authentication
     * @return PlatformManagementClient instance
     */
    public PlatformManagementClient create(String bearerToken) {
        // Get Platform Management base URL from discovery
            var serviceInstance = loadBalancerClient.choose(platformManagementConsulServiceName);
            if (serviceInstance == null) {
                throw new RuntimeException("No service instance '" + platformManagementConsulServiceName + "' was found");
            }
            var platformManagementBaseUrl = serviceInstance.getUri().toString() + PlatformManagementApiConfig.BASE_PATH;

        var authRequestInterceptor = new BearerTokenAuthRequestInterceptor(bearerToken);

        var client = new PlatformManagementClient(platformManagementBaseUrl, messageConverters, authRequestInterceptor);
        return client;
    }

}
