package org.eclipse.slm.common.awx.model;

public class TokenRequest {
    String grant_type = "password";
    String username;
    String password;
    String scope;

    public TokenRequest() {
    }

    public TokenRequest(String username, String password, String scope) {
        this.grant_type = grant_type;
        this.username = username;
        this.password = password;
        this.scope = scope;
    }

    public TokenRequest(String grant_type, String username, String password, String scope) {
        this.grant_type = grant_type;
        this.username = username;
        this.password = password;
        this.scope = scope;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public void setGrant_type(String grant_type) {
        this.grant_type = grant_type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
