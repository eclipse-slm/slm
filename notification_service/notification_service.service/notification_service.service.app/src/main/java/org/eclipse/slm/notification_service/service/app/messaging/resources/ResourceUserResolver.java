package org.eclipse.slm.notification_service.service.app.messaging.resources;

import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.notification_service.service.app.messaging.UserUtils;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves and caches the users associated with a resource, so that users can still be notified after the
 * resource (and its Consul policy) has been deleted.
 */
@Component
public class ResourceUserResolver {

    private final UserUtils userUtils;

    private final Map<UUID, List<String>> resourceIdToUserIdsCache = new ConcurrentHashMap<>();

    public ResourceUserResolver(UserUtils userUtils, ConsulClientFactory consulClientFactory) {
        this.userUtils = userUtils;

        // Get for all existing resources the associated userIds and cache them, so that the correct userIds can be notified when a resource is deleted
        var consulClient = consulClientFactory.createAdminClient();
        var consulRoles = consulClient.acl().getRoles();
        for (var role : consulRoles) {
            if (role.getPolicies() != null) {
                for (var policyLink : role.getPolicies()) {
                    if (policyLink.getName().startsWith(ResourcesConsulClient.POLICY_RESOURCE_PREFIX) && !policyLink.getName().contains("management")) {
                        var resourceIdString = policyLink.getName().substring(ResourcesConsulClient.POLICY_RESOURCE_PREFIX.length());
                        var resourceId = UUID.fromString(resourceIdString);
                        var userId = role.getName().substring("users_".length());
                        this.resourceIdToUserIdsCache.computeIfAbsent(resourceId, k -> new ArrayList<>()).add(userId);
                    }
                }
            }
        }
    }

    public List<String> refresh(UUID resourceId) {
        var userIds = userUtils.getUserIdsAssociatedToPolicy(ResourcesConsulClient.getResourcePolicyName(resourceId));
        this.resourceIdToUserIdsCache.put(resourceId, userIds);
        return userIds;
    }

    public List<String> evict(UUID resourceId) {
        var userIds = this.resourceIdToUserIdsCache.remove(resourceId);
        return userIds != null ? userIds : new ArrayList<>();
    }

    public List<String> getUserIds(UUID resourceId) {
        var userIds = this.resourceIdToUserIdsCache.get(resourceId);
        return userIds != null ? userIds : refresh(resourceId);
    }

    public Set<UUID> getKnownResourceIds() {
        return new HashSet<>(this.resourceIdToUserIdsCache.keySet());
    }
}
