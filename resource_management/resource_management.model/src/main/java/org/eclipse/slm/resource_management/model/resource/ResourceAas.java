package org.eclipse.slm.resource_management.model.resource;

import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;

import java.util.UUID;

public class ResourceAas extends DefaultAssetAdministrationShell {

    public static final String AAS_ID_PREFIX = "Resource_";

    public ResourceAas(BasicResource resource) {
        this.id = AAS_ID_PREFIX + resource.getId();
        this.idShort = AAS_ID_PREFIX + resource.getId();
    }

    public static String createAasIdFromResourceId(UUID resourceId) {
        return AAS_ID_PREFIX + resourceId;
    }
}
