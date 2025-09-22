package org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siemens.common.filters.v1.CommonFilters;

import java.util.ArrayList;
import java.util.List;

public class DriverInfoDriverClient extends AbstractDriverClient {

    public final static Logger LOG = LoggerFactory.getLogger(DriverInfoDriverClient.class);

    public DriverInfoDriverClient(DriverInfo driverInfo) {
        super(driverInfo);
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
}
