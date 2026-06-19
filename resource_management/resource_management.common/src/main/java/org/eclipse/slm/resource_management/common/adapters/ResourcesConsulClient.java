package org.eclipse.slm.resource_management.common.adapters;

import java.util.UUID;

/**
 * Temporary holder for the Consul ACL policy-name convention for resources.
 *
 * Resource storage has moved to the database (see ResourcesManagerImpl + ResourceJpaRepository).
 * Only the policy-name helper remains, because cluster ACL handling
 * (ClusterCreateFunctions) and the notification_service still create/parse Consul
 * ACL policies named "resource_<id>". This class should be removed once those are migrated.
 */
public class ResourcesConsulClient {

    public static final String POLICY_RESOURCE_PREFIX = "resource_";

    public static String getResourcePolicyName(UUID resourceId) {
        return POLICY_RESOURCE_PREFIX + resourceId;
    }
}
