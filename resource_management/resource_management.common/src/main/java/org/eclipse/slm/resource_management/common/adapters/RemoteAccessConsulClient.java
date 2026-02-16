package org.eclipse.slm.resource_management.common.adapters;

import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.model.acl.Policy;
import org.eclipse.slm.resource_management.common.remote_access.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RemoteAccessConsulClient {
    private final static Logger LOG = LoggerFactory.getLogger(RemoteAccessConsulClient.class);

    public static final String REMOTE_ACCESS_SERVICE_POLICY_PREFIX = "remote-access-service_";

    public static String getRemoteAccessServicePolicyName(UUID remoteAccessId) {
        return REMOTE_ACCESS_SERVICE_POLICY_PREFIX + remoteAccessId.toString();
    }

    private final ConsulClient consulClient;

    public RemoteAccessConsulClient(ConsulClient consulClient) {
        this.consulClient = consulClient;
    }

    public RemoteAccessDTOReadMinimal addRemoteAccess(RemoteAccessCreateDTO remoteAccess, UUID resourceId)  {
        // Register Consul service
        var remoteAccessConsulService = new RemoteAccessConsulService(UUID.randomUUID(), remoteAccess);
        var serviceOfCatalogRegistration = RemoteAccessConsulMapper.INSTANCE.toCatalogRegistrationService(remoteAccessConsulService);
        var createdConsulService = this.consulClient.services().registerService(resourceId, serviceOfCatalogRegistration);
        // Create read access policy and assign it to role of user group of owner
        var policyName = getRemoteAccessServicePolicyName(remoteAccessConsulService.getId());
        var policyRule =  "service \"" + remoteAccessConsulService.getServiceName() + "\" { policy = \"read\" }";
        var policy = Policy.builder(policyName)
                .rules(policyRule)
            .build();
        var createdPolicy = this.consulClient.acl().createPolicy(policy);
        this.consulClient.acl().addPolicyToRole(remoteAccess.getFullPathOwnerGroupId(), createdPolicy.getId());
        // Get and return created remote access
        var createdRemoteAccess = this.getRemoteAccessByIdOrThrow(resourceId, createdConsulService.getServiceId());
        return createdRemoteAccess;
    }

    public void removeRemoteAccess(UUID resourceId, UUID remoteAccessId) {
        var remoteAccess = this.getRemoteAccessByIdOrThrow(resourceId, remoteAccessId);

        var remoteAccessConsulServiceName = RemoteAccessConsulService.convertIdToServiceName(
                remoteAccess.getId(),
                remoteAccess.getConnectionType());
        // Unregister Consul service
        this.consulClient.services().removeServiceByName(resourceId, remoteAccessConsulServiceName);
        // Remove remote access policy
        var policyName = getRemoteAccessServicePolicyName(remoteAccessId);
        var policy = this.consulClient.acl().getPolicyByNameOrThrow(policyName);
        this.consulClient.acl().deletePolicyById(policy.getId());
    }

    public RemoteAccessDTOReadMinimal getRemoteAccessByIdOrThrow(UUID resourceId, UUID remoteAccessId) {
        var remoteAccessOptional = getRemoteAccessById(resourceId, remoteAccessId);

        if (remoteAccessOptional.isEmpty()) {
            throw new RemoteAccessNotFoundException(remoteAccessId, resourceId);
        }

        return remoteAccessOptional.get();
    }

    public Optional<RemoteAccessDTOReadMinimal> getRemoteAccessById(UUID resourceId, UUID remoteAccessId) {
        var remoteAccesses = getRemoteAccesses(resourceId);

        var filteredRemoteAccessOptional = remoteAccesses.stream()
                .filter(remoteAccess -> remoteAccess.getId().toString().equals(remoteAccessId.toString()))
                .findFirst();;

        return filteredRemoteAccessOptional;
    }

    public List<RemoteAccessDTOReadMinimal> getRemoteAccesses(UUID resourceId) {
        var remoteAccesses = new ArrayList<RemoteAccessDTOReadMinimal>();

        var consulRemoteAccessNodeServices = this.consulClient.services()
                .getNodeServicesByNodeIdAndServiceTag(resourceId, RemoteAccessConsulService.class.getSimpleName());

        for (var consuleRemoteAccessNodeService : consulRemoteAccessNodeServices) {
            var remoteAccessConsulService = RemoteAccessConsulService.createFromNodeService(consuleRemoteAccessNodeService);
            var remoteAccess = RemoteAccessConsulMapper.INSTANCE.toDto(remoteAccessConsulService);
            remoteAccesses.add(remoteAccess);
        }

        return remoteAccesses;
    }
}
