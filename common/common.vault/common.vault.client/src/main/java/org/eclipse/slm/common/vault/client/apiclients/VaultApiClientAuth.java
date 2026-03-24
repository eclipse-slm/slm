package org.eclipse.slm.common.vault.client.apiclients;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.eclipse.slm.common.vault.model.auth.AppRoleLoginRequest;
import org.eclipse.slm.common.vault.model.VaultApiResponse;
import org.eclipse.slm.common.vault.model.auth.AuthDetails;
import org.eclipse.slm.common.vault.model.auth.JwtLoginRequest;

import java.util.Map;

public interface VaultApiClientAuth {

    @RequestLine("POST /auth/{mountAuthPath}/config")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    void createAuthPathConfig(@Param("mountAuthPath") String mountAuthPath, Map<String, String> body);

    @RequestLine("POST /auth/{mountAuthPath}/role/default")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    void createDefaultRoleForAuthPath(@Param("mountAuthPath") String mountAuthPath, Map<String, Object> body);

    @RequestLine("POST /auth/jwt/login")
    @Headers("Content-Type: application/json")
    @Body("{loginRequest}")
    VaultApiResponse<AuthDetails> loginWithJwt(JwtLoginRequest jwtLoginRequest);

    @RequestLine("POST /auth/approle/login")
    @Headers("Content-Type: application/json")
    @Body("{loginRequest}")
    VaultApiResponse<AuthDetails> loginWithAppRole(AppRoleLoginRequest loginRequest);
}
