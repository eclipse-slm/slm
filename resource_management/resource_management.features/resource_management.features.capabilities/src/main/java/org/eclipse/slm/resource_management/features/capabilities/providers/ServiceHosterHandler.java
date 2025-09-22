package org.eclipse.slm.resource_management.features.capabilities.providers;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.providers.ServiceHoster;
import org.eclipse.slm.resource_management.features.providers.ServiceHosterFilter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ServiceHosterHandler extends ProviderHandler {

    public ServiceHosterHandler(CapabilitiesConsulClient capabilitiesConsulClient) {
        super(
                DeploymentCapability.class,
                capabilitiesConsulClient
        );
    }
    public List<ServiceHoster> getServiceHosters(
            ConsulCredential consulCredential,
            Optional<ServiceHosterFilter> filter) throws ConsulLoginFailedException {
        List<ServiceHoster> serviceHosterList = new ArrayList<>();

        List<CapabilityService> deploymentCapabilityServices = capabilitiesConsulClient.getCapabilityServicesByCapabilityClass(
                consulCredential,
                DeploymentCapability.class
        );

        List<CapabilityService> filteredList = new ArrayList<>(deploymentCapabilityServices);
        if (filter.isPresent()) {
            if (filter.get().getCapabilityServiceId() != null) {
                filteredList = filteredList.stream()
                        .filter(dcs -> dcs.getId().equals(filter.get().getCapabilityServiceId()))
                        .collect(Collectors.toList());
            }

            if (filter.get().getSupportedDeploymentType() != null) {
                filteredList = filteredList.stream()
                        .filter(dcs -> {
                            if (dcs.getCapability() instanceof DeploymentCapability) {
                                return ((DeploymentCapability)dcs.getCapability()).getSupportedDeploymentTypes()
                                        .contains(filter.get().getSupportedDeploymentType());
                            }
                            else {
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
            }

            if (filter.get().getCapabilityHostType() != null) {
                filteredList = filteredList.stream()
                        .filter(dcs -> {
                            switch (filter.get().getCapabilityHostType()) {
                                case SINGLE_HOST -> {
                                    return dcs instanceof SingleHostCapabilityService;
                                }
                                case MULTI_HOST -> {
                                    return dcs instanceof MultiHostCapabilityService;
                                }
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
            }
        }

        filteredList.stream().forEach(
                dcs -> serviceHosterList.add(new ServiceHoster(dcs))
        );

        return serviceHosterList;
    }
}
