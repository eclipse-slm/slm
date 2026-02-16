package org.eclipse.slm.resource_management.features.capabilities;


import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.model.catalog.Service;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionConfigParameter;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionConfigParameterValueType;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterMemberType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class CapabilityUtil {

    private final static Logger LOG = LoggerFactory.getLogger(CapabilityUtil.class);

    public Optional<ClusterMemberType> getClusterMemberTypeOfConsulService(Capability capability, Service service) {
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

    public Map<UUID, String> getMemberMappingOfMultiHostCapabilityService(Capability capability, UUID serviceId) {
//        Map<UUID, String> memberMapping = new HashMap<>();
//        CapabilityService capabilityService = new CapabilityService(capability, serviceId, "");
//        Optional<List<Service>> optionalServices = this.consulAdminClient.services().getServiceByName(capabilityService.getServiceName());
//        List<Service> services = null;
//
//        if(optionalServices.isEmpty())
//            return memberMapping;
//        else
//            services = optionalServices.get();
//
//        for(Service service : services) {
//            Optional<ClusterMemberType> clusterMemberTypeOptional = getClusterMemberTypeOfConsulService(
//                    capability,
//                    service
//            );
//
//            if(clusterMemberTypeOptional.isPresent())
//                memberMapping.put(
//                        service.getNodeId(),
//                        clusterMemberTypeOptional.get().getName()
//                );
//
//        }
//
//        return memberMapping;

        return null;
    }

    private static Map<String, String> getConfigParameterFilteredBySecret(
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

    public static Map<String, String> getSecretConfigParameter(
            Capability capability,
            Map<String, String> configParameter
    ) {
        return CapabilityUtil.getConfigParameterFilteredBySecret(capability, configParameter, true);
    }

    public static Map<String, String> getNonSecretConfigParameter(
            Capability capability,
            Map<String, String> configParameter
    ) {
        return CapabilityUtil.getConfigParameterFilteredBySecret(capability, configParameter, false);
    }

    public static Map<String, String> getCustomMeta(NodeService nodeService) throws IllegalAccessException {
        return CapabilityUtil.getCustomMeta(nodeService.getMeta());
    }

    public static Map<String, String> getCustomMeta(Map<String, String> serviceMeta) throws IllegalAccessException {
        List<String> staticFieldNames = new ArrayList<>();
        for (Field field : CapabilityService.class.getDeclaredFields()) {
            if (field.getType().equals(String.class) && Modifier.isStatic(field.getModifiers())) {
                String metaKey = (String) field.get(null);
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

    public static Optional<Integer> getServicePortFromConfigParameter(
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
