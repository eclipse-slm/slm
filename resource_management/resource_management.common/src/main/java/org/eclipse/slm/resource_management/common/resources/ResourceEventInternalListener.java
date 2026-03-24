package org.eclipse.slm.resource_management.common.resources;

public interface ResourceEventInternalListener {

    void onResourceCreated(ResourceDTO resourceDTO);

    void onResourceUpdated(ResourceDTO resourceDTO);

    void onResourceDeleted(ResourceDTO resourceDTO);

}
