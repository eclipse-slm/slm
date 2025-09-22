package org.eclipse.slm.resource_management.features.device_integration.discovery.driver;

import org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver.DriverInfo;
import org.eclipse.slm.resource_management.features.device_integration.discovery.persistence.DiscoveryJobRepository;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryDriverClientFactory {


    private final DiscoveryJobRepository discoveryJobRepository;

    public DiscoveryDriverClientFactory(DiscoveryJobRepository discoveryJobRepository) {
        this.discoveryJobRepository = discoveryJobRepository;
    }

    public DiscoveryDriverClient createDriverClient(DriverInfo driverInfo){
        return new DiscoveryDriverClient(driverInfo, this.discoveryJobRepository);
    }


}
