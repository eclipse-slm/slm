package org.eclipse.slm.common.vault.client.apiclients;

import feign.*;
import org.eclipse.slm.common.vault.model.mounts.Mount;
import org.eclipse.slm.common.vault.model.acl.Policy;
import org.eclipse.slm.common.vault.model.mounts.SecretsEngine;
import org.eclipse.slm.common.vault.model.VaultApiResponse;
import org.eclipse.slm.common.vault.model.auth.AuthMethod;

import java.util.Map;

public interface VaultApiClientSys {

    //region /sys/policy
    @RequestLine("PUT /sys/policy/{policyName}")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    void createPolicy(@Param("policyName") String policyName, Map<String, String> body);

    @RequestLine("GET /sys/policy/{policyName}")
    VaultApiResponse<Policy> getPolicy(@Param("policyName") String policyName);

    @RequestLine("DELETE /sys/policy/{policyName}")
    void deletePolicy(@Param("policyName") String policyName);
    //endregion /sys/policy

    //region /sys/auth
    @RequestLine("POST /sys/auth/{mountAuthPath}")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    void createMountAuthPath(@Param("mountAuthPath") String mountAuthPath, Map<String, String> body);

    @RequestLine("GET /sys/auth")
    VaultApiResponse<Map<String, AuthMethod>> getAuthMethods();
    //endregion /sys/auth

    //region /sys/mounts
    @RequestLine("GET /sys/mounts")
    @Headers("Content-Type: application/json")
    VaultApiResponse<Map<String, Mount>> getMounts();

    @RequestLine("POST /sys/mounts/{path}")
    @Headers("Content-Type: application/json")
    @Body("{secretEngine}")
    void createSecretEngine(@Param("path") String path, SecretsEngine secretsEngine);

    @RequestLine("DELETE /sys/mounts/{path}")
    void deleteMount(@Param("path") String path);
    //endregion /sys/mounts
}
