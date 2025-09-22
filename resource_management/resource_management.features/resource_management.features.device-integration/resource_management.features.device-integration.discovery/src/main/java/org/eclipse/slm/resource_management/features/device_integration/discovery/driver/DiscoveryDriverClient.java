package org.eclipse.slm.resource_management.features.device_integration.discovery.driver;

import com.google.protobuf.ByteString;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver.DriverInfo;
import org.eclipse.slm.resource_management.features.device_integration.discovery.model.*;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver.AbstractDriverClient;
import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveryJob;
import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveryRequest;
import org.eclipse.slm.resource_management.features.device_integration.discovery.persistence.DiscoveryJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siemens.common.filters.v1.CommonFilters;
import siemens.common.types.v1.CommonVariant;

public class DiscoveryDriverClient extends AbstractDriverClient {

    public final static Logger LOG = LoggerFactory.getLogger(DiscoveryDriverClient.class);

    private final DiscoveryJobRepository discoveryJobRepository;

    public DiscoveryDriverClient(DriverInfo driverInfo, DiscoveryJobRepository discoveryJobRepository) {
        super(driverInfo);
        this.discoveryJobRepository = discoveryJobRepository;
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
}
