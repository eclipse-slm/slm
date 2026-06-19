package org.eclipse.slm.resource_management.common.resources;

import org.eclipse.slm.resource_management.common.access.UserContext;

import java.util.UUID;

public interface ResourceUpdatedListener {

    void onResourceUpdated(UUID resourceId, UserContext userContext);

}
