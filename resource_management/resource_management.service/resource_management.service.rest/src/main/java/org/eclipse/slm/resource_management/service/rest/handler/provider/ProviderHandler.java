package org.eclipse.slm.resource_management.service.rest.handler.provider;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.model.capabilities.provider.Provider;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityService;
import org.eclipse.slm.resource_management.service.rest.capabilities.CapabilitiesConsulClient;

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
