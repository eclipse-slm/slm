package org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.softwarenameplate;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.slm.common.aas.repositories.submodels.SubmodelUtils;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SoftwareNameplateSubmodel extends DefaultSubmodel {
    public static final String SUBMODEL_ID_PREFIX = "SoftwareNameplate";

    public static final String SEMANTIC_ID_VALUE = "https://admin-shell.io/idta/SoftwareNameplate/1/0";

    public static final Reference SEMANTIC_ID = new DefaultReference.Builder().keys(
            new DefaultKey.Builder()
                    .type(KeyTypes.CONCEPT_DESCRIPTION)
                    .value(SEMANTIC_ID_VALUE).build()).build();

    public SoftwareNameplateSubmodel(ServiceOfferingVersion serviceOfferingVersion)  {
        super();
        this.id = SoftwareNameplateSubmodel.getSubmodelIdForServiceOfferingVersionId(serviceOfferingVersion.getId());
        this.idShort = SoftwareNameplateSubmodel.getSubmodelIdShortForServiceOfferingVersion(serviceOfferingVersion);
        this.setSemanticId(SoftwareNameplateSubmodel.SEMANTIC_ID);

        var smesSoftwareNameplateType = new ArrayList<SubmodelElement>();
        // Property | URIOfTheProduct
        var propURIOfTheProduct = new DefaultProperty.Builder()
                .idShort("URIOfTheProduct")
                .category(SubmodelUtils.CATEGORY_CONSTANT)
                .valueType(DataTypeDefXsd.STRING)
                .qualifiers(SubmodelUtils.QUALIFIER_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0173-1#02-AAY811#001"))
                .build();
        smesSoftwareNameplateType.add(propURIOfTheProduct);
        // MLP | ManufacturerName
        var mlpManufacturerName = new DefaultMultiLanguageProperty.Builder()
                .idShort("ManufacturerName")
                .category(SubmodelUtils.CATEGORY_PARAMETER)
                .qualifiers(SubmodelUtils.QUALIFIER_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0173-1#02-AAO677#002"))
                .value(new DefaultLangStringTextType.Builder()
                            .language("en")
                            .text(serviceOfferingVersion.getServiceOffering().getServiceVendor().getName())
                            .build())
                .build();
        smesSoftwareNameplateType.add(mlpManufacturerName);
        // MLP | ManufacturerProductDesignation
        var mlpManufacturerProductDesignation = new DefaultMultiLanguageProperty.Builder()
                .idShort("ManufacturerProductDesignation")
                .category(SubmodelUtils.CATEGORY_PARAMETER)
                .qualifiers(SubmodelUtils.QUALIFIER_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0173-1#02-AAW338#001"))
                .build();
        smesSoftwareNameplateType.add(mlpManufacturerProductDesignation);
        // Property | Version
        var propVersion = new DefaultProperty.Builder()
                .idShort("Version")
                .category(SubmodelUtils.CATEGORY_PARAMETER)
                .valueType(DataTypeDefXsd.STRING)
                .qualifiers(SubmodelUtils.QUALIFIER_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/Version"))
                .value(serviceOfferingVersion.getVersion())
                .build();
        smesSoftwareNameplateType.add(propVersion);
        // Property | BuildDate
        var propBuildDate = new DefaultProperty.Builder()
                .idShort("BuildDate")
                .category(SubmodelUtils.CATEGORY_PARAMETER)
                .valueType(DataTypeDefXsd.STRING)
                .qualifiers(SubmodelUtils.QUALIFIER_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/BuildDate"))
                .build();
        smesSoftwareNameplateType.add(propBuildDate);
        // Property | ReleaseDate
        var propReleaseDate = new DefaultProperty.Builder()
                .idShort("ReleaseDate")
                .category(SubmodelUtils.CATEGORY_PARAMETER)
                .valueType(DataTypeDefXsd.STRING)
                .qualifiers(SubmodelUtils.QUALIFIER_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/ReleaseDate"))
                .build();
        smesSoftwareNameplateType.add(propReleaseDate);

        ////// Optional
        // MLP ManufacturerProductDescription

        // MLP ManufacturerProductFamily

        // ManufacturerProductType

        // Property SoftwareType

        // MLP VersionName

        // MLP VersionInfo

        // MLP ReleaseNotes

        // Property InstallationURI

        // Property InstallationFile

        // Property InstallerType

        // Property InstallationChecksum


        var smcSoftwareNameplateType = new DefaultSubmodelElementCollection.Builder()
                .idShort("SoftwareNameplateType")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType"))
                .value(smesSoftwareNameplateType)
                .build();

        this.setSubmodelElements(List.of(smcSoftwareNameplateType));


    }

    public static String getSubmodelIdForServiceOfferingVersionId(UUID serviceOfferingVersionId) {
        return SoftwareNameplateSubmodel.SUBMODEL_ID_PREFIX + "-" + serviceOfferingVersionId;
    }

    public static String getSubmodelIdShortForServiceOfferingVersion(ServiceOfferingVersion serviceOfferingVersion) {
        return SoftwareNameplateSubmodel.SUBMODEL_ID_PREFIX
                + "-" + serviceOfferingVersion.getServiceOffering().getName()
                + "-" + serviceOfferingVersion.getVersion();
    }
}
