package org.eclipse.slm.resource_management.service.rest.capabilities;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulHealthApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.model.capabilities.Capability;
import org.eclipse.slm.resource_management.model.capabilities.DeploymentCapability;
import org.eclipse.slm.resource_management.model.capabilities.VirtualizationCapability;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityService;
import org.eclipse.slm.resource_management.model.consul.capability.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.persistence.api.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CapabilitiesConsulClient {
    //region Properties
    private final static Logger LOG = LoggerFactory.getLogger(CapabilitiesConsulClient.class);

    private final SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient;

    private final ConsulServicesApiClient consulServicesApiClient;

    private final ConsulNodesApiClient consulNodesApiClient;

    private final ConsulAclApiClient consulAclApiClient;

    private final ConsulHealthApiClient consulHealthApiClient;

    private final ResourcesConsulClient resourcesConsulClient;

    private final CapabilityJpaRepository capabilityJpaRepository;

    private final CapabilityUtil capabilityUtil;
    //endregion

    public CapabilitiesConsulClient(
            SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient,
            ConsulServicesApiClient consulServicesApiClient,
            ConsulNodesApiClient consulNodesApiClient,
            ConsulAclApiClient consulAclApiClient,
            ConsulHealthApiClient consulHealthApiClient,
            ResourcesConsulClient resourcesConsulClient,
            CapabilityJpaRepository capabilityJpaRepository,
            CapabilityUtil capabilityUtil
    ) {
        this.singleHostCapabilitiesConsulClient = singleHostCapabilitiesConsulClient;
        this.consulServicesApiClient = consulServicesApiClient;
        this.consulNodesApiClient = consulNodesApiClient;
        this.consulAclApiClient = consulAclApiClient;
        this.consulHealthApiClient = consulHealthApiClient;
        this.resourcesConsulClient = resourcesConsulClient;
        this.capabilityJpaRepository = capabilityJpaRepository;
        this.capabilityUtil = capabilityUtil;
    }

    public List<CapabilityService> getCapabilityServicesByCapabilityClass(
            ConsulCredential consulCredential,
            Class capabilityClass
    ) throws ConsulLoginFailedException {
        Map<String, List<String>> ServiceNamesToTagsMap = capabilityUtil.getCapabilityServiceNamesAndTagsMapByCapabilityClass(
                consulCredential,
                capabilityClass
        );

        Map<String, List<CatalogService>> serviceNameToCatalogServiceMap = consulServicesApiClient.getServicesByName(
                consulCredential,
                ServiceNamesToTagsMap.keySet()
        );

        List<CapabilityService> capabilityServices = new ArrayList<>();

        for(var catalogServiceName : serviceNameToCatalogServiceMap.keySet()) {
            var catalogService = serviceNameToCatalogServiceMap.get(catalogServiceName).get(0);
            capabilityServices.add(capabilityUtil.getCapabilityServiceFromCatalogService(
                    consulCredential,
                    catalogService
            ));
        }

        return capabilityServices;
    }

    //region Getter by resourceId
    private List<Capability> getInstalledCapabilitiesOfResourceByCapabilityClass(
            ConsulCredential consulCredential,
            UUID resourceId,
            Class capabilityClass
    ) throws ConsulLoginFailedException, ResourceNotFoundException {
        var consulNodeOfResourceOptional = this.consulNodesApiClient.getNodeById(consulCredential, resourceId);
        if (consulNodeOfResourceOptional.isEmpty()) {
            throw new ResourceNotFoundException(resourceId);
        }

        var capabilitiesOfResource = new ArrayList<Capability>();
        try {
            var consulNodeServices = this.consulServicesApiClient.getNodeServicesByNodeId(
                    consulCredential,
                    consulNodeOfResourceOptional.get().getId()
            );
            if(consulNodeServices.size() != 0) {
                List<NodeService> deploymentCapabilityNodeServices = consulNodeServices
                        .stream()
                        .filter(s -> s.getTags().contains(capabilityClass.getSimpleName()))
                        .collect(Collectors.toList());

                for (var nodeService : deploymentCapabilityNodeServices) {
                    var capabilityService = this.capabilityUtil.getCapabilityServiceFromNodeService(
                            consulCredential,
                            nodeService
                    );

                    if (capabilityService != null) {
                        if (capabilityService.getCapability().getHealthCheck() == null) {
                            capabilitiesOfResource.add(capabilityService.getCapability());
                        }
                        else {
                            var capabilityServiceHealthCheckStatus = this.consulHealthApiClient.getStatusOfServiceCheck(
                                    consulCredential,
                                    capabilityService.getService(),
                                    capabilityService.getCapability().getId().toString());
                            if (capabilityServiceHealthCheckStatus.equals("passing")) {
                                capabilitiesOfResource.add(capabilityService.getCapability());
                            }
                        }
                    }
                }
            }
        } catch (ConsulLoginFailedException e) {
            LOG.error("Unable to get deployment capabilities of resource [id='" + resourceId + "'], because login to " +
                    "Consul failed: " + e.getMessage());
            return capabilitiesOfResource;
        }

        return capabilitiesOfResource;
    }

    public List<VirtualizationCapability> getInstalledVirtualizationCapabilitiesOfResource(
            ConsulCredential consulCredential,
            UUID resourceId
    ) throws ConsulLoginFailedException, ResourceNotFoundException {
        List<Capability> capabilities = getInstalledCapabilitiesOfResourceByCapabilityClass(
                consulCredential,
                resourceId,
                VirtualizationCapability.class
        );

        return capabilities
                .stream()
                .map(genericCap -> (VirtualizationCapability) genericCap)
                .collect(Collectors.toList());
    }

    public List<DeploymentCapability> getInstalledDeploymentCapabilitiesOfResource(
            ConsulCredential consulCredential,
            UUID resourceId
    ) throws ResourceNotFoundException, ConsulLoginFailedException {
        List<Capability> capabilities = getInstalledCapabilitiesOfResourceByCapabilityClass(
                consulCredential,
                resourceId,
                DeploymentCapability.class
        );

        return capabilities
                .stream()
                .map(genericCap -> (DeploymentCapability) genericCap)
                .collect(Collectors.toList());
    }

    public List<Capability> getInstalledSingleHostCapabilitiesOfResource(
            ConsulCredential consulCredential,
            UUID resourceId
    ) throws ConsulLoginFailedException, ResourceNotFoundException {
        return getInstalledCapabilitiesOfResourceByCapabilityClass(
                consulCredential,
                resourceId,
                SingleHostCapabilityService.class
        );
    }
    //endregion
}
