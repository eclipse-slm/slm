package org.eclipse.slm.resource_management.common.adapters;


import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.client.ConsulServicesClient;
import org.eclipse.slm.common.consul.model.acl.policies.Policy;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulNodeNotFoundException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceRuntimeException;
import org.eclipse.slm.resource_management.common.location.Location;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.resources.BasicResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;

public class ResourcesConsulClient {

    private final static Logger LOG = LoggerFactory.getLogger(ResourcesConsulClient.class);

    public static final String POLICY_RESOURCE_PREFIX = "resource_";
    public static final String KEYCLOAK_ROLE_RESOURCE_PREFIX = "resource_";

    private final ConsulClient consulClient;

    public static String getResourcePolicyName(UUID resourceId) {
        return POLICY_RESOURCE_PREFIX + resourceId;
    }

    public static String getResourceKeycloakRoleName(UUID resourceId) {
        return KEYCLOAK_ROLE_RESOURCE_PREFIX + resourceId;
    }

    public ResourcesConsulClient(ConsulClient consulClient) {
        this.consulClient = consulClient;
    }

    public List<BasicResource> getResources() {
        List<BasicResource> resources = new ArrayList<>();

        try {
            var consulNodes = this.consulClient.nodes().getNodes();
            for (var node : consulNodes) {
                // Check if node is a resource node by checking for resourceId in meta data, else skip node
                if (node.getMeta() != null && node.getMeta().containsKey("resourceId")) {
                    Optional<NodeService> optionalConsulService = this.consulClient.services()
                            .getNodeServices(node.getNodeName())
                            .stream()
                            .filter(nodeService -> nodeService.getServiceName().equals(ConsulServicesClient.CONSUL_SERVICE_NAME))
                            .findFirst();

                    if (node.getMeta() != null && optionalConsulService.isEmpty() && !node.getNodeName().equals("SLM")) {
                        var basicResource = ResourceConsulNode.convertConsulNodeToBasicResource(node);
                        resources.add(basicResource);
                    }
                }
            }
        }
        catch (Exception e) {
            LOG.error("Error while retrieving resources from Consul: {}", e.getMessage(), e);
            return resources;
        }

        return resources;
    }

    public Optional<BasicResource> getResourceById(UUID resourceId) throws ResourceRuntimeException {
        try {
            return Optional.of(this.getResourceByIdOrThrow(resourceId));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }

    public BasicResource getResourceByIdOrThrow(UUID resourceId) throws ResourceNotFoundException, ResourceRuntimeException {
        try {
            var optionalNode = consulClient.nodes().getNodeById(resourceId);
            if(optionalNode.isEmpty()) {
                throw new ResourceNotFoundException(resourceId);
            }

            return ResourceConsulNode.convertConsulNodeToBasicResource(optionalNode.get());
        } catch (ConsulLoginFailedException e) {
            throw new ResourceRuntimeException("Consul login failed: " + e.getMessage(), e);
        }
    }

    public Optional<BasicResource> getResourceByHostname(String hostname) throws ConsulLoginFailedException {
        Predicate<Node> p = (n) -> n.getNodeName().equals(hostname);

        return getResourceByX(p);
    }

    private Optional<BasicResource> getResourceByX(Predicate predicate) throws ConsulLoginFailedException {
        Optional<Node> optionalNode = consulClient.nodes().getNodes().stream().filter(predicate).findFirst();

        if(optionalNode.isEmpty())
            return Optional.empty();

        return Optional.of(ResourceConsulNode.convertConsulNodeToBasicResource(optionalNode.get()));
    }

    //region ADD/DELETE
    public BasicResource addResource(BasicResource resource, String fullPathOwnerGroupId) throws ConsulLoginFailedException {
        try {
            // Add new resource as node in Consul
            var resourceConsulNode = new ResourceConsulNode(resource);
            this.consulClient.nodes().registerNode(resourceConsulNode);
            // Create policy for resource
            var resourcePolicyName = ResourcesConsulClient.getResourcePolicyName(resource.getId());
            var resourcePolicyRule =  "node \"" + resource.getId() + "\" { policy = \"read\" }";
            var resourcePolicy = Policy.builder(resourcePolicyName)
                        .description("Access policy for resource '" + resource.getId() + "'")
                        .rules(resourcePolicyRule)
                    .build();
            var createdPolicy = this.consulClient.acl().createPolicy(resourcePolicy);
            // Assign resource policy to owner group role
            this.consulClient.acl().addPolicyToRole(fullPathOwnerGroupId, createdPolicy.getId());
            // Retrieve newly created resource from Consul
            var newResourceNode = this.consulClient.nodes().getNodeById( resource.getId());
            resource = ResourceConsulNode.convertConsulNodeToBasicResource(newResourceNode.get());

            return resource;
        } catch (ConsulLoginFailedException e) {
            throw new ResourceRuntimeException(e);
        }
    }

    public BasicResource updateResource(BasicResource resource) {
        var existingResource = this.getResourceByIdOrThrow(resource.getId());

        var resourceConsulNode = new ResourceConsulNode(resource);
        this.consulClient.nodes().registerNode(resourceConsulNode);

        // Retrieve updated resource from Consul
        var newResourceNode = this.consulClient.nodes().getNodeById(resource.getId());
        resource = ResourceConsulNode.convertConsulNodeToBasicResource(newResourceNode.get());
        return resource;
    }

    public void setResourceLocation (UUID resourceId, Location location) {
        Map<String, String> locationMetaData = new HashMap<>();
        locationMetaData.put(ResourceConsulNode.META_KEY_LOCATION, location.getId().toString());

        this.consulClient.nodes().addMetaDataToNode( resourceId, locationMetaData);
    }

    public void deleteResource(BasicResource resource)
            throws ConsulLoginFailedException {
        var consulNodeOptional = this.consulClient.nodes().getNodeById(resource.getId());
        if(consulNodeOptional.isEmpty()) {
            throw new ConsulNodeNotFoundException(resource.getId());
        }

        // Delete node
        var consulNode = consulNodeOptional.get();
        this.consulClient.nodes().deleteNodeById(consulNode.getId());
        // Delete resource policy
        var resourcePolicyName = ResourcesConsulClient.getResourcePolicyName(resource.getId());
        var resourcePolicy = this.consulClient.acl().getPolicyByNameOrThrow(resourcePolicyName);
        this.consulClient.acl().deletePolicyById(resourcePolicy.getId());

        LOG.info("Resource with id '" + resource.getId() + "' successfully delete in Consul");
    }
    //endregion
}
