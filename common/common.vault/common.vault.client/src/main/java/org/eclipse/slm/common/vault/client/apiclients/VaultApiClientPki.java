package org.eclipse.slm.common.vault.client.apiclients;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.eclipse.slm.common.vault.model.VaultApiResponse;
import org.eclipse.slm.common.vault.model.pki.*;

public interface VaultApiClientPki {

    @RequestLine("GET /{pkiName}/config/issuers")
    VaultApiResponse<IssuerRef> getIssuerRef(@Param("pkiName") String pkiName);

    @RequestLine("POST /{pkiName}/roles/{roleName}")
    @Headers({"Content-Type: application/json",})
    @Body("{request}")
    void createIntermediateRole(@Param("pkiName") String pkiName, @Param("roleName") String roleName, CreateIntermediateRoleRequest request);

    @RequestLine("POST /{pkiName}/roles/{roleName}")
    void removeRoleFromPki(@Param("pkiName") String pkiName, @Param("roleName") String roleName);

    @RequestLine("POST /sys/mounts/{pkiName}/tune")
    @Headers("Content-Type: application/json")
    @Body("{request}")
    void tunePkiSecretsEngine(@Param("pkiName") String pkiName, TunePkiSecretsEngineRequest request);

    @RequestLine("POST /{pkiName}/intermediate/generate/internal")
    @Headers("Content-Type: application/json")
    @Body("{request}")
    VaultApiResponse<GenerateCACertResponse> generateIntermediate(@Param("pkiName") String pkiName, GenerateCACertRequest request);

    @RequestLine("POST /{rootPkiName}/root/sign-intermediate")
    @Headers("Content-Type: application/json")
    @Body("{request}")
    VaultApiResponse<SignIntermediateCertResponse> signIntermediateCert(@Param("rootPkiName") String rootPkiName, SignIntermediateCertRequest request);

    @RequestLine("POST /{pkiName}/intermediate/set-signed")
    @Headers("Content-Type: application/json")
    @Body("{request}")
    void importSignedIntermediateCert(@Param("pkiName") String pkiName, ImportSignedIntermediateCertRequest request);

    @RequestLine("POST /{pkiName}/root/generate/internal")
    @Headers("Content-Type: application/json")
    @Body("{request}")
    VaultApiResponse<GenerateCACertResponse> generateRootInternalCA(@Param("pkiName") String pkiName, GenerateCACertRequest request);
}
