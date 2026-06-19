package org.eclipse.slm.resource_management.service.client;

import org.eclipse.slm.resource_management.common.api.ResourceManagementApiConfig;
import org.eclipse.slm.resource_management.common.resources.ResourcesRestApi;
import org.eclipse.slm.resource_management.common.resources.ResourcesRestApiConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "${resource-management.consul-service-name:resource-management}",
        contextId = "resourceManagementResourcesClient",
        path = ResourceManagementApiConfig.BASE_PATH + ResourcesRestApiConfig.BASE_PATH
)
public interface ResourcesApiClient extends ResourcesRestApi {
}
