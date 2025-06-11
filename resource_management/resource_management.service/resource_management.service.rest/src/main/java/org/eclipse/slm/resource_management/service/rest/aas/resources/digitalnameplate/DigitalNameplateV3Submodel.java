package org.eclipse.slm.resource_management.service.rest.aas.resources.digitalnameplate;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.slm.common.aas.repositories.SubmodelUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DigitalNameplateV3Submodel extends DefaultSubmodel {

    public static final String SUBMODEL_IDSHORT = "Nameplate";
    public static final Reference SEMANTICID = new DefaultReference.Builder()
            .type(ReferenceTypes.EXTERNAL_REFERENCE)
            .keys(Collections.singletonList(
                    new DefaultKey.Builder()
                            .value("https://admin-shell.io/idta/nameplate/3/0/Nameplate")
                            .type(KeyTypes.GLOBAL_REFERENCE)
                            .build()))
            .build();

    private final DigitalNameplateV3 digitalNameplateV3;

    public DigitalNameplateV3Submodel(String submodelId, DigitalNameplateV3 digitalNameplateV3) {
        this.digitalNameplateV3 = digitalNameplateV3;

        this.setId(submodelId);
        this.setIdShort(DigitalNameplateV3Submodel.SUBMODEL_IDSHORT);
        this.setSemanticId(DigitalNameplateV3Submodel.SEMANTICID);

        var submodelElements = new ArrayList<SubmodelElement>();

        submodelElements.add(uriOfTheProductProperty());
        submodelElements.add(manufacturerNameProperty());
        submodelElements.add(manufacturerProductDesignationProperty());
        submodelElements.add(addressInformationProperty());
        if (this.digitalNameplateV3.getManufacturerProductRoot() != null) {
            submodelElements.add(manufacturerProductRootProperty());
        }
        if (this.digitalNameplateV3.getManufacturerProductFamily() != null) {
            submodelElements.add(manufacturerProductFamilyProperty());
        }
        if (this.digitalNameplateV3.getManufacturerProductType() != null) {
            submodelElements.add(manufacturerProductTypeProperty());
        }
        if (this.digitalNameplateV3.getOrderCodeOfManufacturer() != null) {
            submodelElements.add(orderCodeOfManufacturerProperty());
        }
        if (this.digitalNameplateV3.getProductArticleNumberOfManufacturer() != null) {
            submodelElements.add(productArticleNumberOfManufacturerProperty());
        }
        if (this.digitalNameplateV3.getSerialNumber() != null) {
            submodelElements.add(serialNumberProperty());
        }
        if (this.digitalNameplateV3.getYearOfConstruction() != null) {
            submodelElements.add(yearOfConstructionProperty());
        }
        if (this.digitalNameplateV3.getDateOfManufacture() != null) {
            submodelElements.add(dateOfManufactureProperty());
        }
        if (this.digitalNameplateV3.getHardwareVersion() != null) {
            submodelElements.add(hardwareVersionProperty());
        }
        if (this.digitalNameplateV3.getFirmwareVersion() != null) {
            submodelElements.add(firmwareVersionProperty());
        }
        if (this.digitalNameplateV3.getSoftwareVersion() != null) {
            submodelElements.add(softwareVersionProperty());
        }
        if (this.digitalNameplateV3.getCountryOfOrigin() != null) {
            submodelElements.add(countryOfOriginProperty());
        }
        if (this.digitalNameplateV3.getUniqueFacilityIdentifier() != null) {
            submodelElements.add(uniqueFacilityIdentifierProperty());
        }

        this.setSubmodelElements(submodelElements);
    }

    private Property uriOfTheProductProperty() {
        return new DefaultProperty.Builder()
            .idShort("URIOfTheProduct")
            .qualifiers(SubmodelUtils.QUALIFIER_ONE)
            .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABN590#002"))
            .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-ABH173#003"))
            .value(this.digitalNameplateV3.getUriOfTheProduct())
            .valueType(DataTypeDefXsd.STRING)
            .build();
    }

    private SubmodelElement manufacturerNameProperty() {
        return new DefaultMultiLanguageProperty.Builder()
                .idShort("ManufacturerName")
                .qualifiers(SubmodelUtils.QUALIFIER_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABA565#009"))
                .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-AAO677#004"))
                .value(SubmodelUtils.generateLangString("en", this.digitalNameplateV3.getManufacturerName()))
                .build();
    }

    private SubmodelElement manufacturerProductDesignationProperty() {
        return new DefaultMultiLanguageProperty.Builder()
                .idShort("ManufacturerProductDesignation")
                .qualifiers(SubmodelUtils.QUALIFIER_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABA567#009"))
                .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-AAW338#003"))
                .value(SubmodelUtils.generateLangString("en", this.digitalNameplateV3.getManufacturerProductDesignation()))
                .build();
    }

    private SubmodelElement addressInformationProperty() {
        return new DefaultProperty.Builder()
                .idShort("AddressInformation")
                .qualifiers(SubmodelUtils.QUALIFIER_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/AddressInformation"))
                .supplementalSemanticIds(List.of(
                        SubmodelUtils.generateSemanticId("https://admin-shell.io/smt-dropin/smt-dropin-use/1/0"),
                        SubmodelUtils.generateSemanticId("0112/2///61360_7#AAS002#001"),
                        SubmodelUtils.generateSemanticId("0173-1#02-AAQ837#008/0173-1#01-ADR448#008")))
                .value(this.digitalNameplateV3.getAddressInformation())
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }

    private SubmodelElement manufacturerProductRootProperty() {
        return new DefaultMultiLanguageProperty.Builder()
                .idShort("ManufacturerProductRoot")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61360_7#AAS011#001"))
                .supplementalSemanticIds(List.of(SubmodelUtils.generateSemanticId("0173-1#02-AAU732#003")))
                .value(SubmodelUtils.generateLangString("en", this.digitalNameplateV3.getManufacturerProductRoot()))
                .build();
    }

    private SubmodelElement manufacturerProductFamilyProperty() {
        return new DefaultMultiLanguageProperty.Builder()
                .idShort("ManufacturerProductFamily")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABP464#002"))
                .supplementalSemanticIds(List.of(SubmodelUtils.generateSemanticId("0173-1#02-AAU731#003")))
                .value(SubmodelUtils.generateLangString("en", this.digitalNameplateV3.getManufacturerProductFamily()))
                .build();
    }

    private SubmodelElement manufacturerProductTypeProperty() {
        return new DefaultProperty.Builder()
                .idShort("ManufacturerProductType")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABA300#008"))
                .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-AAO057#004"))
                .value(this.digitalNameplateV3.getManufacturerProductType())
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }

    private SubmodelElement orderCodeOfManufacturerProperty() {
        return new DefaultProperty.Builder()
                .idShort("OrderCodeOfManufacturer")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABA950#008"))
                .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-AAO227#004"))
                .value(this.digitalNameplateV3.getOrderCodeOfManufacturer())
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }

    private SubmodelElement productArticleNumberOfManufacturerProperty() {
        return new DefaultProperty.Builder()
                .idShort("ProductArticleNumberOfManufacturer")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABA581#007"))
                .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-AAO676#005"))
                .value(this.digitalNameplateV3.getProductArticleNumberOfManufacturer())
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }

    private SubmodelElement serialNumberProperty() {
        return new DefaultProperty.Builder()
                .idShort("SerialNumber")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABA951#009"))
                .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-AAM556#004"))
                .value(this.digitalNameplateV3.getSerialNumber())
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }

    private SubmodelElement yearOfConstructionProperty() {
        return new DefaultProperty.Builder()
                .idShort("YearOfConstruction")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABP000#002"))
                .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-AAP906#003"))
                .value(this.digitalNameplateV3.getYearOfConstruction())
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }

    private SubmodelElement dateOfManufactureProperty() {
        return new DefaultProperty.Builder()
                .idShort("DateOfManufacture")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABB757#007"))
                .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-AAR972#004"))
                .value(this.digitalNameplateV3.getDateOfManufacture().toString())
                .valueType(DataTypeDefXsd.DATE)
                .build();
    }

    private SubmodelElement hardwareVersionProperty() {
        return new DefaultProperty.Builder()
                .idShort("HardwareVersion")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABA926#008"))
                .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-AAN270#004"))
                .value(this.digitalNameplateV3.getHardwareVersion())
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }

    private SubmodelElement firmwareVersionProperty() {
        return new DefaultProperty.Builder()
                .idShort("FirmwareVersion")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABA302#006"))
                .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-AAN270#004"))
                .value(this.digitalNameplateV3.getFirmwareVersion())
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }

    private SubmodelElement softwareVersionProperty() {
        return new DefaultProperty.Builder()
                .idShort("SoftwareVersion")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABA601#008"))
                .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-AAM737#004"))
                .value(this.digitalNameplateV3.getSoftwareVersion())
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }

    private SubmodelElement countryOfOriginProperty() {
        return new DefaultProperty.Builder()
                .idShort("CountryOfOrigin")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("0112/2///61987#ABP462#001"))
                .supplementalSemanticIds(SubmodelUtils.generateSemanticId("0173-1#02-AAO259#007"))
                .value(this.digitalNameplateV3.getCountryOfOrigin())
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }

    private SubmodelElement uniqueFacilityIdentifierProperty() {
        return new DefaultProperty.Builder()
                .idShort("UniqueFacilityIdentifier")
                .qualifiers(SubmodelUtils.QUALIFIER_ZERO_TO_ONE)
                .semanticId(SubmodelUtils.generateSemanticId("https://admin-shell.io/idta/nameplate/3/0/UniqueFacilityIdentifier"))
                .value(this.digitalNameplateV3.getUniqueFacilityIdentifier())
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }

}

