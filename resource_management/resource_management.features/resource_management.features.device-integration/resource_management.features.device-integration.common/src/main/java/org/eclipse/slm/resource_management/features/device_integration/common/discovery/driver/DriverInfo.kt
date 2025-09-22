package org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver

class DriverInfo {
    var instanceId: String? = null

    var name: String? = null

    var version: String? = null

    var vendorName: String? = null

    var domainName: String? = null

    var portNumber: Int = 0

    var ipv4Address: String? = null

    var discoveryRequestFilters: MutableList<DiscoveryRequestFilter?>? = ArrayList()

    var discoveryRequestOptions: MutableList<DiscoveryRequestOption?>? = ArrayList()

    override fun toString(): String {
        return "DriverInfo{" +
                "instanceId='" + instanceId + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", domainName='" + domainName + '\'' +
                ", portNumber=" + portNumber +
                ", ipv4Address='" + ipv4Address +
                '}'
    }
}