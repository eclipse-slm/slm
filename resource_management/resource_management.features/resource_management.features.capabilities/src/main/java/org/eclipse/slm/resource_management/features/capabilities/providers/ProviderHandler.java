package org.eclipse.slm.resource_management.features.capabilities.providers;


import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityServiceQueryService;

import java.util.ArrayList;
import java.util.List;

public class ProviderHandler {

    protected final CapabilityServiceQueryService capabilityServiceQueryService;
    Class capabilityClass;

    public ProviderHandler(
            Class capabilityClass,
            CapabilityServiceQueryService capabilityServiceQueryService
    ) {
        this.capabilityClass = capabilityClass;
        this.capabilityServiceQueryService = capabilityServiceQueryService;
    }

    protected List<Provider> getProvider() throws ConsulLoginFailedException {
        List<Provider> provider = new ArrayList<>();

        List<CapabilityService> capabilityServices = capabilityServiceQueryService.getCapabilityServicesByCapabilityClass(capabilityClass);

        capabilityServices.stream().forEach(
                cs -> provider.add(new Provider(cs, capabilityClass))
        );

        return provider;
    }
}
