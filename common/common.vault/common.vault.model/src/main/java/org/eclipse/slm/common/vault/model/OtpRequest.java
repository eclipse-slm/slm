package org.eclipse.slm.common.vault.model;

public class OtpRequest {
    public String username;
    public String ip;

    public OtpRequest() {
    }

    public OtpRequest(String username, String ip) {
        this.username = username;
        this.ip = ip;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
