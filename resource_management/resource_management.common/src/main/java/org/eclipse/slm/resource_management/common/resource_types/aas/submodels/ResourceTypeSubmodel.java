package org.eclipse.slm.resource_management.common.resource_types.aas.submodels;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.eclipse.slm.resource_management.common.resources.ResourceType;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ResourceTypeSubmodel extends DefaultSubmodel {
    public static final String SUBMODEL_ID_SHORT = "ResourceType";
    public static final String SEMANTIC_ID_VALUE = "https://eclipse.dev/slm/aas/sm/ResourceType";
    public static final Reference SEMANTIC_ID = new DefaultReference.Builder()
            .type(ReferenceTypes.EXTERNAL_REFERENCE)
            .keys(new DefaultKey.Builder()
                    .type(KeyTypes.CONCEPT_DESCRIPTION)
                    .value(SEMANTIC_ID_VALUE).build()).build();

    private final ResourceType resourceType;

    public ResourceTypeSubmodel(ResourceType resourceType) {
        this.resourceType = resourceType;

        this.setId(SUBMODEL_ID_SHORT + "-" + resourceType.getManufacturerName() + "-" + resourceType.getTypeName());
        this.setIdShort(SUBMODEL_ID_SHORT + "-" + resourceType.getManufacturerName() + "-" + resourceType.getTypeName());
        this.setDisplayName(
                List.of(
                        new DefaultLangStringNameType.Builder()
                                .language("en")
                                .text(resourceType.getManufacturerName() + " | " + resourceType.getTypeName())
                                .build()
                ));
        this.setSemanticId(SEMANTIC_ID);

        addManufacturerNameProperty();
        addTypeNameProperty();
        addInstancesCollection();
        addSoftwareNameplateCollection();
    }

    public void addManufacturerNameProperty() {
        var prop = new DefaultProperty.Builder()
                .idShort("ManufacturerName")
                .valueType(DataTypeDefXsd.STRING)
                .value(this.resourceType.getManufacturerName())
                .build();
        this.submodelElements.add(prop);
    }

    public void addTypeNameProperty() {
        var prop = new DefaultProperty.Builder()
                .idShort("TypeName")
                .valueType(DataTypeDefXsd.STRING)
                .value(this.resourceType.getTypeName())
                .build();
        this.submodelElements.add(prop);
    }

    public void addInstancesCollection() {
        var instanceSubmodelElements = new ArrayList<SubmodelElement>();

        for (var resourceInstanceId : resourceType.getResourceInstanceIds()) {
            var resourceInstanceAasId = ResourceAas.createAasIdFromResourceId(resourceInstanceId);
            var resourceInstanceRefElement = new DefaultReferenceElement.Builder()
                    .idShort(resourceInstanceAasId)
                    .value(
                        new DefaultReference.Builder()
                            .type(ReferenceTypes.MODEL_REFERENCE)
                            .keys(List.of(
                                    new DefaultKey.Builder()
                                            .type(KeyTypes.ASSET_ADMINISTRATION_SHELL)
                                            .value(resourceInstanceAasId)
                                            .build()
                            )
                        ).build()
                    )
                    .build();
            instanceSubmodelElements.add(resourceInstanceRefElement);
        }

        var smeCollection = new DefaultSubmodelElementCollection.Builder()
                .idShort("Instances")
                .value(instanceSubmodelElements)
                .build();
        this.submodelElements.add(smeCollection);
    }

    public void addSoftwareNameplateCollection() {
        var instanceSubmodelElements = new ArrayList<SubmodelElement>();

        for (var softwareNameplateId : resourceType.getSoftwareNameplateIds()) {
            var idShortEncoded = Base64.getEncoder().encodeToString(softwareNameplateId.getBytes());
            var resourceInstanceRefElement = new DefaultReferenceElement.Builder()
                    .idShort(idShortEncoded)
                    .displayName(
                        List.of(
                            new DefaultLangStringNameType.Builder()
                                    .language("en")
                                    .text("Software Nameplate")
                                    .build()
                        )
                    )
                    .value(
                            new DefaultReference.Builder()
                                    .type(ReferenceTypes.MODEL_REFERENCE)
                                    .keys(List.of(
                                            new DefaultKey.Builder()
                                                    .type(KeyTypes.SUBMODEL)
                                                    .value(softwareNameplateId)
                                                    .build(),
                                            new DefaultKey.Builder()
                                                    .type(KeyTypes.SUBMODEL_ELEMENT)
                                                    .value("SoftwareNameplateType")
                                                    .build()
                                            )
                                    ).build()
                    )
                    .build();
            instanceSubmodelElements.add(resourceInstanceRefElement);
        }

        var smeCollection = new DefaultSubmodelElementCollection.Builder()
                .idShort("SoftwareNameplates")
                .value(instanceSubmodelElements)
                .build();
        this.submodelElements.add(smeCollection);
    }
}
