package org.eclipse.slm.resource_management.features.capabilities.persistence;


import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CapabilitiesConsulClient {
    private final static Logger LOG = LoggerFactory.getLogger(CapabilitiesConsulClient.class);

    public static final String CAPABILITY_SERVICE_POLICY_PREFIX = "capability-service_";

    public static String getCapabilityServicePolicyName(UUID remoteAccessId) {
        return CAPABILITY_SERVICE_POLICY_PREFIX + remoteAccessId.toString();
    }

    private final ConsulClient consulAdminClient;

    private final CapabilityJpaRepository capabilityJpaRepository;

    @Autowired
    public CapabilitiesConsulClient(ConsulClientFactory consulClientFactory, CapabilityJpaRepository capabilityJpaRepository) {
        this(consulClientFactory.createAdminClient(), capabilityJpaRepository);
    }

    public CapabilitiesConsulClient(ConsulClient consulAdminClient, CapabilityJpaRepository capabilityJpaRepository) {
        this.consulAdminClient = consulAdminClient;
        this.capabilityJpaRepository = capabilityJpaRepository;
    }

    private List<CapabilityService> getCapabilityServicesByTag(String tag) {
        var serviceNameToServiceTagMap = this.consulAdminClient.services().getServicesByTag(tag);
        var serviceNameToCatalogServiceMap = this.consulAdminClient.services().getServicesByName(serviceNameToServiceTagMap.keySet());

        List<CapabilityService> capabilityServices = new ArrayList<>();
        for(var catalogServiceName : serviceNameToCatalogServiceMap.keySet()) {
            var catalogService = serviceNameToCatalogServiceMap.get(catalogServiceName).get(0);

            var capabilityId = UUID.fromString(catalogService.getServiceMeta().get(CapabilityService.META_KEY_CAPABILITY_ID));
            var capabilityOptional = capabilityJpaRepository.findById(capabilityId);
            capabilityOptional.ifPresentOrElse((capability) -> {
                var capabilityService = CapabilityService.createFromCatalogService(catalogService, capability);
                capabilityServices.add(capabilityService);
            }, () -> {
                LOG.error("CapabilityService with ServiceID '{}' references non-existing Capability with ID '{}'", capabilityId, capabilityId);
            });
        }

        return capabilityServices;
    }

    public List<CapabilityService> getCapabilityServices() {
        var capabilityServices = this.getCapabilityServicesByTag(CapabilityService.TAG_CAPABILITY);
        return capabilityServices;
    }

    public List<CapabilityService> getCapabilityServicesByCapabilityClass(Class capabilityClass) throws ConsulLoginFailedException {
        var capabilityServices = this.getCapabilityServicesByTag(capabilityClass.getSimpleName());

        return capabilityServices;
    }

    public List<CapabilityService> getCapabilityServicesOfResource(UUID resourceId)  {
        var node = this.consulAdminClient.nodes().getNodeByIdOrThrow(resourceId);

        var capabilityServicesOfResource = new ArrayList<CapabilityService>();
        try {
            var nodesServices = this.consulAdminClient.services().getNodeServicesByNodeId(node.getId());

            var capabilityNodeServices = nodesServices
                    .stream()
                    .filter(s -> Objects.requireNonNull(s.getTags()).contains(CapabilityService.TAG_CAPABILITY))
                    .toList();

            for (var nodeService : capabilityNodeServices) {
                var capabilityId = UUID.fromString(nodeService.getMeta().get(CapabilityService.META_KEY_CAPABILITY_ID));
                var capabilityOptional = capabilityJpaRepository.findById(capabilityId);
                capabilityOptional.ifPresentOrElse((capability) -> {
                    var capabilityService = CapabilityService.createFromNodeService(nodeService, resourceId, capability);
                    capabilityServicesOfResource.add(capabilityService);
                }, () -> {
                    LOG.error("CapabilityService with ServiceID '{}' references non-existing Capability with ID '{}'", capabilityId, capabilityId);
                });


            }
        } catch (ConsulLoginFailedException e) {
            LOG.error("Unable to get deployment capabilities of resource [id='" + resourceId + "'], because login to " +
                    "Consul failed: " + e.getMessage());
            return capabilityServicesOfResource;
        }

        return capabilityServicesOfResource;
    }
}
