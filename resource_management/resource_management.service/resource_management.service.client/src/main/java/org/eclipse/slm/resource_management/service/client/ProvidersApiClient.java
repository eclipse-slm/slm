package org.eclipse.slm.resource_management.service.client;

import org.eclipse.slm.resource_management.common.api.ResourceManagementApiConfig;
import org.eclipse.slm.resource_management.features.capabilities.CapabilitiesRestApi;
import org.eclipse.slm.resource_management.features.capabilities.providers.ProvidersRestApi;
import org.eclipse.slm.resource_management.features.capabilities.providers.ProvidersRestApiConfig;
import org.eclipse.slm.resource_management.features.profiler.ProfilerRestApiConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "resource-management",
        contextId = "resourceManagementProvidersClient",
        path = ResourceManagementApiConfig.BASE_PATH + ProvidersRestApiConfig.BASE_PATH
)
public interface ProvidersApiClient extends ProvidersRestApi {
}
