package org.eclipse.slm.resource_management.service.client;

import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.resource_management.service.client.handler.ApiClient;
import org.eclipse.slm.resource_management.service.client.handler.auth.Authentication;
import org.eclipse.slm.resource_management.service.client.handler.auth.HttpBearerAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

@Component
public class ResourceManagementApiClientInitializer {

    public final static Logger LOG = LoggerFactory.getLogger(ResourceManagementApiClientInitializer.class);
    private final static String CONSUL_SERVICE_ID = "resource-management";
    private final DiscoveryClient discoveryClient;

    private String resourceManagementUrl;

    public ResourceManagementApiClientInitializer(
            @Value("${resource-management.url}") String resourceManagementUrl,
            DiscoveryClient discoveryClient
    ) {
        this.discoveryClient = discoveryClient;
        this.resourceManagementUrl = resourceManagementUrl;
    }

    public ApiClient init(String accessToken) {
        var bearerAuth = new HttpBearerAuth("Bearer");
        bearerAuth.setBearerToken(accessToken);
        var authenticationMap = new HashMap<String, Authentication>();
        authenticationMap.put("bearer_auth", bearerAuth);

        var apiClient = new ApiClient();
        apiClient.setAccessToken(accessToken);
        apiClient.setBasePath(getResourceManagementUrl().toString());
        apiClient.getObjectMapper().registerModule(new KotlinModule.Builder().build());

        return apiClient;
    }

    public ApiClient init(JwtAuthenticationToken jwtAuthenticationToken) throws SSLException {
        var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);

        return init(accessToken);
    }

    private URL getResourceManagementUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances(CONSUL_SERVICE_ID);

        URL resourceManagementUrl;
        try {
            if(instances.size() > 0) {
                resourceManagementUrl = new URL(
                        "http",
                        instances.get(0).getHost(),
                        instances.get(0).getPort(),
                        "/resource-management"
                );
            } else {
                resourceManagementUrl = new URL(this.resourceManagementUrl);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return resourceManagementUrl;
    }
}
