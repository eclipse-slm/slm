package org.eclipse.slm.resource_management.common.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.*;
import org.eclipse.slm.common.consul.model.acl.BindingRule;
import org.eclipse.slm.common.consul.model.catalog.CatalogNode;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.catalog.TaggedAddresses;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.location.Location;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.location.LocationJpaRepository;
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType;
import org.eclipse.slm.resource_management.common.remote_access.CredentialClass;
import org.eclipse.slm.resource_management.common.remote_access.CredentialUsernamePassword;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessConsulService;
import org.eclipse.slm.resource_management.common.resources.BasicResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class ResourcesConsulClient {

    public final static Logger LOG = LoggerFactory.getLogger(ResourcesConsulClient.class);
    private static final String META_KEY_LOCATION = "locationId";
    private static final String META_KEY_RESOURCE_ID = "resourceId";
    private static final String META_KEY_ASSET_ID = "assetId";
    private static final String META_KEY_FIRMWARE_VERSION = "firmwareVersion";
    private static final String META_KEY_CONNECTION_TYPE = "resourceConnectionType";
    private static final String META_KEY_DRIVER_ID = "driverId";

    private static final String KV_FOLDER_REMOTE_ACCESS_SERVICES = "remoteAccessServices";

    public static final String POLICY_RESOURCE_PREFIX = "resource_";
    public static final String KEYCLOAK_ROLE_RESOURCE_PREFIX = "resource_";

    private final ConsulNodesApiClient consulNodesApiClient;
    private final ConsulAclApiClient consulAclApiClient;
    private final ConsulServicesApiClient consulServicesApiClient;
    private final ConsulGenericServicesClient consulGenericServicesClient;
    private final ConsulKeyValueApiClient consulKeyValueApiClient;
    private final LocationJpaRepository locationJpaRepository;
    private final ConsulGenericNodeRemoveClient consulGenericNodeRemoveClient;

    public static String getResourcePolicyName(UUID resourceId) {
        return POLICY_RESOURCE_PREFIX + resourceId;
    }

    public static String getResourceKeycloakRoleName(UUID resourceId) {
        return KEYCLOAK_ROLE_RESOURCE_PREFIX + resourceId;
    }

    public ResourcesConsulClient(
            ConsulNodesApiClient consulNodesApiClient,
            ConsulAclApiClient consulAclApiClient,
            ConsulServicesApiClient consulServicesApiClient,
            ConsulGenericServicesClient consulGenericServicesClient,
            ConsulKeyValueApiClient consulKeyValueApiClient,
            ConsulGenericNodeRemoveClient consulGenericNodeRemoveClient,
            LocationJpaRepository locationJpaRepository
    ) {
        this.consulNodesApiClient = consulNodesApiClient;
        this.consulAclApiClient = consulAclApiClient;
        this.consulServicesApiClient = consulServicesApiClient;
        this.consulGenericServicesClient = consulGenericServicesClient;
        this.consulKeyValueApiClient = consulKeyValueApiClient;
        this.consulGenericNodeRemoveClient = consulGenericNodeRemoveClient;
        this.locationJpaRepository = locationJpaRepository;
    }

    public List<BasicResource> getResources(ConsulCredential consulCredential)
            throws ConsulLoginFailedException {
        List<BasicResource> resources = new ArrayList<>();

        try {
            var consulNodes = this.consulNodesApiClient.getNodes(consulCredential);
            for (var node : consulNodes) {
                Optional<NodeService> optionalConsulService = consulServicesApiClient
                        .getNodeServices(consulCredential, node.getNode())
                        .stream()
                        .filter(nodeService -> nodeService.getService().equals(ConsulServicesApiClient.CONSUL_SERVICE_NAME))
                        .findFirst();

                if (node.getMeta() != null && optionalConsulService.isEmpty()) {
                    var basicResource = this.convertConsulNodeToBasicResource(node);
                    resources.add(basicResource);
                }
            }
        }
        catch (Exception e) {
            LOG.error("Error while retrieving resources from Consul: {}", e.getMessage(), e);
            return resources;
        }

        return resources;
    }

    public Optional<BasicResource> getResourceById(ConsulCredential consulCredential, UUID resourceId) throws ConsulLoginFailedException, ResourceNotFoundException {
        Optional<Node> optionalNode = consulNodesApiClient.getNodeById(consulCredential, resourceId);

        if(optionalNode.isEmpty())
            return Optional.empty();

        consulServicesApiClient.getNodeServicesByNodeId(consulCredential, resourceId);

        return Optional.of(
                this.convertConsulNodeToBasicResource(optionalNode.get())
        );
    }

    public Optional<BasicResource> getResourceByHostname(ConsulCredential consulCredential, String hostname) throws ConsulLoginFailedException {
        Predicate<Node> p = (n) -> n.getNode().equals(hostname);

        return getResourceByX(consulCredential,p);
    }

    public Optional<BasicResource> getResourceByIp(ConsulCredential consulCredential, String ip) throws ConsulLoginFailedException {
        Predicate<Node> p = (n) -> n.getAddress().equals(ip);

        return getResourceByX(consulCredential, p);
    }

    private Optional<BasicResource> getResourceByX(ConsulCredential consulCredential, Predicate predicate) throws ConsulLoginFailedException {
        Optional<Node> optionalNode = consulNodesApiClient.getNodes(consulCredential).stream().filter(predicate).findFirst();

        if(optionalNode.isEmpty())
            return Optional.empty();

        return Optional.of(convertConsulNodeToBasicResource(optionalNode.get()));
    }

    //region ConnectionService
    public RemoteAccessConsulService addConnectionService(
            ConnectionType connectionType,
            int connectionPort,
            UUID resourceId,
            CredentialUsernamePassword credential
    ) throws ConsulLoginFailedException {
        var consulCredential = new ConsulCredential();
        var consulResourcePolicyName = POLICY_RESOURCE_PREFIX + resourceId;

        var remoteAccessConsulService = new RemoteAccessConsulService(
                connectionType,
                connectionPort,
                credential
        );

        // Register Consul service
        consulGenericServicesClient.registerService(
                consulCredential,
                resourceId,
                remoteAccessConsulService.getService(),
                remoteAccessConsulService.getId(),
                Optional.ofNullable(remoteAccessConsulService.getPort()),
                remoteAccessConsulService.getTags(),
                remoteAccessConsulService.getServiceMeta()
        );
        this.consulAclApiClient.addReadRuleToPolicy(consulCredential,   // Add read access for remote access Consul service to resource policy
                consulResourcePolicyName,
                "service",
                remoteAccessConsulService.getService());

        // Register Consul KV
        var kvPathPrefix = KV_FOLDER_REMOTE_ACCESS_SERVICES + "/" + remoteAccessConsulService.getId();
        consulKeyValueApiClient.createKey(
                consulCredential,
                kvPathPrefix + "/credentialClasses",
                Arrays.asList(remoteAccessConsulService.getCredential().getClass().getSimpleName())
        );

        consulKeyValueApiClient.createKey(
                consulCredential,
                kvPathPrefix + "/connectionTypes",
                Arrays.asList(remoteAccessConsulService.getConnectionType().name())
        );

        this.consulAclApiClient.addReadRuleToPolicy(consulCredential,   // Add read access for Consul KV folder remote access to resource policy
                consulResourcePolicyName,
                "key_prefix",
                kvPathPrefix);

        return remoteAccessConsulService;
    }

    public void removeConnectionService(UUID resourceId, UUID remoteAccessId) throws ConsulLoginFailedException {
        var consulCredential = new ConsulCredential();
        var consulResourcePolicyName = POLICY_RESOURCE_PREFIX + resourceId;
        var kvPathPrefix = KV_FOLDER_REMOTE_ACCESS_SERVICES + "/" + remoteAccessId;

        var optionalRemoteAccessService = getRemoteAccessServiceOfResourceAsNodeService(consulCredential, resourceId);
        if(optionalRemoteAccessService.isPresent()) {
            this.consulServicesApiClient.removeServiceByName(consulCredential, resourceId, optionalRemoteAccessService.get().getService());
            this.consulAclApiClient.addReadRuleToPolicy(consulCredential,   // Add read access for remote access Consul service to resource policy
                    consulResourcePolicyName,
                    "service",
                    optionalRemoteAccessService.get().getService());

            this.consulKeyValueApiClient.deleteKeyRecursive(consulCredential, kvPathPrefix);
            this.consulAclApiClient.removeReadRuleFromPolicy(consulCredential,
                    consulResourcePolicyName,
                    "key_prefix",
                    kvPathPrefix);
        }
        else {
            LOG.error("Unable to remove remote access service with id '" + remoteAccessId + "' for resource with id '" + resourceId + "', no corresponding Consul service found.");
        }
    }

    public Optional<NodeService> getRemoteAccessServiceOfResourceAsNodeService(ConsulCredential consulCredential, UUID resourceId) throws ConsulLoginFailedException {
        return consulServicesApiClient.getNodeServiceByNodeIdAndServiceTag(
                consulCredential,
                resourceId,
                RemoteAccessConsulService.class.getSimpleName()
        );
    }

    public List<ConnectionType> getConnectionTypesOfRemoteAccessService(
            ConsulCredential consulCredential,
            UUID remoteAccessId
    ) throws ConsulLoginFailedException, JsonProcessingException {
        List<String> connectionTypesAsStrings = consulKeyValueApiClient.getValueOfKey(
                consulCredential,
                KV_FOLDER_REMOTE_ACCESS_SERVICES + "/" + remoteAccessId + "/connectionTypes",
                new TypeReference<>() {}
        );

        List<ConnectionType> resourceConnectionTypes = new ArrayList<>();
        if (connectionTypesAsStrings != null) {
            for (String connectionType : connectionTypesAsStrings) {
                resourceConnectionTypes.add(ConnectionType.valueOf(connectionType));
            }
        }

        return resourceConnectionTypes;
    }

    public List<CredentialClass> getCredentialClassesOfRemoteAccessService(
            ConsulCredential consulCredential,
            UUID remoteAccessId
    ) throws ConsulLoginFailedException, JsonProcessingException {
        List<String> credentialClassesAsString = consulKeyValueApiClient.getValueOfKey(
                consulCredential,
                KV_FOLDER_REMOTE_ACCESS_SERVICES + "/" + remoteAccessId + "/credentialClasses",
                new TypeReference<>() {}
        );

        List<CredentialClass> credentialClasses = new ArrayList<>();
        if (credentialClassesAsString != null) {
            for (String credentialClass : credentialClassesAsString) {
                credentialClasses.add(CredentialClass.valueOf(credentialClass));
            }
        }

        return credentialClasses;

    }
    //endregion

    //region ADD/DELETE
    public BasicResource addResource(BasicResource resource) throws ConsulLoginFailedException {
        /// Add new resource as node in Consul
        CatalogNode node = new CatalogNode();
        node.setId(resource.getId());
        node.setNode(resource.getHostname());
        node.setAddress(resource.getIp());

        var taggedAddresses = new TaggedAddresses();
        taggedAddresses.setLan(resource.getIp());
        node.setTaggedAddresses(taggedAddresses);

        HashMap<String, String> meta = new HashMap<>();
        meta.put("external-node", "true");
        meta.put("external-probe", "true");
        meta.put(META_KEY_RESOURCE_ID, resource.getId().toString());
        if (resource.getAssetId() != null) {
            meta.put(META_KEY_ASSET_ID, resource.getAssetId());
        }
        if (resource.getFirmwareVersion() != null) {
            meta.put(META_KEY_FIRMWARE_VERSION, resource.getFirmwareVersion());
        }
        if (resource.getDriverId() != null) {
            meta.put(META_KEY_DRIVER_ID, resource.getDriverId());
        }
        node.setNodeMeta(meta);

        this.consulNodesApiClient.registerNode(new ConsulCredential(), node);

        var newResourceNode = this.consulNodesApiClient.getNodeById(new ConsulCredential(), resource.getId());
        resource = this.convertConsulNodeToBasicResource(newResourceNode.get());

        return resource;
    }

    public void setResourceLocation (UUID resourceId, Location location)
            throws ConsulLoginFailedException {
        Map<String, String> locationMetaData = new HashMap<>();
        locationMetaData.put(META_KEY_LOCATION, location.getId().toString());

        this.consulNodesApiClient.addMetaDataToNode(new ConsulCredential(), resourceId, locationMetaData);
    }

    public void setResourceConnectionType (UUID resourceId, ConnectionType connectionType)
            throws ConsulLoginFailedException {
        Map<String, String> connectionTypeMetaData = new HashMap<>();
        connectionTypeMetaData.put(META_KEY_CONNECTION_TYPE, connectionType.name());

        this.consulNodesApiClient.addMetaDataToNode(new ConsulCredential(), resourceId, connectionTypeMetaData);
    }

    public void deleteResource(ConsulCredential consulCredential, BasicResource resource)
            throws ConsulLoginFailedException {
        List<String> policyNames = new ArrayList<>();
        List<String> roleNames = new ArrayList<>();
        List<String> bindungRuleNames = new ArrayList<>();
        var consulNodeOptional = this.consulNodesApiClient.getNodeById(consulCredential, resource.getId());

        if(consulNodeOptional.isEmpty()) {
            LOG.error("Unable to delete resource with id '" + resource.getId() + "', no corresponding Consul node found.");
            return;
        }

        var consulNode = consulNodeOptional.get();
        this.consulGenericNodeRemoveClient.removeNode(consulCredential, consulNode.getNode());

        policyNames.add("resource_"+resource.getId());
        roleNames.add("resource_"+resource.getId());
        bindungRuleNames.add(String.valueOf(resource.getId()));

//        var policyName = "resource_" + resource.getId();
        for(String policyName : policyNames) {
            var policy = this.consulAclApiClient.getPolicyByName(consulCredential, policyName);
            if (policy != null) {
                this.consulAclApiClient.deletePolicyById(consulCredential, policy.getId());
            } else {
                LOG.error("Unable to delete Consul policy for resource with id '" + resource.getId()
                        + "', policy with name '" + policyName + "'not found");
            }
        }


//        var roleName = "resource_" + resource.getId();
        for(String roleName : roleNames) {
            var role = this.consulAclApiClient.getRoleByName(consulCredential, roleName);
            if (role != null) {
                this.consulAclApiClient.deleteRoleById(consulCredential, role.getId());
            } else {
                LOG.error("Unable to delete Consul role for resource with id '" + resource.getId()
                        + "', role with name '" + roleName + "'not found");
            }
        }


        var bindingRules = this.consulAclApiClient.getBindingRules(consulCredential);

        for(String bindingRuleName : bindungRuleNames) {
            List<BindingRule> bindingRulesOfResource = bindingRules.stream()
                    .filter(
                            r -> r.getBindName().equals(bindingRuleName)
                    )
                    .collect(Collectors.toList());
            if (bindingRulesOfResource.size() > 0) {
                for (BindingRule bindingRule : bindingRulesOfResource) {
                    this.consulAclApiClient.deleteBindingRuleById(consulCredential, bindingRule.getId());
                }
            }
        }

        LOG.info("Resource with id '" + resource.getId() + "' successfully delete in Consul");
    }
    //endregion

    private BasicResource convertConsulNodeToBasicResource(Node node) {
        var basicResource = new BasicResource(node.getId());

        getResourceLocation(node).ifPresent(resourceLocation -> {
            basicResource.setLocationId(resourceLocation.getId());
        });
        basicResource.setHostname(node.getNode());
        basicResource.setIp(node.getAddress());
        if (node.getMeta().containsKey(META_KEY_ASSET_ID)) {
            basicResource.setAssetId(node.getMeta().get(META_KEY_ASSET_ID));
        }
        if (node.getMeta().containsKey(META_KEY_FIRMWARE_VERSION)) {
            basicResource.setFirmwareVersion(node.getMeta().get(META_KEY_FIRMWARE_VERSION));
        }
        if (node.getMeta().containsKey(META_KEY_DRIVER_ID)) {
            basicResource.setDriverId(node.getMeta().get(META_KEY_DRIVER_ID));
        }

        return basicResource;
    }

    private Optional<Location> getResourceLocation(Node node) {
        Optional<String> optionalLocationId = Optional.ofNullable(
            node.getMeta().get(META_KEY_LOCATION)
        );

        if (optionalLocationId.isPresent())
            return locationJpaRepository.findById(
                    UUID.fromString(optionalLocationId.get())
            );
        else
            return Optional.empty();
    }
}
