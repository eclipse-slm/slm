package org.eclipse.slm.resource_management.service.client;

import org.eclipse.slm.resource_management.common.api.ResourceManagementApiConfig;
import org.eclipse.slm.resource_management.features.capabilities.CapabilitiesRestApi;
import org.eclipse.slm.resource_management.features.capabilities.CapabilitiesRestApiConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "${resource-management.consul-service-name:resource-management}",
        contextId = "resourceManagementCapabilitiesClient",
        path = ResourceManagementApiConfig.BASE_PATH + CapabilitiesRestApiConfig.BASE_PATH
)
public interface CapabilitiesApiClient extends CapabilitiesRestApi {
}
