package org.eclipse.slm.resource_management.features.capabilities;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.catalog.CatalogNode;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionConfigParameter;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionConfigParameterValueType;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterMemberType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CapabilityUtil {

    private final static Logger LOG = LoggerFactory.getLogger(CapabilityUtil.class);
    private final CapabilityJpaRepository capabilityJpaRepository;
    private final ConsulNodesApiClient consulNodesApiClient;
    private final ConsulServicesApiClient consulServicesApiClient;

    public CapabilityUtil(
            CapabilityJpaRepository capabilityJpaRepository,
            ConsulNodesApiClient consulNodesApiClient,
            ConsulServicesApiClient consulServicesApiClient
    ) {
        this.capabilityJpaRepository = capabilityJpaRepository;
        this.consulNodesApiClient = consulNodesApiClient;
        this.consulServicesApiClient = consulServicesApiClient;
    }

    public Optional<ClusterMemberType> getClusterMemberTypeOfConsulService(Capability capability, CatalogService service) {
        return capability.getClusterMemberTypes()
                .stream()
                .filter(t -> {
                    if (service.getServiceMeta().containsKey("clusterMemberType")) {
                        return service.getServiceMeta().get("clusterMemberType").equals(t.getName());
                    }
                    else {
                        return false;
                    }
                })
                .findFirst();
    }

    public CapabilityService getCapabilityServiceFromNodeService(
            ConsulCredential consulCredential,
            NodeService nodeService
    ) throws ConsulLoginFailedException {
        return getCapabilityServiceFromServiceIdAndCapabilityId(
                consulCredential,
                UUID.fromString(nodeService.getID()),
                UUID.fromString(nodeService.getMeta().get("capabilityId"))
        );
    }

    public CapabilityService getCapabilityServiceFromCatalogService(ConsulCredential consulCredential, CatalogService catalogService) throws ConsulLoginFailedException {
        return getCapabilityServiceFromServiceIdAndCapabilityId(
                consulCredential,
                UUID.fromString(catalogService.getServiceId()),
                UUID.fromString(catalogService.getServiceMeta().get("capabilityId"))
        );
    }

    private CapabilityService getCapabilityServiceFromServiceIdAndCapabilityId(
            ConsulCredential consulCredential,
            UUID serviceId,
            UUID capabilityId
    ) throws ConsulLoginFailedException {
        var capabilityOptional = this.capabilityJpaRepository.findById(capabilityId);

        if (capabilityOptional.isPresent()) {
            Capability capability = capabilityOptional.get();
            CapabilityService capabilityService = new CapabilityService(capability, serviceId);

            if(capability.isCluster()) {
                return new MultiHostCapabilityService(
                        capability,
                        getMemberMappingOfMultiHostCapabilityService(consulCredential, capability, serviceId),
                        serviceId,
                        capabilityService.getStatus(),
                        capabilityService.getManaged()
                );
            } else {
                Optional<List<CatalogService>> optionalCatalogService = consulServicesApiClient.getServiceByName(
                        consulCredential,
                        capabilityService.getService()
                );

                CatalogService catalogService = optionalCatalogService.get().get(0);

                return new SingleHostCapabilityService(
                        capability,
                        catalogService.getId(),
                        serviceId,
                        getStatusOfConsulCatalogService(catalogService),
                        getIsManagedOfConsulService(catalogService)
                );
            }
        } else {
            LOG.error("Unable to convert Consul Service [id='" + serviceId + "'] to capability, capability [id='"
                    + capabilityId + "']not found in database");
            return null;
        }
    }

    public Map<UUID, String> getMemberMappingOfMultiHostCapabilityService(
            ConsulCredential consulCredential,
            Capability capability,
            UUID serviceId
    ) throws ConsulLoginFailedException {
        Map<UUID, String> memberMapping = new HashMap<>();
        CapabilityService capabilityService = new CapabilityService(capability, serviceId);
        Optional<List<CatalogService>> optionalServices = consulServicesApiClient.getServiceByName(
                consulCredential,
                capabilityService.getService()
        );
        List<CatalogService> services = null;

        if(optionalServices.isEmpty())
            return memberMapping;
        else
            services = optionalServices.get();

        for(CatalogService service : services) {
            Optional<ClusterMemberType> clusterMemberTypeOptional = getClusterMemberTypeOfConsulService(
                    capability,
                    service
            );

            if(clusterMemberTypeOptional.isPresent())
                memberMapping.put(
                        service.getId(),
                        clusterMemberTypeOptional.get().getName()
                );

        }

        return memberMapping;
    }

    public CatalogNode.Check getCheckByCapability(
            ConsulCredential consulCredential,
            UUID nodeId,
            Capability capability
    ) throws ResourceNotFoundException, ConsulLoginFailedException {
        Optional<Node> optionalNode = consulNodesApiClient.getNodeById(consulCredential, nodeId);

        if(optionalNode.isEmpty())
            throw new ResourceNotFoundException(nodeId);

        CapabilityHealthCheck capabilityHealthCheck = capability.getHealthCheck();
        var httpCheckDefinition = new CatalogNode.HttpCheckDefinition();
        httpCheckDefinition.setHttp(capabilityHealthCheck.getUrl(optionalNode.get().getAddress()));
        httpCheckDefinition.setInterval(capabilityHealthCheck.getInterval() + "s");
        var consulCheck = new CatalogNode.Check();
        consulCheck.setCheckId(capability.getId().toString());
        consulCheck.setName("capability_" + capability.getName());
        consulCheck.setDefinition(httpCheckDefinition);

        return consulCheck;
    }

    public Map<String, List<String>> getCapabilityServiceNamesAndTagsMapByTag(
            ConsulCredential consulCredential,
            String tag
    ) throws ConsulLoginFailedException {
        Map<String, List<String>> serviceNameToServiceTagMap = consulServicesApiClient.getServicesOfUserFilteredByTag(
                consulCredential,
                tag
        );

        return serviceNameToServiceTagMap;
    }

    public Boolean getIsManagedOfConsulService(CatalogService serviceOfNode) {
        return getIsManagedFromMetaInfo(serviceOfNode.getServiceMeta());
    }

    public Boolean getIsManagedOfConsulService(NodeService serviceOfNode) {
        return getIsManagedFromMetaInfo(serviceOfNode.getMeta());
    }

    private Boolean getIsManagedFromMetaInfo(Map<String, String> meta) {
        return  Boolean.valueOf(meta.get(CapabilityService.META_KEY_MANAGED));
    }

    public CapabilityServiceStatus getStatusOfConsulService(NodeService nodeService) {
        return getStatusFromServiceMeta(nodeService.getMeta());
    }

    public CapabilityServiceStatus getStatusOfConsulCatalogService(CatalogService catalogService) {
        return getStatusFromServiceMeta(catalogService.getServiceMeta());
    }

    private CapabilityServiceStatus getStatusFromServiceMeta(Map<String, String> serviceMeta) {
        return CapabilityServiceStatus.valueOf(
                serviceMeta.get(CapabilityService.META_KEY_STATUS)
        );
    }

    private Map<String, String> getConfigParameterFilteredBySecret(
            Capability capability,
            Map<String, String> configParameter,
            Boolean expectedSecretPropertyValue
    ) {
        List<ActionConfigParameter> secretConfigParameters = capability
                .getActions()
                .get(ActionType.INSTALL)
                .getConfigParameters()
                .stream()
                .filter(cp -> cp.getSecret() == expectedSecretPropertyValue)
                .collect(Collectors.toList());

        List<String> nonSecretConfigParameterNames = secretConfigParameters
                .stream()
                .map(nscp -> nscp.getName())
                .collect(Collectors.toList());

        return configParameter
                .entrySet()
                .stream()
                .filter(cp-> nonSecretConfigParameterNames.contains(cp.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, String> getSecretConfigParameter(
            Capability capability,
            Map<String, String> configParameter
    ) {
        return getConfigParameterFilteredBySecret(capability, configParameter, true);
    }

    public Map<String, String> getNonSecretConfigParameter(
            Capability capability,
            Map<String, String> configParameter
    ) {
        return getConfigParameterFilteredBySecret(capability, configParameter, false);
    }

    public Map<String, String> getCustomMeta(NodeService nodeService) throws IllegalAccessException {
        return getCustomMeta(nodeService.getMeta());
    }

    public Map<String, String> getCustomMeta(CatalogService catalogService) throws IllegalAccessException {
        return getCustomMeta(catalogService.getServiceMeta());
    }

    private Map<String, String> getCustomMeta(Map<String, String> serviceMeta) throws IllegalAccessException {
        List<String> staticFieldNames = new ArrayList<>();
        for (Field f : CapabilityService.class.getDeclaredFields()) {
            if (f.getType().equals(String.class) && Modifier.isStatic(f.getModifiers())) {
                String metaKey = (String) f.get(new CapabilityService());
                staticFieldNames.add(metaKey);
            }
        }

        Map<String, String> customMeta = serviceMeta
                .entrySet()
                .stream()
                .filter(set -> !staticFieldNames.contains(set.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return customMeta;
    }

    public Optional<Integer> getServicePortFromConfigParameter(
            Capability capability,
            Map<String, String> configParameter
    ) {
        Optional<ActionConfigParameter> optionalServicePortConfigParamDefinition = capability
                .getActions()
                .get(ActionType.INSTALL)
                .getConfigParameters()
                .stream()
                .filter(cp -> cp.getValueType().equals(ActionConfigParameterValueType.SERVICE_PORT))
                .findFirst();

        if(optionalServicePortConfigParamDefinition.isEmpty())
            return Optional.empty();

        ActionConfigParameter servicePortConfigParamDefinition = optionalServicePortConfigParamDefinition.get();
        String servicePort = configParameter.get(servicePortConfigParamDefinition.getName());

        if(servicePort == null)
            return Optional.empty();

        return Optional.of(Integer.valueOf(servicePort)
        );
    }
}
