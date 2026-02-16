package org.eclipse.slm.resource_management.features.capabilities.clusters;


import org.eclipse.slm.common.consul.client.*;
import org.eclipse.slm.common.consul.model.acl.bindingrules.BindingRule;
import org.eclipse.slm.common.consul.model.catalog.CatalogRegistration;
import org.eclipse.slm.common.consul.model.catalog.Service;
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

    private final ConsulClientFactory consulClientFactory;
    private final ConsulClient consulAdminClient;

    private final CapabilityJpaRepository capabilityJpaRepository;

    public MultiHostCapabilitiesConsulClient(
            ConsulClientFactory consulClientFactory,
            CapabilityJpaRepository capabilityJpaRepository
    ) {
        this.consulClientFactory = consulClientFactory;
        this.consulAdminClient = consulClientFactory.createAdminClient();
        this.capabilityJpaRepository = capabilityJpaRepository;
    }

    //region GET Functions
    public List<MultiHostCapabilityService> getMultiHostCapabilitiesServicesOfUser() throws ConsulLoginFailedException {
//        Map<String, List<String>> filteredServices = capabilityUtil.getCapabilityServiceNamesAndTagsMapByTag(
//
//                MultiHostCapabilityService.class.getSimpleName()
//        );

        Map<String, List<String>> filteredServices = new HashMap<>();

        List<MultiHostCapabilityService> multiHostCapabilityServices = new ArrayList<>();

        for(String serviceName : filteredServices.keySet()) {

            // get nodes with service matching the serviceName
            Optional<List<Service>> servicesOptional = this.consulAdminClient.services().getServiceByName(
                    
                    serviceName
            );

            // get MHCS "main"/"dummy" node (representing the cluster)
            Optional<Service> serviceOptional = servicesOptional.get().stream().filter(catalogService -> catalogService.getNodeName().equals(serviceName)).findFirst();

            if(serviceOptional.isPresent()) {
                multiHostCapabilityServices.add(createMultiHostCapabilityServiceFromConsulService(serviceOptional.get()));
            } else {
                // as a fallback, get first service matching the serviceName
                Service service = servicesOptional.get().get(0);
                multiHostCapabilityServices.add(createMultiHostCapabilityServiceFromConsulService(service));
            }
        }

        return multiHostCapabilityServices;
    }

    public MultiHostCapabilityService createMultiHostCapabilityServiceFromConsulService(Service service) throws ConsulLoginFailedException {

        // find capability of MHCS
        UUID capabilityId = UUID.fromString(service.getServiceMeta().get(CapabilityService.META_KEY_CAPABILITY_ID));
        Optional<Capability> capabilityOptional = capabilityJpaRepository.findById(capabilityId);

        if(capabilityOptional.isPresent()) {
            var serviceId = service.getServiceId();
//            Map<UUID, String> memberMapping = capabilityUtil.getMemberMappingOfMultiHostCapabilityService(
//                    capabilityOptional.get(),
//                    serviceId
//            );

            // create MHCS object
//            var mhcs = new MultiHostCapabilityService(capabilityOptional.get(), memberMapping, serviceId, "");
            MultiHostCapabilityService mhcs = null;
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
                            .filter(stringStringEntry -> !mhcs.getMeta().containsKey(stringStringEntry.getKey()))
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
            
            UUID resourceId
    ) throws ConsulLoginFailedException {
        List<NodeService> nodesServices = this.consulAdminClient.services().getNodeServicesByNodeId(
                
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
                    getMultiHostCapabilityServiceOfUser(nodeService.getId());

            if(mhcs.isPresent())
                multiHostCapabilityServicesOfResource.add(mhcs.get());
        }

        return multiHostCapabilityServicesOfResource;
    }

    public Optional<MultiHostCapabilityService> getMultiHostCapabilityServiceOfUser(
            
            UUID consulServiceUuid
    ) throws ConsulLoginFailedException {
        List<MultiHostCapabilityService> services = getMultiHostCapabilitiesServicesOfUser();

        return services.stream()
                .filter(service -> service.getId().equals(consulServiceUuid))
                .findFirst();
    }

    public List<Service> getNodesOfMultiHostCapabilityService(
            
            String serviceName
    ) {
        Map<String, List<String>> services = this.consulAdminClient.services().getServices();

        List<String> clusterServiceNames = services.entrySet()
                .stream()
                .filter(set -> set.getValue().contains(DeploymentCapability.class.getSimpleName()) )
                .filter(set -> set.getValue().contains(MultiHostCapabilityService.class.getSimpleName()) )
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        var consulServices = this.consulAdminClient.services().getServicesByName(
                
                clusterServiceNames
        );

        return consulServices.get(serviceName);
    }

    public List<Node> getNodesOfMultiHostCapabilityService(
            
            UUID serviceId
    ) throws ConsulLoginFailedException {
        Optional<MultiHostCapabilityService> optionalService = getMultiHostCapabilityServiceOfUser(
                
                serviceId
        );

        if(optionalService.isPresent())
            return getNodesOfMultiHostCapabilityService(
                    
                    optionalService.get()
            );
        else
            return null;
    }

    private List<Node> getNodesOfMultiHostCapabilityService(
            
            MultiHostCapabilityService multiHostCapabilityService
    ) {
        var nodes = new ArrayList<Node>();

        multiHostCapabilityService.getMapOfNodeIdsAndCatalogServices()
                .keySet()
                .stream()
                .forEach(nodeId -> {
                    Optional<Node> node = null;
                    try {
                        node = this.consulAdminClient.nodes().getNodeById(nodeId);
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
            
            MultiHostCapabilityService multiHostCapabilityService
    ) {
        multiHostCapabilityService.getMemberMapping()
                .keySet()
                .stream()
                .forEach(nodeId -> {
                    var serviceRegistration = CatalogRegistration.Service.builder(multiHostCapabilityService.getServiceName())
                            .id(multiHostCapabilityService.getId())
                            .port(null)
                            .tags(multiHostCapabilityService.getTagsByNodeId(nodeId))
                            .meta(multiHostCapabilityService.getServiceMetaByNodeId(nodeId))
                            .build();

                    this.consulAdminClient.services().registerService(nodeId, serviceRegistration);
                });
    }

    public void updateMultiHostCapabilityService(MultiHostCapabilityService newMultiHostCapabilityService) {
        this.addMultiHostCapabilityService(newMultiHostCapabilityService);
    }
    //endregion

    //region DELETE Functions
    public void removeMultiHostCapabilityService(
            
            UUID serviceId
    ) throws ConsulLoginFailedException {
        Optional<MultiHostCapabilityService> service = getMultiHostCapabilityServiceOfUser(serviceId);

        if(service.isPresent())
            removeMultiHostCapabilityService(
                    
                    service.get()
            );
    }

    private void removeMultiHostCapabilityService(
            
            MultiHostCapabilityService multiHostCapabilityService
    ) {
        try {
            var consulNodeOptional = this.consulAdminClient.nodes().getNodeById(
                    
                    multiHostCapabilityService.getId()
            );
            if (consulNodeOptional.isPresent()) {

                // delete dummy node first
                var consulNode = consulNodeOptional.get();
//                this.consulNodesApiClient.deleteNode(consulNode.getNode());
                this.consulAdminClient.nodes().deleteNodeById(consulNode.getId());

                // remove read rule from policy
                this.consulAdminClient.acl().removeReadRuleFromPolicy(
                        "resource_" + multiHostCapabilityService.getId(),
                        "service",
                        multiHostCapabilityService.getServiceName()
                );

                // remove policy
                var policyName = "resource_" + multiHostCapabilityService.getId();
                var policy = this.consulAdminClient.acl().getPolicyByNameOrThrow(policyName);
                if (policy != null) {
                    this.consulAdminClient.acl().deletePolicyById(policy.getId());
                } else {
                    LOG.error("Unable to delete Consul policy for cluster resource with id '" + multiHostCapabilityService.getId()
                            + "', policy with name '" + policyName + "'not found");
                }

                // remove role
                var roleName = "resource_" + multiHostCapabilityService.getId();
                var role = this.consulAdminClient.acl().getRoleByName(roleName);
                if (role != null) {
                    this.consulAdminClient.acl().deleteRoleById(role.getId());
                } else {
                    LOG.error("Unable to delete Consul role for cluster resource with id '" + multiHostCapabilityService.getId()
                            + "', role with name '" + roleName + "'not found");
                }

                // remove related binding-rules
                var bindingRules = this.consulAdminClient.acl().getBindingRules();
                List<BindingRule> bindingRulesOfResource = bindingRules.stream()
                        .filter(r -> r.getBindName().equals("resource_" + consulNode.getId())).collect(Collectors.toList());
                if (bindingRulesOfResource.size() > 0) {
                    for (BindingRule bindingRule : bindingRulesOfResource) {
                        this.consulAdminClient.acl().deleteBindingRuleById(bindingRule.getId());
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
                        // Remove Capability Service:
                        this.consulAdminClient.services().removeServiceByName(k, multiHostCapabilityService.getServiceName());

                        // Remove Policy to read Service
//                        this.consulAclApiClient.removeReadRuleFromPolicy(
//                                
//                                "resource_" + k,
//                                "service",
//                                multiHostCapabilityService.getService()
//                        );

                        LOG.info("Successfully removed cluster deployment capability '" + multiHostCapabilityService.getServiceName() + "'" +
                                "from resource '" + k + "'");
                });
    }
    //endregion

    //region SCALE Functions
    public void scaleMultiHostCapabilityService(
            
            ScaleOperation scaleOperation,
            UUID serviceId
    ) {
        if (scaleOperation.getClass().equals(ScaleUpOperation.class)) {
            try {
                scaleUpMultiHostCapabilityService(
                        
                        (ScaleUpOperation) scaleOperation,
                        serviceId
                );
            } catch (ConsulLoginFailedException e) {
                e.printStackTrace();
            }

        } else if (scaleOperation.getClass().equals(ScaleDownOperation.class)) {
            try {
                scaleDownMultiHostCapabilityService(
                        
                        (ScaleDownOperation) scaleOperation,
                        serviceId
                );
            } catch (ConsulLoginFailedException e) {
                e.printStackTrace();
            }
        }
    }

    private void scaleDownMultiHostCapabilityService(
            
            ScaleDownOperation scaleDownOperation,
            UUID serviceId
    ) throws ConsulLoginFailedException {
        Optional<MultiHostCapabilityService> optionalMultiHostCapabilityService = getMultiHostCapabilityServiceOfUser(
                
                serviceId
        );

        if(optionalMultiHostCapabilityService.isEmpty()) {
            LOG.warn("Failed to scale down Consul Service. Could not find multiHostCapabilityService [id = '" + serviceId + "']. ");
            return;
        }

        MultiHostCapabilityService multiHostCapabilityService = optionalMultiHostCapabilityService.get();

        this.consulAdminClient.services().removeServiceByName(scaleDownOperation.getResourceId(), multiHostCapabilityService.getServiceName());
//        this.consulAclApiClient.removeReadRuleFromPolicy(
//                
//                "resource_" + scaleDownOperation.getResourceId(),
//                "service", multiHostCapabilityService.getService()
//        );
    }

    private void scaleUpMultiHostCapabilityService(
            
            ScaleUpOperation scaleUpOperation,
            UUID serviceId
    ) throws ConsulLoginFailedException {
        Optional<MultiHostCapabilityService> optionalMultiHostCapabilityService = getMultiHostCapabilityServiceOfUser(
                
                serviceId
        );

        if(optionalMultiHostCapabilityService.isPresent())
            scaleUpMultiHostCapabilityService(scaleUpOperation, optionalMultiHostCapabilityService.get());
    }

    private void scaleUpMultiHostCapabilityService(
            
            ScaleUpOperation scaleUpOperation,
            MultiHostCapabilityService multiHostCapabilityService
    ) throws ConsulLoginFailedException {
        UUID nodeId = scaleUpOperation.getResourceId();

        multiHostCapabilityService.applyScaleUp(scaleUpOperation);

//        this.consulAdminClient.services().registerServiceForNodeWithReadAccessViaKeycloakRole(
//                nodeId,
//                multiHostCapabilityService.getService(),
//                multiHostCapabilityService.getId(),
//                Optional.empty(),
//                multiHostCapabilityService.getTagsByNodeId(nodeId),
//                multiHostCapabilityService.getServiceMetaByNodeId(nodeId)
//        );
    }
    //endregion

    //region ACL Functions
    public void addReadRuleForCapabilityServiceToResourcePolicy(MultiHostCapabilityService multiHostCapabilityService) {
        for (var memberMappingEntry : multiHostCapabilityService.getMemberMapping().entrySet()) {
            // Add access to cluster member service to rule
            this.consulAdminClient.acl().addReadRuleToPolicy(
                    
                    "resource_" + memberMappingEntry.getKey().toString(),
                    "service",
                    multiHostCapabilityService.getServiceName()
            );

            this.consulAdminClient.acl().addReadRuleToPolicy(
                    
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
