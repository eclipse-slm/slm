package org.eclipse.slm.resource_management.service.discovery.driver;

import org.eclipse.slm.resource_management.model.discovery.DriverInfo;
import org.eclipse.slm.resource_management.persistence.api.DiscoveryJobRepository;
import org.springframework.stereotype.Component;

@Component
public class DriverClientFactory {

    private final DiscoveryJobRepository discoveryJobRepository;

    public DriverClientFactory(DiscoveryJobRepository discoveryJobRepository) {
        this.discoveryJobRepository = discoveryJobRepository;
    }

    public DriverClient createDriverClient(DriverInfo driverInfo) {
        return new DriverClient(driverInfo, this.discoveryJobRepository);
    }

}
