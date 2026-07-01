package org.eclipse.slm.service_management.service.client;

import org.eclipse.slm.common.restclient.feign.auth.ApiKeyAuthRequestInterceptor;
import org.eclipse.slm.common.restclient.feign.auth.BearerTokenAuthRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

@Component
public class ServiceManagementClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceManagementClientFactory.class);

    private final static String SERVICE_MANAGEMENT_API_HEADER_KEY = "X-API-KEY";

    private final LoadBalancerClient loadBalancerClient;
    private final ObjectFactory<HttpMessageConverters> messageConverters;

    @Value("${service-management.url:}")
    private String serviceManagementBaseUrl;

    @Value("${service-management.path:}")
    private String serviceManagementBasePath;

    @Value("service-management.consul-service-name:service-management")
    private String serviceManagementConsulServiceName;

    public ServiceManagementClientFactory(LoadBalancerClient loadBalancerClient, ObjectFactory<HttpMessageConverters> messageConverters) {
        this.loadBalancerClient = loadBalancerClient;
        this.messageConverters = messageConverters;
    }

    /**
     * Create a ServiceManagementClient with an additional request interceptor for bearer token authentication. The base URL is discovered via the
     * LoadBalancerClient which queries the service registry (e.g. Consul). If discovery returns no instance, a configured base URL is used.
     *
     * @param bearerToken Bearer token for authentication
     * @return ServiceManagementClient instance
     */
    public ServiceManagementClient createWithBearerTokenAuth(String bearerToken) {
        var baseUrl = resolveBaseUrl();

        var authRequestInterceptor = new BearerTokenAuthRequestInterceptor(bearerToken);

        var client = new ServiceManagementClient(baseUrl, messageConverters, authRequestInterceptor);
        return client;
    }

    /**
     * Create a ServiceManagementClient with an additional request interceptor for api key authentication. The base URL is discovered via the
     * LoadBalancerClient which queries the service registry (e.g. Consul). If discovery returns no instance, a configured base URL is used.
     *
     * @param apiKeyHeaderValue The api key header value
     * @return ServiceManagementClient instance
     */
    public ServiceManagementClient createWithApiKeyAuth(String apiKeyHeaderValue) {
        var baseUrl = resolveBaseUrl();

        var authRequestInterceptor = new ApiKeyAuthRequestInterceptor(apiKeyHeaderValue, ServiceManagementClientFactory.SERVICE_MANAGEMENT_API_HEADER_KEY);

        var client = new ServiceManagementClient(baseUrl, messageConverters, authRequestInterceptor);
        return client;
    }

    private String resolveBaseUrl() {
        var serviceInstance = loadBalancerClient.choose(serviceManagementConsulServiceName);
        if (serviceInstance != null) {
            return serviceInstance.getUri().toString() + serviceManagementBasePath;
        }
        if (serviceManagementBaseUrl != null && !serviceManagementBaseUrl.isBlank()) {
            var url = stripTrailingSlash(serviceManagementBaseUrl);
            LOG.warn("No service instance '{}' was found via service discovery. Using configured Service Management URL: {}", serviceManagementConsulServiceName, url);
            return url;
        }
        throw new RuntimeException("No service instance '" + serviceManagementConsulServiceName + "' or Service Management URL (application.yml) was found");
    }

    private String stripTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

}

