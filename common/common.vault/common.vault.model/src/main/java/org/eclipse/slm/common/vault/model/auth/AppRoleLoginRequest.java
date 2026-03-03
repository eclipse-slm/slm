package org.eclipse.slm.common.vault.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppRoleLoginRequest {

    @JsonProperty("role_id")
    String roleId;

    @JsonProperty("secret_id")
    String secretId;

    public AppRoleLoginRequest() {
    }

    public AppRoleLoginRequest(String roleId, String secretId) {
        this.roleId = roleId;
        this.secretId = secretId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    @Override
    public String toString() {
        return "AppRoleLoginRequest{" +
                "role_id='" + roleId + '\'' +
                ", secret_id='" + secretId + '\'' +
                '}';
    }
}
