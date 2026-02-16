package org.eclipse.slm.resource_management.service.client;

import org.eclipse.slm.common.parent.client.AbstractApiClient;
import org.eclipse.slm.common.restclient.feign.auth.AuthRequestInterceptor;
import org.eclipse.slm.platform_management.service.api.credentials.CredentialManagementRestApiConfig;
import org.eclipse.slm.resource_management.features.capabilities.CapabilitiesRestApiConfig;
import org.eclipse.slm.resource_management.features.capabilities.providers.ProvidersRestApiConfig;
import org.eclipse.slm.resource_management.features.profiler.ProfilerRestApiConfig;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;

public class ResourceManagementClient extends AbstractApiClient {

    private final ProfilerRestApiClient profilerRestApiClient;
    private final CapabilitiesApiClient capabilitiesApiClient;
    private final ProvidersApiClient providersApiClient;

    public ResourceManagementClient(String platformManagementBaseUrl,
                                    ObjectFactory<HttpMessageConverters> messageConverters,
                                    AuthRequestInterceptor authRequestInterceptor) {
        super(platformManagementBaseUrl, messageConverters, authRequestInterceptor);

        var profilerRestApiBaseUrl = this.baseUrl + ProfilerRestApiConfig.BASE_PATH;
        this.profilerRestApiClient = this.buildFeignClient(ProfilerRestApiClient.class, profilerRestApiBaseUrl);
        var capabilitiesRestApiBaseUrl = this.baseUrl + CapabilitiesRestApiConfig.BASE_PATH;
        this.capabilitiesApiClient = this.buildFeignClient(CapabilitiesApiClient.class, capabilitiesRestApiBaseUrl);
        var providersRestApiBaseUrl = this.baseUrl + ProvidersRestApiConfig.BASE_PATH;
        this.providersApiClient = this.buildFeignClient(ProvidersApiClient.class, providersRestApiBaseUrl);
    }

    public ProfilerRestApiClient profiler() {
        return profilerRestApiClient;
    }
    public CapabilitiesApiClient capabilities() {
        return this.capabilitiesApiClient;
    }
    public ProvidersApiClient providers() {
        return providersApiClient;
    }
}
