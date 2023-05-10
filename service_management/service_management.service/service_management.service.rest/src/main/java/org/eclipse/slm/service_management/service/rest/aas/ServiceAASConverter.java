package org.eclipse.slm.service_management.service.rest.aas;

import org.eclipse.slm.service_management.model.offerings.ServiceCategory;
import org.eclipse.slm.service_management.model.offerings.ServiceOffering;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.qualifier.haskind.ModelingKind;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.identifier.Identifier;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.LangString;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.LangStrings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ServiceAASConverter {

    private static final String DELIMITER = "-";
    private static final String SERVICE_OFFERING_VERSION_ASSET = "ServiceOfferingVersionAsset" ;
    private static final String SERVICE_OFFERING_VERSION_SM = "ServiceOfferingVersionSM";
    private static String SERVICEMANAGEMENT_URL;

    @Value("${service-management.url}")
    public void setServiceManagementPath(String serviceManagementUrl) {
        ServiceAASConverter.SERVICEMANAGEMENT_URL = serviceManagementUrl;
    }

    public static AssetAdministrationShell getAASFromServiceOfferingVersion(ServiceOfferingVersion serviceOfferingVersion) {
        AssetAdministrationShell shell = new AssetAdministrationShell();
        shell.setIdentification(getAASIdentifierFromServiceOfferingVersionUUID(serviceOfferingVersion.getId()));
        ServiceOffering serviceOffering = serviceOfferingVersion.getServiceOffering();
        shell.setIdShort(replaceInvalidShortIdChars(serviceOffering.getName() + "_" + serviceOfferingVersion.getVersion()));
        shell.setDescription(new LangStrings("de", serviceOffering.getShortDescription()));
        Asset asset = new Asset();
        asset.setIdentification(getAssetIdentifierFromServiceOfferingVersionUUID(serviceOfferingVersion.getId()));
        asset.setIdShort(replaceInvalidShortIdChars(serviceOffering.getName() + "_" +serviceOfferingVersion.getVersion() + "_Asset"));
        asset.setAssetKind(AssetKind.TYPE);
        shell.setAsset(asset);
        return shell;
    }

    public static Submodel getSoftwareNameplateSMForServiceOfferingVersion(ServiceOfferingVersion serviceOfferingVersion) {
        ServiceOffering serviceOffering = serviceOfferingVersion.getServiceOffering();
        ServiceVendor serviceVendor = serviceOffering.getServiceVendor();
        ServiceCategory serviceCategory = serviceOffering.getServiceCategory();
        Identifier identifier = getSubmodelIdentifierFromServiceOfferingVersionId(serviceOfferingVersion.getId());
        LangString manufacturerName = new LangString("de", serviceVendor.getName());
        LangString manufacturerProductDesignation = new LangString("de", serviceOffering.getName());
        LangString manufacturerProductFamily = new LangString("de", serviceCategory.getName());
        LangString manufacturerProductDescription = new LangString("de", serviceOffering.getDescription());
        String logoUrl = SERVICEMANAGEMENT_URL + "/services/vendors/" + serviceVendor.getId() + "/logo";
        SoftwareNameplateSubmodel sm = new SoftwareNameplateSubmodel(identifier, manufacturerName, manufacturerProductDesignation, manufacturerProductFamily);
        sm.setManufacturerProductDescription(manufacturerProductDescription);
        sm.setVersion(serviceOfferingVersion.getVersion());
        sm.setInstallerType(serviceOfferingVersion.getDeploymentType().name());
        sm.setCompanyLogo(logoUrl);
        sm.setKind(ModelingKind.TEMPLATE);
        return sm;
    }

    public static Identifier getAASIdentifierFromServiceOfferingVersionUUID(UUID uuid) {
        return new CustomId(uuid.toString());
    }

    private static IIdentifier getAssetIdentifierFromServiceOfferingVersionUUID(UUID uuid) {
        return new CustomId(SERVICE_OFFERING_VERSION_ASSET + DELIMITER + uuid);
    }

    public static Identifier getSubmodelIdentifierFromServiceOfferingVersionId(UUID uuid) {
        return new CustomId(SERVICE_OFFERING_VERSION_SM + DELIMITER + uuid);
    }

    public static String replaceInvalidShortIdChars(String invalidString) {
        return invalidString.replace(" ", "").replace(".", "_");
    }

}
