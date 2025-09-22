package org.eclipse.slm.resource_management.features.device_integration.firmware_update.driver;

import org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver.DriverInfo;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.exceptions.DriverNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class FirmwareUpdateDriverClientFactory {

    public FirmwareUpdateDriverClientFactory() {
    }

    public FirmwareUpdateDriverClient createDriverClient(DriverInfo driverInfo) throws DriverNotFoundException {
        return new FirmwareUpdateDriverClient(driverInfo);
    }

}
