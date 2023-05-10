package org.eclipse.slm.service_management.service.client;

import org.eclipse.slm.service_management.service.client.handler.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ServiceManagementApiClientInitializer {

    public final static Logger LOG = LoggerFactory.getLogger(ServiceManagementApiClientInitializer.class);

    private String serviceManagementUrl;

    public ServiceManagementApiClientInitializer(
            @Value("${service-management.url}") String serviceManagementUrl) {

        this.serviceManagementUrl = serviceManagementUrl;
    }

    public ApiClient init(String accessToken) {

        var bearerAuth = new org.eclipse.slm.service_management.service.client.handler.auth.HttpBearerAuth("Bearer");
        bearerAuth.setBearerToken(accessToken);
        var authenticationMap = new HashMap<String, org.eclipse.slm.service_management.service.client.handler.auth.Authentication>();
        authenticationMap.put("bearer_auth", bearerAuth);

        var apiClient = new ApiClient(authenticationMap);
        apiClient.setBasePath(this.serviceManagementUrl);

        return apiClient;
    }

}
