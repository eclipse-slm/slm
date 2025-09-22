package org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.exceptions.DriverNotFoundException;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.exceptions.DriverNotReachableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import siemens.connectivitysuite.registry.v1.ConnSuiteRegistry;
import siemens.connectivitysuite.registry.v1.RegistryApiGrpc;

import java.util.ArrayList;
import java.util.List;

@Component
public class DriverRegistryClient {

    public final static Logger LOG = LoggerFactory.getLogger(DriverRegistryClient.class);

    private final String driverRegistryAddress;

    private final int driverRegistryPort;

    private final DriverInfoClientFactory driverInfoClientFactory;

    public DriverRegistryClient(@Value("${discovery.driver-registry.address}") String driverRegistryAddress,
                                @Value("${discovery.driver-registry.port}") int driverRegistryPort,
                                DriverInfoClientFactory driverInfoClientFactory) {
        this.driverRegistryAddress = driverRegistryAddress;
        this.driverRegistryPort = driverRegistryPort;
        this.driverInfoClientFactory = driverInfoClientFactory;
    }

    public List<DriverInfo> getRegisteredDrivers() {
        ManagedChannel channel = null;
        try {
            channel = ManagedChannelBuilder.forAddress(this.driverRegistryAddress, this.driverRegistryPort)
                    .usePlaintext()
                    .build();


            var registryQueryRequest = ConnSuiteRegistry.QueryRegisteredServicesRequest.newBuilder().build();

            var stub = RegistryApiGrpc.newBlockingStub(channel);
            var response = stub.queryRegisteredServices(registryQueryRequest);

            var driverInfos = new ArrayList<DriverInfo>();
            LOG.info("Found {} registered drivers: {}", response.getInfosCount(), response.getInfosList().stream().map(ConnSuiteRegistry.ServiceInfo::getAppInstanceId).toArray());
            for (var serviceInfo : response.getInfosList()) {
                var driverInfo = new DriverInfo();
                driverInfo.setInstanceId(serviceInfo.getAppInstanceId());
                driverInfo.setIpv4Address(serviceInfo.getIpv4Address());
                driverInfo.setDomainName(serviceInfo.getDnsDomainname());
                driverInfo.setPortNumber(serviceInfo.getGrpcIpPortNumber());

                try {
                    var driverClient = this.driverInfoClientFactory.createDriverClient(driverInfo);
                    LOG.debug("Getting version info for driver: Id: {} | IP: {} | DomainName: {} | Port: {} |",
                            driverInfo.getInstanceId(),
                            driverInfo.getIpv4Address(),
                            driverInfo.getDomainName(),
                            driverInfo.getPortNumber());
                    var versionInfo = driverClient.getVersionInfo();

                    driverInfo.setName(versionInfo.getProductName());
                    driverInfo.setVendorName(versionInfo.getVendorName());
                    var majorVersion = versionInfo.getMajor();
                    var minorVersion = versionInfo.getMinor();
                    var patchVersion = versionInfo.getPatch();
                    driverInfo.setVersion(majorVersion + "." + minorVersion + "." + patchVersion);

                    var discoveryRequestFilters = driverClient.getDiscoveryRequestFilters();
                    driverInfo.setDiscoveryRequestFilters(discoveryRequestFilters);

                    var discoveryRequestOptions = driverClient.getDiscoveryRequestOptions();
                    driverInfo.setDiscoveryRequestOptions(discoveryRequestOptions);

                    driverInfos.add(driverInfo);

                    driverClient.shutdown();
                } catch (DriverNotReachableException e) {
                    LOG.warn("Failed to get version info for driver: {}", driverInfo.getInstanceId());
                }
            }

            return driverInfos;
        } finally {
            if (channel != null) channel.shutdown();
        }
    }

    public DriverInfo getRegisteredDriver(String driverId) throws DriverNotFoundException {
        var optionalDriverInfo = this.getRegisteredDrivers().stream()
                .filter(driverInfo -> driverInfo.getInstanceId().equals(driverId))
                .findAny();

        if (optionalDriverInfo.isPresent()) {
            return optionalDriverInfo.get();
        }
        else {
            throw new DriverNotFoundException(driverId);
        }
    }

}
