package org.eclipse.slm.resource_management.model.resource;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;

import java.util.UUID;

public class ResourceAas extends DefaultAssetAdministrationShell {

    public static final String AAS_ID_PREFIX = "Resource_";

    public static final Reference RESOURCE_ID_SEMANTIC_ID = new DefaultReference.Builder()
            .type(ReferenceTypes.EXTERNAL_REFERENCE)
            .keys(
                new DefaultKey.Builder()
                        .type(KeyTypes.CONCEPT_DESCRIPTION)
                        .value("http://eclipse.dev/slm/aas/ResourceId").build())
                .build();

    public ResourceAas(BasicResource resource) {
        this.id = AAS_ID_PREFIX + resource.getId();
        this.idShort = AAS_ID_PREFIX + resource.getId();

        this.assetInformation = new DefaultAssetInformation.Builder()
                .assetKind(AssetKind.INSTANCE)
                .specificAssetIds(new DefaultSpecificAssetId.Builder()
                        .name("Eclipse SLM Resource Id")
                        .value(resource.getId().toString())
                        .semanticId(RESOURCE_ID_SEMANTIC_ID)
                        .build())
                .build();
    }

    public static String createAasIdFromResourceId(UUID resourceId) {
        return AAS_ID_PREFIX + resourceId;
    }

    public static String getResourceIdFromAasId(String aasId) {
        return aasId.replace(AAS_ID_PREFIX, "");
    }
}
