package org.eclipse.slm.resource_management.service.rest.aas.resources;

import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.slm.resource_management.model.resource.BasicResource;

import java.util.UUID;

public class ResourceAAS extends DefaultAssetAdministrationShell {

    public static final String AAS_ID_PREFIX = "Resource_";

    public ResourceAAS(BasicResource resource) {
        this.id = AAS_ID_PREFIX + resource.getId();
        this.idShort = AAS_ID_PREFIX + resource.getId();
    }

    public static String createAasIdFromResourceId(UUID resourceId) {
        return AAS_ID_PREFIX + resourceId;
    }
}
