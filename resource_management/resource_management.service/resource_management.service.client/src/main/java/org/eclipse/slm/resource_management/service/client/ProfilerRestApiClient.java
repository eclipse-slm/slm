package org.eclipse.slm.resource_management.service.client;

import org.eclipse.slm.resource_management.common.api.ResourceManagementApiConfig;
import org.eclipse.slm.resource_management.features.profiler.ProfilerRestApi;
import org.eclipse.slm.resource_management.features.profiler.ProfilerRestApiConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "${resource-management.consul-service-name:resource-management}",
        contextId = "resourceManagementProfilerClient",
        path = ResourceManagementApiConfig.BASE_PATH + ProfilerRestApiConfig.BASE_PATH
)
public interface ProfilerRestApiClient extends ProfilerRestApi {
}
