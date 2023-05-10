package org.eclipse.slm.common.vault.model;

public class LoginRequest {
    public String role = "default";
    public String jwt;

    public LoginRequest() {
    }

    public LoginRequest(String jwt) {
        this.jwt = jwt;
    }

    public LoginRequest(String role, String jwt) {
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
