package org.eclipse.slm.resource_management.common.resources;

import java.util.UUID;

public interface ResourceUpdatedListener {

    void onResourceUpdated(UUID resourceId);

}
