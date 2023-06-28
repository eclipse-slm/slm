package org.eclipse.slm.tests.stack;

public class TestResource {
    String hostname;
    String ip;
    String username;
    String password;

    public TestResource() {
    }

    public TestResource(java.lang.String hostname, java.lang.String ip, java.lang.String username, java.lang.String password) {
        this.hostname = hostname;
        this.ip = ip;
        this.username = username;
        this.password = password;
    }

    public java.lang.String getHostname() {
        return hostname;
    }

    public void setHostname(java.lang.String hostname) {
        this.hostname = hostname;
    }

    public java.lang.String getIp() {
        return ip;
    }

    public void setIp(java.lang.String ip) {
        this.ip = ip;
    }

    public java.lang.String getUsername() {
        return username;
    }

    public void setUsername(java.lang.String username) {
        this.username = username;
    }

    public java.lang.String getPassword() {
        return password;
    }

    public void setPassword(java.lang.String password) {
        this.password = password;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "TestResource{" +
                "hostname=" + hostname +
                ", ip=" + ip +
                ", username=" + username +
                ", password=" + password +
                '}';
    }
}
