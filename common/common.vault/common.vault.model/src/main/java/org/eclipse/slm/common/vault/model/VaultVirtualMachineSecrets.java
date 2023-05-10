package org.eclipse.slm.common.vault.model;

public class VaultVirtualMachineSecrets {

    private String execution_uuid;

    private String hostname;

    private String ip;

    private String password;

    private String username;

    private String vsphere_instance_uuid;

    public String getExecution_uuid() {
        return execution_uuid;
    }

    public void setExecution_uuid(String execution_uuid) {
        this.execution_uuid = execution_uuid;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVsphere_instance_uuid() {
        return vsphere_instance_uuid;
    }

    public void setVsphere_instance_uuid(String vsphere_instance_uuid) {
        this.vsphere_instance_uuid = vsphere_instance_uuid;
    }
}
