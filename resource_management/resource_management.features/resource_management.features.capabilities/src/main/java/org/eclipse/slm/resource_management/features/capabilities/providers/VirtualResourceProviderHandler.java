package org.eclipse.slm.resource_management.features.capabilities.providers;

import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.VirtualizationCapability;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.providers.VirtualResourceProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class VirtualResourceProviderHandler extends ProviderHandler {

    private ConsulServicesApiClient consulServicesApiClient;

    public VirtualResourceProviderHandler(
            CapabilitiesConsulClient capabilitiesConsulClient,
            ConsulServicesApiClient consulServicesApiClient
    ) {
        super(
                VirtualizationCapability.class,
                capabilitiesConsulClient
        );

        this.consulServicesApiClient = consulServicesApiClient;
    }

    public List<VirtualResourceProvider> getVirtualResourceProviders(
            ConsulCredential consulCredential
    ) throws ConsulLoginFailedException {
        List<VirtualResourceProvider> virtualResourceProviders = new ArrayList<>();

        List<CapabilityService> capabilityServices = capabilitiesConsulClient.getCapabilityServicesByCapabilityClass(
                consulCredential,
                VirtualizationCapability.class
        );

        capabilityServices.stream().forEach(
                cs -> virtualResourceProviders.add(new VirtualResourceProvider(cs))
        );

        return virtualResourceProviders;
    }



    public void createVm(
            AwxCredential awxCredential,
            ConsulCredential consulCredential,
            UUID virtualResourceProviderId
    ) throws ConsulLoginFailedException {
        consulServicesApiClient.getServiceById(consulCredential, virtualResourceProviderId);
    }
}
