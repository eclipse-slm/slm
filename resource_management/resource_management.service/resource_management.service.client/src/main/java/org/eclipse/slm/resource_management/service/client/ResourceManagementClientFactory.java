package org.eclipse.slm.resource_management.service.client;

import org.eclipse.slm.common.restclient.feign.auth.ApiKeyAuthRequestInterceptor;
import org.eclipse.slm.common.restclient.feign.auth.BearerTokenAuthRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResourceManagementClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceManagementClientFactory.class);

    private final static String RESOURCE_MANAGEMENT_API_HEADER_KEY = "X-API-KEY";

    private final LoadBalancerClient loadBalancerClient;
    private final ObjectFactory<HttpMessageConverters> messageConverters;

    @Value("${resource-management.url:}")
    private String resourceManagementBaseUrl;

    @Value("${resource-management.path:}")
    private String resourceManagementBasePath;

    private final String resourceManagementConsulServiceName = "resource-management";

    public ResourceManagementClientFactory(LoadBalancerClient loadBalancerClient, ObjectFactory<HttpMessageConverters> messageConverters) {
        this.loadBalancerClient = loadBalancerClient;
        this.messageConverters = messageConverters;
    }

    /**
     * Create a ResourceManagementClient with an additional request interceptor for bearer token authentication. The base URL is discovered via the
     * LoadBalancerClient which queries the service registry (e.g. Consul). If discovery returns no instance, a configured base URL is used.
     *
     * @param bearerToken Bearer token for authentication
     * @return ResourceManagementClient instance
     */
    public ResourceManagementClient createWithBearerTokenAuth(String bearerToken) {
        var baseUrl = resolveBaseUrl();

        var authRequestInterceptor = new BearerTokenAuthRequestInterceptor(bearerToken);

        var client = new ResourceManagementClient(baseUrl, messageConverters, authRequestInterceptor);
        return client;
    }

    /**
     * Create a ResourceManagementClient with an additional request interceptor for api key authentication. The base URL is discovered via the
     * LoadBalancerClient which queries the service registry (e.g. Consul). If discovery returns no instance, a configured base URL is used.
     *
     * @param apiKeyHeaderValue The api key header value
     * @return ResourceManagementClient instance
     */
    public ResourceManagementClient createWithApiKeyAuth(String apiKeyHeaderValue) {
        var baseUrl = resolveBaseUrl();

        var authRequestInterceptor = new ApiKeyAuthRequestInterceptor(apiKeyHeaderValue, ResourceManagementClientFactory.RESOURCE_MANAGEMENT_API_HEADER_KEY);

        var client = new ResourceManagementClient(baseUrl, messageConverters, authRequestInterceptor);
        return client;
    }

    private String resolveBaseUrl() {
        var serviceInstance = loadBalancerClient.choose(resourceManagementConsulServiceName);
        if (serviceInstance != null) {
            return serviceInstance.getUri().toString() + resourceManagementBasePath;
        }
        if (resourceManagementBaseUrl != null && !resourceManagementBaseUrl.isBlank()) {
            var url = stripTrailingSlash(resourceManagementBaseUrl);
            LOG.warn("No service instance '{}' was found via service discovery. Using configured Resource Management URL: {}", resourceManagementConsulServiceName, url);
            return url;
        }
        throw new RuntimeException("No service instance '" + resourceManagementConsulServiceName + "' or Resource Management URL (application.yml) was found");
    }

    private String stripTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

}
