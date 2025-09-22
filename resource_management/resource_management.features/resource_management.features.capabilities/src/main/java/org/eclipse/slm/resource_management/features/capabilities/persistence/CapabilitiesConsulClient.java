package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulHealthApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.CapabilityUtil;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CapabilitiesConsulClient {
    private final static Logger LOG = LoggerFactory.getLogger(CapabilitiesConsulClient.class);

    private final SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient;

    private final ConsulServicesApiClient consulServicesApiClient;

    private final ConsulNodesApiClient consulNodesApiClient;

    private final ConsulAclApiClient consulAclApiClient;

    private final ConsulHealthApiClient consulHealthApiClient;

    private final ResourcesConsulClient resourcesConsulClient;

    private final CapabilityJpaRepository capabilityJpaRepository;

    private final CapabilityUtil capabilityUtil;


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

    private List<CapabilityService> getCapabilityServicesByTag(ConsulCredential consulCredential, String tag
    ) throws ConsulLoginFailedException {
        Map<String, List<String>> ServiceNamesToTagsMap = capabilityUtil.getCapabilityServiceNamesAndTagsMapByTag(
                consulCredential,
                tag
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

    public List<CapabilityService> getCapabilityServices(ConsulCredential consulCredential) throws ConsulLoginFailedException {
        var capabilityServices = this.getCapabilityServicesByTag(
                consulCredential,
                CapabilityService.TAG_CAPABILITY
        );

        return capabilityServices;
    }

    public List<CapabilityService> getCapabilityServicesByCapabilityClass(ConsulCredential consulCredential, Class capabilityClass
    ) throws ConsulLoginFailedException {
        var capabilityServices = this.getCapabilityServicesByTag(
                consulCredential,
                capabilityClass.getSimpleName()
        );

        return capabilityServices;
    }

    public List<CapabilityService> getCapabilityServicesOfResource(
            UUID resourceId
    ) throws ConsulLoginFailedException, ResourceNotFoundException {
        var consulCredential = new ConsulCredential();

        var consulNodeOfResourceOptional = this.consulNodesApiClient.getNodeById(consulCredential, resourceId);
        if (consulNodeOfResourceOptional.isEmpty()) {
            throw new ResourceNotFoundException(resourceId);
        }

        var capabilityServicesOfResource = new ArrayList<CapabilityService>();
        try {
            var consulNodeServices = this.consulServicesApiClient.getNodeServicesByNodeId(consulCredential, consulNodeOfResourceOptional.get().getId());

            var capabilityNodeServices = consulNodeServices
                    .stream()
                    .filter(s -> s.getTags().contains(CapabilityService.TAG_CAPABILITY))
                    .toList();

            for (var nodeService : capabilityNodeServices) {
                var capabilityService = this.capabilityUtil.getCapabilityServiceFromNodeService(consulCredential, nodeService);
                capabilityServicesOfResource.add(capabilityService);
            }
        } catch (ConsulLoginFailedException e) {
            LOG.error("Unable to get deployment capabilities of resource [id='" + resourceId + "'], because login to " +
                    "Consul failed: " + e.getMessage());
            return capabilityServicesOfResource;
        }

        return capabilityServicesOfResource;
    }
}
