package org.eclipse.slm.common.vault.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtLoginRequest {
    public String role = "default";
    public String jwt;

    public JwtLoginRequest() {
    }

    public JwtLoginRequest(String jwt) {
        this.jwt = jwt;
    }

    public JwtLoginRequest(String role, String jwt) {
        this.role = role;
        this.jwt = jwt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}
