package org.eclipse.slm.resource_management.features.capabilities.providers;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.providers.Provider;

import java.util.ArrayList;
import java.util.List;

public class ProviderHandler {

    protected final CapabilitiesConsulClient capabilitiesConsulClient;
    Class capabilityClass;

    public ProviderHandler(
            Class capabilityClass,
            CapabilitiesConsulClient capabilitiesConsulClient
    ) {
        this.capabilityClass = capabilityClass;
        this.capabilitiesConsulClient = capabilitiesConsulClient;
    }

    protected List<Provider> getProvider(
            ConsulCredential consulCredential
    ) throws ConsulLoginFailedException {
        List<Provider> provider = new ArrayList<>();

        List<CapabilityService> capabilityServices = capabilitiesConsulClient.getCapabilityServicesByCapabilityClass(
                consulCredential,
                capabilityClass
        );

        capabilityServices.stream().forEach(
                cs -> provider.add(new Provider(cs, capabilityClass))
        );

        return provider;
    }
}
