package org.eclipse.slm.platform_management.service.client;

import org.eclipse.slm.platform_management.service.api.PlatformManagementApiConfig;
import org.eclipse.slm.platform_management.service.api.credentials.CredentialManagementRestApi;
import org.eclipse.slm.platform_management.service.api.credentials.CredentialManagementRestApiConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${platform-management.consul-service-name:platform-management}", path = PlatformManagementApiConfig.BASE_PATH + CredentialManagementRestApiConfig.BASE_PATH)
public interface PlatformManagementCredentialsClient extends CredentialManagementRestApi {
}
