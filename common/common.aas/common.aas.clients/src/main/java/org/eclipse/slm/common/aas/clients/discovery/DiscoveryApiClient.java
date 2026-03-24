package org.eclipse.slm.common.aas.clients.discovery;

import feign.RequestLine;
import feign.Param;

public interface DiscoveryApiClient {

    @RequestLine("GET /lookup/shells?assetId={assetId}")
    String[] getAllAssetAdministrationShellIdsByAssetId(@Param("assetId") String assetId);

}
