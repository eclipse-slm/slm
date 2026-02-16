package org.eclipse.slm.platform_management.service.client;

import org.eclipse.slm.common.parent.client.AbstractApiClient;
import org.eclipse.slm.common.restclient.feign.auth.AuthRequestInterceptor;
import org.eclipse.slm.platform_management.service.api.credentials.CredentialManagementRestApiConfig;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;

public class PlatformManagementClient extends AbstractApiClient {

    private final PlatformManagementCredentialsClient credentialsClient;

    public PlatformManagementClient(String platformManagementBaseUrl,
                                    ObjectFactory<HttpMessageConverters> messageConverters,
                                    AuthRequestInterceptor authRequestInterceptor) {
        super(platformManagementBaseUrl, messageConverters, authRequestInterceptor);

        var credentialsApiBaseUrl = this.baseUrl + CredentialManagementRestApiConfig.BASE_PATH;
        this.credentialsClient = this.buildFeignClient(PlatformManagementCredentialsClient.class, credentialsApiBaseUrl);
    }

    public PlatformManagementCredentialsClient credentials() {
        return credentialsClient;
    }
}
