package org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.exceptions.DriverNotReachableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siemens.connectivitysuite.drvinfo.v1.ConnSuiteDrvInfo;
import siemens.connectivitysuite.drvinfo.v1.DriverInfoApiGrpc;

public abstract class AbstractDriverClient {

    public final static Logger LOG = LoggerFactory.getLogger(AbstractDriverClient.class);

    protected final DriverInfo driverInfo;

    protected ManagedChannel channel;

    public AbstractDriverClient(DriverInfo driverInfo) {
        this.driverInfo = driverInfo;
        this.channel = this.getChannel();
    }

    public ConnSuiteDrvInfo.VersionInfo getVersionInfo() throws DriverNotReachableException {
        var stub = DriverInfoApiGrpc.newBlockingStub(this.getChannel());

        try {
            var versionInfoResponse = stub.getVersionInfo(ConnSuiteDrvInfo.GetVersionInfoRequest.newBuilder().build());

            return versionInfoResponse.getVersion();
        } catch (Exception e) {
            LOG.debug("Failed to get version info from driver '{}:{}': {}", this.getDriverAddress(), this.getDriverPort(), e.getMessage());
            this.channel.shutdown();
            throw new DriverNotReachableException(this.getDriverAddress(), this.getDriverPort());
        }
    }

    public String getDriverAddress() {
        if (driverInfo.getIpv4Address().isEmpty()) {
            return driverInfo.getDomainName();
        } else {
            return driverInfo.getIpv4Address();
        }
    }

    public int getDriverPort() {
        return this.driverInfo.getPortNumber();
    }

    private ManagedChannel getChannel() {
        try {
            if (this.channel == null) {
                this.channel = ManagedChannelBuilder.forAddress(this.getDriverAddress(), this.getDriverPort())
                        .usePlaintext()
                        .build();
            }
        } catch (RuntimeException e) {
            LOG.error(e.getMessage());
        }

       return this.channel;
    }

    public void shutdown() {
        if (this.channel != null) {
            this.channel.shutdown();
        }
    }
}
