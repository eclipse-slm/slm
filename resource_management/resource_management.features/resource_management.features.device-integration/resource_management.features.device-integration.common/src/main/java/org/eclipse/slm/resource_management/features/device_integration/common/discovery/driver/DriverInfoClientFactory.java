package org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver;

import org.springframework.stereotype.Component;

@Component
public class DriverInfoClientFactory {

    public DriverInfoDriverClient createDriverClient(DriverInfo driverInfo){
        return new DriverInfoDriverClient(driverInfo);
    }

}
