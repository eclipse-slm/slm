package org.eclipse.slm.resource_management.service.client;

import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.resource_management.service.client.handler.ApiClient;
import org.eclipse.slm.resource_management.service.client.handler.auth.Authentication;
import org.eclipse.slm.resource_management.service.client.handler.auth.HttpBearerAuth;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.util.HashMap;

@Component
public class ResourceManagementApiClientInitializer {

    public final static Logger LOG = LoggerFactory.getLogger(ResourceManagementApiClientInitializer.class);

    private String resourceManagementUrl;

    public ResourceManagementApiClientInitializer(
            @Value("${resource-management.url}") String resourceManagementUrl) {

        this.resourceManagementUrl = resourceManagementUrl;
    }

    public ApiClient init(String accessToken) {
        var bearerAuth = new HttpBearerAuth("Bearer");
        bearerAuth.setBearerToken(accessToken);
        var authenticationMap = new HashMap<String, Authentication>();
        authenticationMap.put("bearer_auth", bearerAuth);

        var apiClient = new ApiClient();
        apiClient.setAccessToken(accessToken);
        apiClient.setBasePath(this.resourceManagementUrl);
        apiClient.getObjectMapper().registerModule(new KotlinModule.Builder().build());

        return apiClient;
    }

    public ApiClient init(KeycloakPrincipal keycloakPrincipal) throws SSLException {
        var accessToken = KeycloakTokenUtil.getToken(keycloakPrincipal);

        return init(accessToken);
    }

}
