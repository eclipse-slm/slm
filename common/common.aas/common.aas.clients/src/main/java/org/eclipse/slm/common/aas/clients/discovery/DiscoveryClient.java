package org.eclipse.slm.common.aas.clients.discovery;

import feign.Param;
import org.eclipse.slm.common.aas.clients.auth.AuthRequestInterceptor;
import org.eclipse.slm.common.aas.clients.base.FeignClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryClient {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryClient.class);

    private final DiscoveryApiClient discoveryApiClient;

    public DiscoveryClient(String aasDiscoveryUrl, AuthRequestInterceptor authRequestInterceptor) {
        this.discoveryApiClient = FeignClientFactory.createClient(DiscoveryApiClient.class, aasDiscoveryUrl, authRequestInterceptor);
    }

    public String[] getAllAssetAdministrationShellIdsByAssetId(@Param("assetId") String assetId) {
        var assIds = this.discoveryApiClient.getAllAssetAdministrationShellIdsByAssetId(assetId);

        return assIds;
    }
}
