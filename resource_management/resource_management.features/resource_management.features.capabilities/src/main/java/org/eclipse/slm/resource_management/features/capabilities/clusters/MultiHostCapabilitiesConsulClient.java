package org.eclipse.slm.resource_management.features.capabilities.clusters;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.*;
import org.eclipse.slm.common.consul.model.acl.BindingRule;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.features.capabilities.CapabilityUtil;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MultiHostCapabilitiesConsulClient {

    private final static Logger LOG = LoggerFactory.getLogger(MultiHostCapabilitiesConsulClient.class);
    private final CapabilityUtil capabilityUtil;
    private final ConsulServicesApiClient consulServicesApiClient;
    private final ConsulGenericServicesClient consulGenericServicesClient;
    private final CapabilityJpaRepository capabilityJpaRepository;
    private final ConsulNodesApiClient consulNodesApiClient;
    private final ConsulAclApiClient consulAclApiClient;
    private final ConsulGenericNodeRemoveClient consulGenericNodeRemoveClient;


    public MultiHostCapabilitiesConsulClient(
            CapabilityJpaRepository capabilityJpaRepository,
            ConsulNodesApiClient consulNodesApiClient,
            ConsulServicesApiClient consulServicesApiClient,
            ConsulGenericServicesClient consulGenericServicesClient,
            ConsulAclApiClient consulAclApiClient,
            ConsulGenericNodeRemoveClient consulGenericNodeRemoveClient,
            CapabilityUtil capabilityUtil
    ) {
        this.capabilityJpaRepository = capabilityJpaRepository;
        this.consulNodesApiClient = consulNodesApiClient;
        this.consulServicesApiClient = consulServicesApiClient;
        this.consulGenericServicesClient = consulGenericServicesClient;
        this.consulAclApiClient = consulAclApiClient;
        this.consulGenericNodeRemoveClient = consulGenericNodeRemoveClient;
        this.capabilityUtil = capabilityUtil;
    }

    //region GET Functions
    public List<MultiHostCapabilityService> getMultiHostCapabilitiesServicesOfUser(
            ConsulCredential consulCredential
    ) throws ConsulLoginFailedException {
        Map<String, List<String>> filteredServices = capabilityUtil.getCapabilityServiceNamesAndTagsMapByTag(
                consulCredential,
                MultiHostCapabilityService.class.getSimpleName()
        );

        List<MultiHostCapabilityService> multiHostCapabilityServices = new ArrayList<>();

        for(String serviceName : filteredServices.keySet()) {

            // get nodes with service matching the serviceName
            Optional<List<CatalogService>> servicesOptional = consulServicesApiClient.getServiceByName(
                    consulCredential,
                    serviceName
            );

            // get MHCS "main"/"dummy" node (representing the cluster)
            Optional<CatalogService> serviceOptional = servicesOptional.get().stream().filter(catalogService -> catalogService.getNode().equals(serviceName)).findFirst();

            if(serviceOptional.isPresent()) {
                multiHostCapabilityServices.add(createMultiHostCapabilityServiceFromConsulService(serviceOptional.get(), consulCredential));
            } else {
                // as a fallback, get first service matching the serviceName
                CatalogService service = servicesOptional.get().get(0);
                multiHostCapabilityServices.add(createMultiHostCapabilityServiceFromConsulService(service, consulCredential));
            }
        }

        return multiHostCapabilityServices;
    }

    public MultiHostCapabilityService createMultiHostCapabilityServiceFromConsulService(CatalogService service, ConsulCredential consulCredential) throws ConsulLoginFailedException {

        // find capability of MHCS
        UUID capabilityId = UUID.fromString(service.getServiceMeta().get(CapabilityService.META_KEY_CAPABILITY_ID));
        Optional<Capability> capabilityOptional = capabilityJpaRepository.findById(capabilityId);

        if(capabilityOptional.isPresent()) {
            UUID serviceId = UUID.fromString(service.getServiceId());
            Map<UUID, String> memberMapping = capabilityUtil.getMemberMappingOfMultiHostCapabilityService(
                    consulCredential,
                    capabilityOptional.get(),
                    serviceId
            );

            // create MHCS object
            MultiHostCapabilityService mhcs = new MultiHostCapabilityService(
                    capabilityOptional.get(),
                    memberMapping,
                    serviceId
            );

            //section enrich MHCS with data from node meta-data and service meta-data (from cluster "dummy" node)
            Map<String, String> serviceMeta = service.getServiceMeta();
            Boolean isManaged;
            CapabilityServiceStatus status;

            try {
                if (serviceMeta.containsKey(CapabilityService.META_KEY_MANAGED)){
                    isManaged = serviceMeta.get(CapabilityService.META_KEY_MANAGED).equalsIgnoreCase("true");
                } else {
                    isManaged = false;
                    LOG.warn("Could not determine property 'managed' of cluster service with id '"+serviceId+"'. Using cluster default value ('"+isManaged+"')");
                }
            } catch (Exception e) {
                isManaged = false;
                LOG.warn("Could not determine property 'managed' of cluster service with id '"+serviceId+"'. Using cluster default value ('"+isManaged+"'). Error: " +e);
            }
            mhcs.setManaged(isManaged);

            try {
                if (serviceMeta.containsKey(CapabilityService.META_KEY_STATUS)){
                    status = CapabilityServiceStatus.valueOf(serviceMeta.get(CapabilityService.META_KEY_STATUS));
                } else {
                    status = CapabilityServiceStatus.UNKNOWN;
                    LOG.warn("Could not determine status of cluster service with id '"+serviceId+"'. Using cluster default value ('"+status+"')");
                }
            } catch (Exception e) {
                status = CapabilityServiceStatus.UNKNOWN;
                LOG.warn("Could not determine status of cluster service with id '"+serviceId+"'. Using cluster default value ('"+status+"'). Error: " +e);
            }
            mhcs.setStatus(status);

            try {
                // load and enrich service meta-data from consul service
                if (serviceMeta.size() > 0) {
                    // add meta data entries from consul service as map to mhcs.customMeta but skip already available entries
                    mhcs.setCustomMeta((Map<String, String>) serviceMeta.entrySet()
                            .stream()
                            .filter(stringStringEntry -> !mhcs.getServiceMeta().containsKey(stringStringEntry.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new)));

                }
            } catch (Exception e) {
                LOG.warn("Could not enrich cluster meta data from consul data for service with id '"+serviceId+"'. Error: " +e);
            }
            //endsection

            return mhcs;
        } else {
            LOG.warn("Capability [id = '"+capabilityId+"'] has not been found in capability DB");
            return null;
        }

    }

    public List<MultiHostCapabilityService> getMultiHostCapabilityServicesOfResource(
            ConsulCredential consulCredential,
            UUID resourceId
    ) throws ConsulLoginFailedException {
        List<NodeService> nodesServices = consulServicesApiClient.getNodeServicesByNodeId(
                new ConsulCredential(),
                resourceId
        );
        List<NodeService> filteredNodeService = new ArrayList<>();
        List<MultiHostCapabilityService> multiHostCapabilityServicesOfResource = new ArrayList<>();

        for(NodeService nodeService : nodesServices) {
            if(isMultiHostCapabilityService(nodeService))
                filteredNodeService.add(nodeService);
        }

        for(NodeService nodeService : filteredNodeService) {
            Optional<MultiHostCapabilityService> mhcs =
                    getMultiHostCapabilityServiceOfUser(consulCredential, UUID.fromString(nodeService.getID()));

            if(mhcs.isPresent())
                multiHostCapabilityServicesOfResource.add(mhcs.get());
        }

        return multiHostCapabilityServicesOfResource;
    }

    public Optional<MultiHostCapabilityService> getMultiHostCapabilityServiceOfUser(
            ConsulCredential consulCredential,
            UUID consulServiceUuid
    ) throws ConsulLoginFailedException {
        List<MultiHostCapabilityService> services = getMultiHostCapabilitiesServicesOfUser(consulCredential);

        return services.stream()
                .filter(service -> service.getId().equals(consulServiceUuid))
                .findFirst();
    }

    public List<CatalogService> getNodesOfMultiHostCapabilityService(
            ConsulCredential consulCredential,
            String serviceName
    ) throws ConsulLoginFailedException {
        Map<String, List<String>> services = this.consulServicesApiClient.getServices(consulCredential);

        List<String> clusterServiceNames = services.entrySet()
                .stream()
                .filter(set -> set.getValue().contains(DeploymentCapability.class.getSimpleName()) )
                .filter(set -> set.getValue().contains(MultiHostCapabilityService.class.getSimpleName()) )
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        var consulServices = this.consulServicesApiClient.getServicesByName(
                consulCredential,
                clusterServiceNames
        );

        return consulServices.get(serviceName);
    }

    public List<Node> getNodesOfMultiHostCapabilityService(
            ConsulCredential consulCredential,
            UUID serviceId
    ) throws ConsulLoginFailedException {
        Optional<MultiHostCapabilityService> optionalService = getMultiHostCapabilityServiceOfUser(
                consulCredential,
                serviceId
        );

        if(optionalService.isPresent())
            return getNodesOfMultiHostCapabilityService(
                    consulCredential,
                    optionalService.get()
            );
        else
            return null;
    }

    private List<Node> getNodesOfMultiHostCapabilityService(
            ConsulCredential consulCredential,
            MultiHostCapabilityService multiHostCapabilityService
    ) {
        var nodes = new ArrayList<Node>();

        multiHostCapabilityService.getMapOfNodeIdsAndCatalogServices()
                .keySet()
                .stream()
                .forEach(nodeId -> {
                    Optional<Node> node = null;
                    try {
                        node = consulNodesApiClient.getNodeById(
                                consulCredential,
                                nodeId
                        );
                    } catch (ConsulLoginFailedException e) {
                        e.printStackTrace();
                    }

                    if(node.isPresent())
                        nodes.add(node.get());
                });

        return nodes;
    }
    //endregion

    //region ADD Functions
    public void addMultiHostCapabilityService(
            ConsulCredential consulCredential,
            MultiHostCapabilityService multiHostCapabilityService
    ) throws ConsulLoginFailedException {
        multiHostCapabilityService.getMemberMapping()
                .keySet()
                .stream()
                .forEach(nodeId -> {
                    try {
                        this.consulGenericServicesClient.registerService(
                                consulCredential,
                                nodeId,
                                multiHostCapabilityService.getService(),
                                multiHostCapabilityService.getId(),
                                Optional.empty(),
                                multiHostCapabilityService.getTagsByNodeId(nodeId),
                                multiHostCapabilityService.getServiceMetaByNodeId(nodeId)
                        );
                    } catch (ConsulLoginFailedException e) {
                        e.printStackTrace();
                    }
                });
    }

    public void updateMultiHostCapabilityService(
            ConsulCredential consulCredential,
            MultiHostCapabilityService newMultiHostCapabilityService
    ) throws ConsulLoginFailedException {
        addMultiHostCapabilityService(consulCredential, newMultiHostCapabilityService);
    }
    //endregion

    //region DELETE Functions
    public void removeMultiHostCapabilityService(
            ConsulCredential consulCredential,
            UUID serviceId
    ) throws ConsulLoginFailedException {
        Optional<MultiHostCapabilityService> service = getMultiHostCapabilityServiceOfUser(consulCredential, serviceId);

        if(service.isPresent())
            removeMultiHostCapabilityService(
                    consulCredential,
                    service.get()
            );
    }

    private void removeMultiHostCapabilityService(
            ConsulCredential consulCredential,
            MultiHostCapabilityService multiHostCapabilityService
    ) {
        try {
            var consulNodeOptional = this.consulNodesApiClient.getNodeById(
                    consulCredential,
                    multiHostCapabilityService.getId()
            );
            if (consulNodeOptional.isPresent()) {

                // delete dummy node first
                var consulNode = consulNodeOptional.get();
//                this.consulNodesApiClient.deleteNode(consulCredential, consulNode.getNode());
                this.consulGenericNodeRemoveClient.removeNode(consulCredential, consulNode.getNode());

                // remove read rule from policy
                this.consulAclApiClient.removeReadRuleFromPolicy(
                        consulCredential,
                        "resource_" + multiHostCapabilityService.getId(),
                        "service",
                        multiHostCapabilityService.getService()
                );

                // remove policy
                var policyName = "resource_" + multiHostCapabilityService.getId();
                var policy = this.consulAclApiClient.getPolicyByName(consulCredential, policyName);
                if (policy != null) {
                    this.consulAclApiClient.deletePolicyById(consulCredential, policy.getId());
                } else {
                    LOG.error("Unable to delete Consul policy for cluster resource with id '" + multiHostCapabilityService.getId()
                            + "', policy with name '" + policyName + "'not found");
                }

                // remove role
                var roleName = "resource_" + multiHostCapabilityService.getId();
                var role = this.consulAclApiClient.getRoleByName(consulCredential, roleName);
                if (role != null) {
                    this.consulAclApiClient.deleteRoleById(consulCredential, role.getId());
                } else {
                    LOG.error("Unable to delete Consul role for cluster resource with id '" + multiHostCapabilityService.getId()
                            + "', role with name '" + roleName + "'not found");
                }

                // remove related binding-rules
                var bindingRules = this.consulAclApiClient.getBindingRules(consulCredential);
                List<BindingRule> bindingRulesOfResource = bindingRules.stream()
                        .filter(r -> r.getBindName().equals("resource_" + consulNode.getId())).collect(Collectors.toList());
                if (bindingRulesOfResource.size() > 0) {
                    for (BindingRule bindingRule : bindingRulesOfResource) {
                        this.consulAclApiClient.deleteBindingRuleById(consulCredential, bindingRule.getId());
                    }
                }
                LOG.info("Cluster resource with id '" + multiHostCapabilityService.getId() + "' successfully deleted in Consul");
            }
        } catch (ConsulLoginFailedException e) {
            LOG.error(e.getMessage());
        }

        // if cluster has nodes, perform node actions here
        multiHostCapabilityService.getMemberMapping().keySet()
                .forEach(k -> {
                    try {
                        // Remove Capability Service:
                        this.consulGenericServicesClient.deregisterService(
                                new ConsulCredential(),
                                k,
                                multiHostCapabilityService.getService()
                        );

                        // Remove Policy to read Service
                        this.consulAclApiClient.removeReadRuleFromPolicy(
                                new ConsulCredential(),
                                "resource_" + k,
                                "service",
                                multiHostCapabilityService.getService()
                        );

                        LOG.info("Successfully removed cluster deployment capability '" + multiHostCapabilityService.getService() + "'" +
                                "from resource '" + k + "'");
                    } catch (ConsulLoginFailedException e) {
                        LOG.warn("Unable to remove " + multiHostCapabilityService.getService() + " from resource '" + k + "'");
                    }
                });
    }
    //endregion

    //region SCALE Functions
    public void scaleMultiHostCapabilityService(
            ConsulCredential consulCredential,
            ScaleOperation scaleOperation,
            UUID serviceId
    ) {
        if (scaleOperation.getClass().equals(ScaleUpOperation.class)) {
            try {
                scaleUpMultiHostCapabilityService(
                        consulCredential,
                        (ScaleUpOperation) scaleOperation,
                        serviceId
                );
            } catch (ConsulLoginFailedException e) {
                e.printStackTrace();
            }

        } else if (scaleOperation.getClass().equals(ScaleDownOperation.class)) {
            try {
                scaleDownMultiHostCapabilityService(
                        consulCredential,
                        (ScaleDownOperation) scaleOperation,
                        serviceId
                );
            } catch (ConsulLoginFailedException e) {
                e.printStackTrace();
            }
        }
    }

    private void scaleDownMultiHostCapabilityService(
            ConsulCredential consulCredential,
            ScaleDownOperation scaleDownOperation,
            UUID serviceId
    ) throws ConsulLoginFailedException {
        Optional<MultiHostCapabilityService> optionalMultiHostCapabilityService = getMultiHostCapabilityServiceOfUser(
                consulCredential,
                serviceId
        );

        if(optionalMultiHostCapabilityService.isEmpty()) {
            LOG.warn("Failed to scale down Consul Service. Could not find multiHostCapabilityService [id = '" + serviceId + "']. ");
            return;
        }

        MultiHostCapabilityService multiHostCapabilityService = optionalMultiHostCapabilityService.get();

        this.consulGenericServicesClient.deregisterService(
                new ConsulCredential(),
                scaleDownOperation.getResourceId(),
                multiHostCapabilityService.getService()
        );
        this.consulAclApiClient.removeReadRuleFromPolicy(
                new ConsulCredential(),
                "resource_" + scaleDownOperation.getResourceId(),
                "service", multiHostCapabilityService.getService()
        );
    }

    private void scaleUpMultiHostCapabilityService(
            ConsulCredential consulCredential,
            ScaleUpOperation scaleUpOperation,
            UUID serviceId
    ) throws ConsulLoginFailedException {
        Optional<MultiHostCapabilityService> optionalMultiHostCapabilityService = getMultiHostCapabilityServiceOfUser(
                consulCredential,
                serviceId
        );

        if(optionalMultiHostCapabilityService.isPresent())
            scaleUpMultiHostCapabilityService(consulCredential, scaleUpOperation, optionalMultiHostCapabilityService.get());
    }

    private void scaleUpMultiHostCapabilityService(
            ConsulCredential consulCredential,
            ScaleUpOperation scaleUpOperation,
            MultiHostCapabilityService multiHostCapabilityService
    ) throws ConsulLoginFailedException {
        UUID nodeId = scaleUpOperation.getResourceId();

        multiHostCapabilityService.applyScaleUp(scaleUpOperation);

        this.consulGenericServicesClient.registerServiceForNodeWithReadAccessViaKeycloakRole(
                consulCredential,
                nodeId,
                multiHostCapabilityService.getService(),
                multiHostCapabilityService.getId(),
                Optional.empty(),
                multiHostCapabilityService.getTagsByNodeId(nodeId),
                multiHostCapabilityService.getServiceMetaByNodeId(nodeId)
        );
    }
    //endregion

    //region ACL Functions
    public void addReadRuleForCapabilityServiceToResourcePolicy(
            ConsulCredential consulCredential,
            MultiHostCapabilityService multiHostCapabilityService
    ) throws ConsulLoginFailedException {
        for (var memberMappingEntry : multiHostCapabilityService.getMemberMapping().entrySet()) {
            // Add access to cluster member service to rule
            this.consulAclApiClient.addReadRuleToPolicy(
                    new ConsulCredential(),
                    "resource_" + memberMappingEntry.getKey().toString(),
                    "service",
                    multiHostCapabilityService.getService()
            );

            this.consulAclApiClient.addReadRuleToPolicy(
                    new ConsulCredential(),
                    "resource_" + memberMappingEntry.getKey().toString(),
                    "key_prefix",
                    String.valueOf(multiHostCapabilityService.getId())
            );
        }
    }
    //endregion

    private boolean isMultiHostCapabilityService(NodeService nodeService) {
        return nodeService.getTags().contains(MultiHostCapabilityService.class.getSimpleName());
    }
}
