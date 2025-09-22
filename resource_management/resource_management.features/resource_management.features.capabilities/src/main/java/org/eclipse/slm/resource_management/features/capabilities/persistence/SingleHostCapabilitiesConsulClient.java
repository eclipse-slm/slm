package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.*;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.CapabilityUtil;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SingleHostCapabilitiesConsulClient {
    private final static Logger LOG = LoggerFactory.getLogger(SingleHostCapabilitiesConsulClient.class);

    private static final String KV_FOLDER_CAPABILITY_SERVICES = "capabilityServices";

    private final ResourcesConsulClient resourcesConsulClient;
    private final ConsulNodesApiClient consulNodesApiClient;
    private final ConsulServicesApiClient consulServicesApiClient;
    private final ConsulGenericServicesClient consulGenericServicesClient;
    private final ConsulAclApiClient consulAclApiClient;
    private final ConsulHealthApiClient consulHealthApiClient;
    private final CapabilityUtil capabilityUtil;
    private final CapabilityJpaRepository capabilityJpaRepository;


    public SingleHostCapabilitiesConsulClient(
            ResourcesConsulClient resourcesConsulClient,
            ConsulNodesApiClient consulNodesApiClient,
            ConsulServicesApiClient consulServicesApiClient,
            ConsulGenericServicesClient consulGenericServicesClient,
            ConsulAclApiClient consulAclApiClient,
            ConsulHealthApiClient consulHealthApiClient,
            CapabilityUtil capabilityUtil,
            CapabilityJpaRepository capabilityJpaRepository
    ) {
        this.resourcesConsulClient = resourcesConsulClient;
        this.consulNodesApiClient = consulNodesApiClient;
        this.consulServicesApiClient = consulServicesApiClient;
        this.consulGenericServicesClient = consulGenericServicesClient;
        this.consulAclApiClient = consulAclApiClient;
        this.consulHealthApiClient = consulHealthApiClient;
        this.capabilityUtil = capabilityUtil;
        this.capabilityJpaRepository = capabilityJpaRepository;
    }

    //region ADD Function
    public void addSingleHostCapabilityWithHealthCheckToAllConsulNodes(
            ConsulCredential consulCredential,
            Capability capability,
            Boolean isManaged,
            Map<String, String> configParameter
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
        if (capability.getHealthCheck() != null) {
            var existingResources = this.resourcesConsulClient.getResources(consulCredential);
            for (var existingResource : existingResources) {
                List<NodeService> nodeServices = consulServicesApiClient.getNodeServicesByNodeId(new ConsulCredential(), existingResource.getId());
                Optional<NodeService> consulService = nodeServices.stream().filter(srv -> srv.getService().equals("consul")).findFirst();

                if(consulService.isEmpty())
                    this.addSingleHostCapabilityToNode(
                        consulCredential,
                        capability,
                        existingResource.getId(),
                        CapabilityServiceStatus.INSTALL,
                        isManaged,
                        configParameter
                    );
            }
        }
        else {
            LOG.error("Capability " + capability + "has no health check defined");
        }
    }

    public CapabilityService addSingleHostCapabilityToNode(
            ConsulCredential consulCredential,
            Capability capability,
            UUID nodeId,
            CapabilityServiceStatus capabilityServiceStatus
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
        return addSingleHostCapabilityToNode(
                consulCredential,
                capability,
                nodeId,
                capabilityServiceStatus,
                false,
                new HashMap<>()
        );
    }

    public CapabilityService addSingleHostCapabilityToNode(
            ConsulCredential consulCredential,
            Capability capability,
            UUID resourceId,
            CapabilityServiceStatus capabilityServiceStatus,
            Boolean isManaged,
            Map<String, String> configParameter
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {

        var singleHostCapabilityService = new SingleHostCapabilityService(
                capability,
                resourceId,
                capabilityServiceStatus,
                isManaged,
                capabilityUtil.getNonSecretConfigParameter(capability,configParameter)
        );
        this.consulGenericServicesClient.registerService(
                consulCredential,
                singleHostCapabilityService.getConsulNodeId(),
                singleHostCapabilityService.getService(),
                singleHostCapabilityService.getId(),
                capabilityUtil.getServicePortFromConfigParameter(capability, configParameter),
                singleHostCapabilityService.getTags(),
                singleHostCapabilityService.getServiceMeta()
        );

        var resourcePolicyName = ResourcesConsulClient.getResourcePolicyName(resourceId);
        this.consulAclApiClient.addReadRuleToPolicy(
                consulCredential,
                resourcePolicyName,
                "service",
                singleHostCapabilityService.getService()
        );
        this.consulAclApiClient.addReadRuleToPolicy(
                consulCredential,
                resourcePolicyName,
                "key_prefix",
                KV_FOLDER_CAPABILITY_SERVICES + singleHostCapabilityService.getId()
        );

        var capabilityService = getCapabilityServiceOfResourceByCapabilityId(
                capability.getId(),
                resourceId
        );

        CapabilityHealthCheck healthCheck = singleHostCapabilityService.getCapability().getHealthCheck();
        if(healthCheck != null) {
            Optional<Node> optionalNode = this.consulNodesApiClient.getNodeById(
                    consulCredential,
                    resourceId
            );

            if(optionalNode.isEmpty())
                throw new ResourceNotFoundException(resourceId);

            this.consulHealthApiClient.addCheckForService(
                    consulCredential,
                    optionalNode.get().getNode(),
                    capabilityService.getId(),
                    capabilityUtil.getCheckByCapability(consulCredential, resourceId, capability)
            );
        }

        return capabilityService;
    }
    //endregion

    //region UPDATE Function
    public void updateCapabilityService(
            ConsulCredential consulCredential,
            UUID nodeId,
            CapabilityService capabilityService
    ) throws ConsulLoginFailedException {
        this.consulGenericServicesClient.registerService(
                consulCredential,
                nodeId,
                capabilityService.getService(),
                capabilityService.getId(),
                Optional.ofNullable(capabilityService.getPort()),
                capabilityService.getTags(),
                capabilityService.getServiceMeta()
        );
    }
    //endregion

    //region DELETE Function
    public void removeCapabilityServiceFromAllConsulNodes(
            ConsulCredential consulCredential,
            Capability capability
    ) throws ConsulLoginFailedException {
        var existingResources = this.resourcesConsulClient.getResources(consulCredential);
        for (var existingResource : existingResources) {
            try {
                this.removeSingleHostCapabilityFromNode(consulCredential, capability, existingResource.getId());
            } catch (ResourceNotFoundException e) {
                LOG.warn("Unable to find resource [id = '"+existingResource.getId()+"'] => Skip removal of capability "
                        + "[id = '"+capability.getId()+"']"
                );
            }
        }
    }

    public void removeSingleHostCapabilityFromNode(
            ConsulCredential consulCredential,
            Capability capability,
            UUID resourceId
    ) throws ConsulLoginFailedException, ResourceNotFoundException {
        var nodeServices = this.consulServicesApiClient.getNodeServicesByNodeId(consulCredential, resourceId);

        Optional<NodeService> capabilityService = nodeServices.stream()
                .filter(ns -> ns.getMeta().containsKey("capabilityId"))
                .filter(ns -> ns.getMeta().get("capabilityId").equals(capability.getId().toString() ))
                .findFirst();

        if(capabilityService.isEmpty()) {
            return;
        }

        this.consulGenericServicesClient.deregisterService(
                new ConsulCredential(),
                resourceId,
                capabilityService.get().getService()
        );
        var resourcePolicyName = ResourcesConsulClient.getResourcePolicyName(resourceId);
        this.consulAclApiClient.removeReadRuleFromPolicy(
                consulCredential,
                resourcePolicyName,
                "service",
                capabilityService.get().getService()
        );
        this.consulAclApiClient.removeReadRuleFromPolicy(
                consulCredential,
                resourcePolicyName,
                "key_prefix",
                KV_FOLDER_CAPABILITY_SERVICES + capabilityService.get().getID()
        );


        var nodeChecks = this.consulHealthApiClient.getChecksOfNode(consulCredential, resourceId);
        for(var nodeCheck : nodeChecks) {
            if ( nodeCheck.getName().equals("capability_" + capability.getName())) {
                this.consulHealthApiClient.removeCheckFromNode(consulCredential, resourceId, nodeCheck.getCheckId());
            }
        }
    }
    //endregion

    //region GET Function
    public List<SingleHostCapabilityService> getSingleHostCapabilityServicesOfResource(
            ConsulCredential consulCredential,
            UUID consulNodeId
    ) throws ConsulLoginFailedException {
        List<SingleHostCapabilityService> singleHostCapabilityServices = new ArrayList<>();
        Optional<Node> optionalNode = consulNodesApiClient.getNodeById(consulCredential, consulNodeId);

        if(optionalNode.isEmpty()) {
            LOG.error("No consul node found with id = '" + consulNodeId + "'");
            return null;
        }

        Node node = optionalNode.get();

        var servicesOfNode = this.consulServicesApiClient.getNodeServices(consulCredential, node.getNode());

        for (var serviceOfNode : servicesOfNode) {
            if (serviceOfNode.getTags().contains(SingleHostCapabilityService.class.getSimpleName())) {
                Optional<Capability> capabilityOptional = capabilityJpaRepository.findById(
                        UUID.fromString(serviceOfNode.getMeta().get("capabilityId"))
                );

                capabilityOptional.ifPresent(capability -> singleHostCapabilityServices.add(new SingleHostCapabilityService(
                        capability,
                        consulNodeId,
                        UUID.fromString(serviceOfNode.getID()),
                        capabilityUtil.getStatusOfConsulService(serviceOfNode),
                        capabilityUtil.getIsManagedOfConsulService(serviceOfNode)
                )));
            }
        }

        return singleHostCapabilityServices;
    }

    public CapabilityService getCapabilityServiceForCapabilityOfResource(
            ConsulCredential consulCredential,
            Capability capability,
            UUID consulNodeId
    ) throws ConsulLoginFailedException, IllegalAccessException {

        Optional<Node> optionalNode = consulNodesApiClient.getNodeById(consulCredential, consulNodeId);

        if(optionalNode.isEmpty()) {
            LOG.error("No consul node found with id = '" + consulNodeId + "'");
            return null;
        }

        Node node = optionalNode.get();

        List<NodeService> servicesOfNode = this.consulServicesApiClient.getNodeServices(consulCredential, node.getNode());
        for (var serviceOfNode : servicesOfNode) {
            if (serviceOfNode.getTags().contains(SingleHostCapabilityService.class.getSimpleName())) {
                return new SingleHostCapabilityService(
                        capability,
                        consulNodeId,
                        UUID.fromString(serviceOfNode.getID()),
                        serviceOfNode.getPort(),
                        capabilityUtil.getStatusOfConsulService(serviceOfNode),
                        capabilityUtil.getIsManagedOfConsulService(serviceOfNode),
                        capabilityUtil.getCustomMeta(serviceOfNode)
                );
            }
        }

        return null;
    }

    public CapabilityService getCapabilityServiceOfResourceByCapabilityId(
            UUID capabilityId,
            UUID consulNodeId
    ) throws ConsulLoginFailedException, IllegalAccessException {
        var consulCredential = new ConsulCredential();

        Optional<Capability> optionalCapability = capabilityJpaRepository.findById(capabilityId);
        Optional<Node> optionalNode = consulNodesApiClient.getNodeById(consulCredential, consulNodeId);

        if(optionalCapability.isEmpty()) {
            LOG.error("No capability found with id = '" + capabilityId + "'");
            return null;
        }

        if(optionalNode.isEmpty()) {
            LOG.error("No consul node found with id = '" + consulNodeId + "'");
            return null;
        }

        Capability capability = optionalCapability.get();
        Node node = optionalNode.get();

        var servicesOfNode = this.consulServicesApiClient.getNodeServices(consulCredential, node.getNode());

        Optional<NodeService> optionalService = servicesOfNode
                .stream()
                .filter(service -> service.getMeta().containsKey(CapabilityService.META_KEY_CAPABILITY_ID))
                .filter(nodeService ->
                        nodeService
                                .getMeta().get(CapabilityService.META_KEY_CAPABILITY_ID)
                                .equals(capabilityId.toString())
                ).findFirst();

        if(optionalService.isEmpty()) {
            LOG.error("Node with id = '" + consulNodeId + "' has no service with capabilitId = '" + capabilityId + "'");
            return null;
        }

        NodeService serviceOfNode = optionalService.get();

        return new SingleHostCapabilityService(
                capability,
                consulNodeId,
                UUID.fromString(serviceOfNode.getID()),
                serviceOfNode.getPort(),
                capabilityUtil.getStatusOfConsulService(serviceOfNode),
                capabilityUtil.getIsManagedOfConsulService(serviceOfNode),
                capabilityUtil.getCustomMeta(serviceOfNode)
        );
    }
    //endregion
}
