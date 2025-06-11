package org.eclipse.slm.resource_management.service.discovery.driver;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.eclipse.slm.resource_management.model.discovery.*;
import org.eclipse.slm.resource_management.persistence.api.DiscoveryJobRepository;
import org.eclipse.slm.resource_management.service.discovery.exceptions.DriverNotReachableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siemens.common.filters.v1.CommonFilters;
import siemens.common.types.v1.CommonVariant;
import siemens.connectivitysuite.drvinfo.v1.ConnSuiteDrvInfo;
import siemens.connectivitysuite.drvinfo.v1.DriverInfoApiGrpc;

import java.util.ArrayList;
import java.util.List;

public class DriverClient {

    public final static Logger LOG = LoggerFactory.getLogger(DriverClient.class);

    private final DriverInfo driverInfo;

    private final DiscoveryJobRepository discoveryJobRepository;

    private ManagedChannel channel;

    public DriverClient(DriverInfo driverInfo, DiscoveryJobRepository discoveryJobRepository) {
        this.driverInfo = driverInfo;
        this.discoveryJobRepository = discoveryJobRepository;

        this.channel = this.getChannel();
    }

    public DiscoveryJob discover(DiscoveryJobListener discoveryJobListener, DiscoveryRequest discoveryRequest) {
        var discoverRequestBuilder = siemens.industrialassethub.discover.v1.IahDiscover.DiscoverRequest.newBuilder();

        for (var filterValue : discoveryRequest.getFilterValues().entrySet()) {
            var encodedValue = CommonVariant.Variant.newBuilder().setRawData(ByteString.copyFromUtf8(filterValue.getValue()));
            var filter = CommonFilters.ActiveFilter.newBuilder()
                    .setKey(filterValue.getKey())
                    .setValue(encodedValue)
                    .build();
            discoverRequestBuilder.addFilters(filter);
        }

        for (var filterValue : discoveryRequest.getOptionValues().entrySet()) {
            if (filterValue.getValue() != null) {
                var encodedValue = CommonVariant.Variant.newBuilder().setRawData(ByteString.copyFromUtf8(filterValue.getValue()));
                var option = CommonFilters.ActiveOption.newBuilder()
                        .setKey(filterValue.getKey())
                        .setValue(encodedValue)
                        .build();
                discoverRequestBuilder.addOptions(option);
            }
        }

        var asyncStub = siemens.industrialassethub.discover.v1.DeviceDiscoverApiGrpc.newStub(this.channel);

        var discoverResponseStreamObserver = new DiscoverResponseStreamObserver(this.driverInfo, this.discoveryJobRepository, this.channel);
        discoverResponseStreamObserver.addListener(discoveryJobListener);

        LOG.info("Starting discovery job '{}' for driver '{}'", discoverResponseStreamObserver.getDiscoveryJob().getId(), this.driverInfo);

        asyncStub.discoverDevices(discoverRequestBuilder.build(), discoverResponseStreamObserver);

        var discoveryJob = discoverResponseStreamObserver.getDiscoveryJob();
        this.discoveryJobRepository.save(discoveryJob);

        LOG.info("Completed Discovery job '{}' of driver '{}'", discoveryJob.getId(), this.driverInfo);

        return discoveryJob;
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

    public List<DiscoveryRequestFilter> getDiscoveryRequestFilters () {
        var stub = siemens.industrialassethub.discover.v1.DeviceDiscoverApiGrpc.newBlockingStub(this.channel);

        var discoveryRequestFilters = new ArrayList<DiscoveryRequestFilter>();

        try {
            var filterTypesRequest = CommonFilters.FilterTypesRequest.newBuilder().build();
            var filterTypesResponse = stub.getFilterTypes(filterTypesRequest);

            for (var filterType : filterTypesResponse.getFilterTypesList()) {
                var discoveryRequestFilter = new DiscoveryRequestFilter();
                discoveryRequestFilter.setKey(filterType.getKey());
                discoveryRequestFilter.setDataType(filterType.getDatatype().name());

                discoveryRequestFilters.add(discoveryRequestFilter);
            }
        } catch (io.grpc.StatusRuntimeException e) {
            if (!e.getMessage().equals("UNKNOWN: no supported filters")) {
                LOG.error(e.getMessage());
            }
        }

        return discoveryRequestFilters;
    }

    public List<DiscoveryRequestOption> getDiscoveryRequestOptions() {
        var stub = siemens.industrialassethub.discover.v1.DeviceDiscoverApiGrpc.newBlockingStub(this.channel);

        var discoveryRequestOptions = new ArrayList<DiscoveryRequestOption>();

        try {
            var filterOptionsRequest = CommonFilters.FilterOptionsRequest.newBuilder().build();
            var filterOptionsResponse = stub.getFilterOptions(filterOptionsRequest);

            for (var filterOption : filterOptionsResponse.getFilterOptionsList()) {
                var discoveryRequestOption = new DiscoveryRequestOption();
                discoveryRequestOption.setKey(filterOption.getKey());
                discoveryRequestOption.setDataType(filterOption.getDatatype().name());

                discoveryRequestOptions.add(discoveryRequestOption);
            }
        } catch (io.grpc.StatusRuntimeException e) {
            if (!e.getMessage().equals("UNKNOWN: no supported filter types")) {
                LOG.error(e.getMessage());
            }
        }

        return discoveryRequestOptions;
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
