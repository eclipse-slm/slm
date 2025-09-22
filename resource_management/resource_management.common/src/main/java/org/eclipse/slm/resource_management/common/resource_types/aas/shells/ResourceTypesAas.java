package org.eclipse.slm.resource_management.common.resource_types.aas.shells;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.slm.resource_management.common.resource_types.ResourceTypesManager;
import org.eclipse.slm.resource_management.common.resource_types.aas.submodels.ResourceTypeSubmodel;

import java.util.ArrayList;
import java.util.List;

public class ResourceTypesAas extends DefaultAssetAdministrationShell {

    public static final String RESOURCE_TYPE_AAS_ID = "ResourceTypes";

    private final ResourceTypesManager resourceTypesManager;

    public ResourceTypesAas(ResourceTypesManager resourceTypesManager) {
        this.resourceTypesManager = resourceTypesManager;
        this.setId("ResourceTypes");
        this.setIdShort("ResourceTypes");
        this.setDisplayName(
                List.of(
                        new DefaultLangStringNameType.Builder()
                                .language("en")
                                .text("Resource Types")
                                .build()
                ));
        this.setDescription(
                List.of(
                        new DefaultLangStringTextType.Builder()
                                .language("en")
                                .text("AAS that contains alls submodels related to resource types")
                                .build()
                ));
        this.setSubmodels(generateSubmodelReferences());
        this.setAssetInformation(generateAssetInformation());
    }

    private AssetInformation generateAssetInformation() {
        var assetInformation = new DefaultAssetInformation.Builder()
                .assetKind(AssetKind.INSTANCE)
                .globalAssetId("ResourceTypes")
                .build();
        return assetInformation;
    }

    private List<Reference> generateSubmodelReferences() {
        var submodelReferences = new ArrayList<Reference>();
        var resourceTypes = this.resourceTypesManager.getResourceTypes();

        for (var resourceType : resourceTypes) {
            var resourceTypeSubmodel = new ResourceTypeSubmodel(resourceType);

            var submodelReference = new DefaultReference.Builder()
                    .type(ReferenceTypes.MODEL_REFERENCE)
                    .keys(new DefaultKey.Builder()
                            .type(KeyTypes.SUBMODEL)
                            .value(resourceTypeSubmodel.getId())
                            .build())
                    .build();
            submodelReferences.add(submodelReference);
        }

        return submodelReferences;
    }

}
