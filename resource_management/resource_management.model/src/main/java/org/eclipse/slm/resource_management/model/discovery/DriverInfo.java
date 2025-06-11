package org.eclipse.slm.resource_management.model.discovery;

import java.util.ArrayList;
import java.util.List;

public class DriverInfo {

    private String instanceId;

    private String name;

    private String version;

    private String vendorName;

    private String domainName;

    private int portNumber;

    private String ipv4Address;

    private List<DiscoveryRequestFilter> discoveryRequestFilters = new ArrayList<>();

    private List<DiscoveryRequestOption> discoveryRequestOptions = new ArrayList<>();

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getIpv4Address() {
        return ipv4Address;
    }

    public void setIpv4Address(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    public List<DiscoveryRequestFilter> getDiscoveryRequestFilters() {
        return discoveryRequestFilters;
    }

    public void setDiscoveryRequestFilters(List<DiscoveryRequestFilter> discoveryRequestFilters) {
        this.discoveryRequestFilters = discoveryRequestFilters;
    }

    public List<DiscoveryRequestOption> getDiscoveryRequestOptions() {
        return discoveryRequestOptions;
    }

    public void setDiscoveryRequestOptions(List<DiscoveryRequestOption> discoveryRequestOptions) {
        this.discoveryRequestOptions = discoveryRequestOptions;
    }

    public String toString() {
        return "DriverInfo{" +
                "instanceId='" + instanceId + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", domainName='" + domainName + '\'' +
                ", portNumber=" + portNumber +
                ", ipv4Address='" + ipv4Address   +
                '}';
    }
}
