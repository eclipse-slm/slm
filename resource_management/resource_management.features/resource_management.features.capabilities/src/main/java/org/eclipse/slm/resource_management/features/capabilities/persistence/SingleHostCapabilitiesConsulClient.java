package org.eclipse.slm.resource_management.features.capabilities.persistence;


import org.eclipse.slm.common.consul.client.*;
import org.eclipse.slm.common.consul.model.acl.policies.Policy;
import org.eclipse.slm.common.consul.model.catalog.CatalogRegistration;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClientFactory;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.CapabilityUtil;
import org.eclipse.slm.resource_management.features.capabilities.exceptions.CapabilityNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.exceptions.CapabilityServiceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.exceptions.CapabilityServiceRuntimeException;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SingleHostCapabilitiesConsulClient {
    private final static Logger LOG = LoggerFactory.getLogger(SingleHostCapabilitiesConsulClient.class);

    private final ResourcesConsulClientFactory resourcesConsulClientFactory;
    private final ResourcesConsulClient resourcesConsulAdminClient;

    private final ConsulClientFactory consulClientFactory;
    private final ConsulClient adminConsulClient;

    private final CapabilityJpaRepository capabilityJpaRepository;

    public SingleHostCapabilitiesConsulClient(
            ConsulClientFactory consulClientFactory,
            ResourcesConsulClientFactory resourcesConsulClientFactory,
            CapabilityJpaRepository capabilityJpaRepository
    ) {
        this.resourcesConsulClientFactory = resourcesConsulClientFactory;
        this.resourcesConsulAdminClient = resourcesConsulClientFactory.createAdminClient();
        this.consulClientFactory = consulClientFactory;
        this.adminConsulClient = consulClientFactory.createAdminClient();
        this.capabilityJpaRepository = capabilityJpaRepository;
    }

    public CapabilityService addSingleHostCapabilityToNode(
            Capability capability,
            UUID resourceId,
            CapabilityServiceStatus capabilityServiceStatus,
            Boolean isManaged,
            Map<String, String> configParameter,
            String fullPathOwnerGroupId
    ) throws ResourceNotFoundException {
        // Register capability service on Consul node of resource
        var capabilityServiceId = UUID.randomUUID();
        var singleHostCapabilityService = new SingleHostCapabilityService(
                resourceId,
                capabilityServiceId,
                capability,
                capabilityServiceStatus,
                isManaged,
                CapabilityUtil.getNonSecretConfigParameter(capability,configParameter)
        );
        var serviceRegistrationBuilder = CatalogRegistration.Service.builder(singleHostCapabilityService.getServiceName())
                .id(singleHostCapabilityService.getId())
                .tags(singleHostCapabilityService.getTags())
                .meta(singleHostCapabilityService.getMeta());
        var optionalPort = CapabilityUtil.getServicePortFromConfigParameter(capability, configParameter);
        optionalPort.ifPresent(serviceRegistrationBuilder::port);
        var serviceRegistration = serviceRegistrationBuilder.build();

        this.adminConsulClient.services().registerService(singleHostCapabilityService.getResourceId(), serviceRegistration);
        // Create read access policy and assign it to role of user group of owner
        var policyName = CapabilitiesConsulClient.getCapabilityServicePolicyName(capabilityServiceId);
        var policyRule =  "service \"" + singleHostCapabilityService.getServiceName() + "\" { policy = \"read\" }";
        var policy = Policy.builder(policyName)
                .rules(policyRule)
                .build();
        var createdPolicy = this.adminConsulClient.acl().createPolicy(policy);
        this.adminConsulClient.acl().addPolicyToRole(fullPathOwnerGroupId, createdPolicy.getId());
        // Get and return created capability service
        var capabilityService = getCapabilityServiceOfResourceByCapabilityId(capability.getId(), resourceId);
        return capabilityService;
    }

    public void updateCapabilityService(UUID nodeId, CapabilityService capabilityService) {
        var serviceRegistration = CatalogRegistration.Service.builder(capabilityService.getServiceName())
                .id(capabilityService.getId())
                .port(capabilityService.getPort())
                .tags(capabilityService.getTags())
                .meta(capabilityService.getMeta())
                .build();

        this.adminConsulClient.services().registerService(nodeId, serviceRegistration);
    }

    public void removeSingleHostCapabilityFromNode(Capability capability, UUID resourceId) throws ResourceNotFoundException {
        // Get capability service on Consul node of resource
        var nodeServices = this.adminConsulClient.services().getNodeServicesByNodeId(resourceId);
        Optional<NodeService> capabilityNodeService = nodeServices.stream()
                .filter(ns -> ns.getMeta().containsKey("capabilityId"))
                .filter(ns -> ns.getMeta().get("capabilityId").equals(capability.getId().toString() ))
                .findFirst();

        if(capabilityNodeService.isEmpty()) {
            return; // No capability service for the capability found on this node, nothing to remove
        }
        // Unregister capability service from Consul node
        var capabilityService = SingleHostCapabilityService.createFromNodeService(capabilityNodeService.get(), resourceId, capability);
        this.adminConsulClient.services().removeServiceByName(resourceId, capabilityService.getServiceName());
        // Remove capability service policy
        var policyName = CapabilitiesConsulClient.getCapabilityServicePolicyName(capabilityService.getId());
        var policy = this.adminConsulClient.acl().getPolicyByNameOrThrow(policyName);
        this.adminConsulClient.acl().deletePolicyById(policy.getId());
    }

    public void removeCapabilityServiceFromAllConsulNodes(Capability capability) {
        var existingResources = this.resourcesConsulAdminClient.getResources();
        for (var existingResource : existingResources) {
            try {
                this.removeSingleHostCapabilityFromNode(capability, existingResource.getId());
            } catch (Exception e) {
                throw new CapabilityServiceRuntimeException("Unable to remove capability service from resource with id = '" + existingResource.getId() + "'", e);
            }
        }
    }

    public List<SingleHostCapabilityService> getSingleHostCapabilityServicesOfResource(UUID resourceId) {
        var node = adminConsulClient.nodes().getNodeByIdOrThrow(resourceId);

        List<SingleHostCapabilityService> singleHostCapabilityServices = new ArrayList<>();
        var servicesOfNode = this.adminConsulClient.services().getNodeServices(node.getNodeName());
        for (var serviceOfNode : servicesOfNode) {
            if (serviceOfNode.getTags().contains(SingleHostCapabilityService.class.getSimpleName())) {
                var capabilityOptional = capabilityJpaRepository.findById(UUID.fromString(serviceOfNode.getMeta().get("capabilityId")));
                capabilityOptional.ifPresent(capability -> singleHostCapabilityServices.add(
                        SingleHostCapabilityService.createFromNodeService(serviceOfNode, resourceId, capability)
                ));
            }
        }

        return singleHostCapabilityServices;
    }

    public CapabilityService getCapabilityServiceForCapabilityOfResource(Capability capability, UUID resourceId) {
        var node = adminConsulClient.nodes().getNodeByIdOrThrow(resourceId);

        List<NodeService> servicesOfNode = this.adminConsulClient.services().getNodeServices(node.getNodeName());
        for (var serviceOfNode : servicesOfNode) {
            if (serviceOfNode.getTags().contains(SingleHostCapabilityService.class.getSimpleName())) {
                return SingleHostCapabilityService.createFromNodeService(serviceOfNode, resourceId, capability);
            }
        }

        throw new CapabilityServiceRuntimeException("No capability service found for capability[id='" + capability.getId() + "'] on resource[id='"
                + resourceId + "']");
    }

    public CapabilityService getCapabilityServiceOfResourceByCapabilityId(UUID capabilityId, UUID resourceId) {
        var node = adminConsulClient.nodes().getNodeByIdOrThrow(resourceId);
        var optionalCapability = capabilityJpaRepository.findById(capabilityId);
        if(optionalCapability.isEmpty()) {
            throw new CapabilityNotFoundException(capabilityId);
        }
        var capability = optionalCapability.get();

        var servicesOfNode = this.adminConsulClient.services().getNodeServices(node.getNodeName());
        Optional<NodeService> optionalNodeService = servicesOfNode
                .stream()
                .filter(service -> service.getMeta().containsKey(CapabilityService.META_KEY_CAPABILITY_ID))
                .filter(nodeService ->
                        nodeService
                                .getMeta().get(CapabilityService.META_KEY_CAPABILITY_ID)
                                .equals(capabilityId.toString())
                ).findFirst();

        if(optionalNodeService.isEmpty()) {
            throw new CapabilityServiceNotFoundException("Resource[id='" + resourceId + "'] has no capability service [id='" + capabilityId + "']");
        }
        return SingleHostCapabilityService.createFromNodeService(optionalNodeService.get(), resourceId, capability);
    }
}
