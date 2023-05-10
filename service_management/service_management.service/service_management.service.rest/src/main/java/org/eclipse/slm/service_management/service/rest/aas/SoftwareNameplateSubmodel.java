package org.eclipse.slm.service_management.service.rest.aas;

import org.eclipse.basyx.aas.metamodel.exception.MetamodelConstructionException;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IMultiLanguageProperty;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.facade.SubmodelElementMapCollectionConverter;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.identifier.Identifier;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.LangString;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.LangStrings;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.File;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.MultiLanguageProperty;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;

import java.util.Collections;
import java.util.Map;

public class SoftwareNameplateSubmodel extends Submodel {
    public static final String MANUFACTURERNAMEID = "ManufacturerName";
    public static final String MANUFACTURERPRODUCTDESIGNATIONID = "ManufacturerProductDesignation";
    private static final String MANUFACTURERPRODUCTDESCRIPTIONID = "ManufacturerProductDescription" ;
    public static final String MANUFACTURERPRODUCTFAMILYID = "ManufacturerProductFamily";
    public static final String COMPANYLOGOID = "CompanyLogo";
    public static final String VERSIONID = "Version";
    public static final String INSTALLERTYPEID = "InstallerType";
    public static final String SERIALNUMBERID = "SerialNumber";
    public static final Reference SEMANTICID = new Reference(Collections.singletonList(new Key(KeyElements.CONCEPTDESCRIPTION, false, "https://admin-shell.io/zvei/nameplate/1/0/NameplateSoftware", KeyType.IRI)));
    public static final String SUBMODELID = "SoftwareNameplate";

    private SoftwareNameplateSubmodel() {
    }

    /**
     * Constructor with default idShort
     *
     * @param identifier
     * @param manufacturerName
     * @param manufacturerProductDesignation
     * @param manufacturerProductFamily
     */
    public SoftwareNameplateSubmodel(Identifier identifier, MultiLanguageProperty manufacturerName, MultiLanguageProperty manufacturerProductDesignation, MultiLanguageProperty manufacturerProductFamily) {
        this(SUBMODELID, identifier, manufacturerName, manufacturerProductDesignation, manufacturerProductFamily);
    }

    /**
     * Constructor with default idShort
     *
     * @param identifier
     * @param manufacturerName
     * @param manufacturerProductDesignation
     * @param manufacturerProductFamily
     */
    public SoftwareNameplateSubmodel(Identifier identifier, LangString manufacturerName, LangString manufacturerProductDesignation, LangString manufacturerProductFamily) {
        this(SUBMODELID, identifier, manufacturerName, manufacturerProductDesignation, manufacturerProductFamily);
    }

    /**
     * Constructor with mandatory attributes
     *
     * @param idShort
     * @param identifier
     * @param manufacturerName
     * @param manufacturerProductDesignation
     * @param manufacturerProductFamily
     */
    public SoftwareNameplateSubmodel(String idShort, Identifier identifier, MultiLanguageProperty manufacturerName, MultiLanguageProperty manufacturerProductDesignation, MultiLanguageProperty manufacturerProductFamily) {
        super(idShort, identifier);
        setSemanticId(SEMANTICID);
        setManufacturerName(manufacturerName);
        setManufacturerProductDesignation(manufacturerProductDesignation);
        setManufacturerProductFamily(manufacturerProductFamily);
    }

    /**
     * Constructor with mandatory attributes
     *
     * @param idShort
     * @param identifier
     * @param manufacturerName
     * @param manufacturerProductDesignation
     * @param manufacturerProductFamily
     */
    public SoftwareNameplateSubmodel(String idShort, Identifier identifier, LangString manufacturerName, LangString manufacturerProductDesignation, LangString manufacturerProductFamily) {
        super(idShort, identifier);
        setSemanticId(SEMANTICID);
        setManufacturerName(manufacturerName);
        setManufacturerProductDesignation(manufacturerProductDesignation);
        setManufacturerProductFamily(manufacturerProductFamily);
    }

    /**
     * Creates a DigitalNameplateSubmodel object from a map
     *
     * @param obj
     *            a DigitalNameplateSubmodel SMC object as raw map
     * @return a DigitalNameplateSubmodel SMC object, that behaves like a facade for
     *         the given map
     */
    public static SoftwareNameplateSubmodel createAsFacade(Map<String, Object> obj) {
        if (obj == null) {
            return null;
        }

        if (!isValid(obj)) {
            throw new MetamodelConstructionException(SoftwareNameplateSubmodel.class, obj);
        }

        SoftwareNameplateSubmodel ret = new SoftwareNameplateSubmodel();
        ret.setMap(SubmodelElementMapCollectionConverter.mapToSM(obj));
        return ret;
    }

    /**
     * Creates a DigitalNameplateSubmodel object from a map without validation
     *
     * @param obj
     *            a DigitalNameplateSubmodel SMC object as raw map
     * @return a DigitalNameplateSubmodel SMC object, that behaves like a facade for
     *         the given map
     */
    private static SoftwareNameplateSubmodel createAsFacadeNonStrict(Map<String, Object> obj) {
        if (obj == null) {
            return null;
        }

        SoftwareNameplateSubmodel ret = new SoftwareNameplateSubmodel();
        ret.setMap(SubmodelElementMapCollectionConverter.mapToSM(obj));
        return ret;
    }

    /**
     * Check whether all mandatory elements for DigitalNameplateSubmodel exist in
     * the map
     *
     * @param obj
     *
     * @return true/false
     */
    @SuppressWarnings("unchecked")
    public static boolean isValid(Map<String, Object> obj) {
        SoftwareNameplateSubmodel submodel = createAsFacadeNonStrict(obj);

        return Submodel.isValid(obj) && MultiLanguageProperty.isValid((Map<String, Object>) submodel.getManufacturerName()) && MultiLanguageProperty.isValid((Map<String, Object>) submodel.getManufacturerProductDesignation())
                && MultiLanguageProperty.isValid((Map<String, Object>) submodel.getManufacturerProductFamily());
    }

    /**
     * Sets manufacturerName legally valid designation of the natural or judicial
     * person which is directly responsible for the design, production, packaging
     * and labeling of a product in respect to its being brought into circulation
     * Note: mandatory property according to EU Machine Directive 2006/42/EC.
     *
     * @param manufacturerName
     *            {@link MultiLanguageProperty}
     */
    public void setManufacturerName(MultiLanguageProperty manufacturerName) {
        addSubmodelElement(manufacturerName);
    }

    /**
     * Sets manufacturerName legally valid designation of the natural or judicial
     * person which is directly responsible for the design, production, packaging
     * and labeling of a product in respect to its being brought into circulation
     * Note: mandatory property according to EU Machine Directive 2006/42/EC.
     *
     * @param manufacturerName
     *            {@link LangString}
     */
    public void setManufacturerName(LangString manufacturerName) {
        MultiLanguageProperty manufacturerNameProp = new MultiLanguageProperty(MANUFACTURERNAMEID);
        manufacturerNameProp.setSemanticId(new Reference(new Key(KeyElements.CONCEPTDESCRIPTION, false, "0173-1#02-AAO677#002", IdentifierType.IRDI)));
        manufacturerNameProp.setValue(new LangStrings(manufacturerName));
        setManufacturerName(manufacturerNameProp);
    }

    /**
     *
     * Gets manufacturerName legally valid designation of the natural or judicial
     * person which is directly responsible for the design, production, packaging
     * and labeling of a product in respect to its being brought into circulation
     * Note: mandatory property according to EU Machine Directive 2006/42/EC.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public IMultiLanguageProperty getManufacturerName() {
        return MultiLanguageProperty.createAsFacade((Map<String, Object>) getSubmodelElement(MANUFACTURERNAMEID));
    }

    /**
     * Sets Short description of the product (short text) Note: mandatory property
     * according to EU Machine Directive 2006/42/EC.
     *
     * @param manufacturerProductDesignation
     *            {@link MultiLanguageProperty}
     */
    public void setManufacturerProductDesignation(MultiLanguageProperty manufacturerProductDesignation) {
        addSubmodelElement(manufacturerProductDesignation);
    }

    /**
     * Sets Short description of the product (short text) Note: mandatory property
     * according to EU Machine Directive 2006/42/EC.
     *
     * @param manufacturerProductDesignation
     *            {@link LangString}
     */
    public void setManufacturerProductDesignation(LangString manufacturerProductDesignation) {
        MultiLanguageProperty manufacturerProductDesignationProp = new MultiLanguageProperty(MANUFACTURERPRODUCTDESIGNATIONID);
        manufacturerProductDesignationProp.setSemanticId(new Reference(new Key(KeyElements.CONCEPTDESCRIPTION, false, "0173-1#02-AAW338#001", IdentifierType.IRDI)));
        manufacturerProductDesignationProp.setValue(new LangStrings(manufacturerProductDesignation));
        setManufacturerProductDesignation(manufacturerProductDesignationProp);
    }

    /**
     * Gets Short description of the product (short text) Note: mandatory property
     * according to EU Machine Directive 2006/42/EC.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public IMultiLanguageProperty getManufacturerProductDesignation() {
        return MultiLanguageProperty.createAsFacade((Map<String, Object>) getSubmodelElement(MANUFACTURERPRODUCTDESIGNATIONID));
    }

    /**
     * Sets description of the product, its technical features and implementation if needed (long text)
     *
     * @param manufacturerProductDescription
     *            {@link MultiLanguageProperty}
     */
    public void setManufacturerProductDescription(MultiLanguageProperty manufacturerProductDescription) {
        addSubmodelElement(manufacturerProductDescription);
    }

    /**
     * Sets description of the product, its technical features and implementation if needed (long text)
     *
     * @param manufacturerProductDescription
     *            {@link LangString}
     */
    public void setManufacturerProductDescription(LangString manufacturerProductDescription) {
        MultiLanguageProperty manufacturerProductDesignationProp = new MultiLanguageProperty(MANUFACTURERPRODUCTDESCRIPTIONID);
        manufacturerProductDesignationProp.setSemanticId(new Reference(new Key(KeyElements.CONCEPTDESCRIPTION, false, "0173-1#02-AAU734#001", IdentifierType.IRDI)));
        manufacturerProductDesignationProp.setValue(new LangStrings(manufacturerProductDescription));
        setManufacturerProductDescription(manufacturerProductDesignationProp);
    }

    /**
     * Gets description of the product, its technical features and implementation if needed (long text)
     */
    @SuppressWarnings("unchecked")
    public IMultiLanguageProperty getManufacturerProductDescription() {
        return MultiLanguageProperty.createAsFacade((Map<String, Object>) getSubmodelElement(MANUFACTURERPRODUCTDESCRIPTIONID));
    }

    /**
     * Sets 2nd level of a 3 level manufacturer specific product hierarchy Note:
     * mandatory property according to EU Machine Directive 2006/42/EC.
     *
     * @param manufacturerProductFamily
     *            {@link MultiLanguageProperty}
     */
    public void setManufacturerProductFamily(MultiLanguageProperty manufacturerProductFamily) {
        addSubmodelElement(manufacturerProductFamily);
    }

    /**
     * Sets 2nd level of a 3 level manufacturer specific product hierarchy Note:
     * mandatory property according to EU Machine Directive 2006/42/EC.
     *
     * @param manufacturerProductFamily
     *            {@link LangString}
     */
    public void setManufacturerProductFamily(LangString manufacturerProductFamily) {
        MultiLanguageProperty manufacturerProductFamilyProp = new MultiLanguageProperty(MANUFACTURERPRODUCTFAMILYID);
        manufacturerProductFamilyProp.setSemanticId(new Reference(new Key(KeyElements.CONCEPTDESCRIPTION, false, "0173-1#02-AAU731#001", IdentifierType.IRDI)));
        manufacturerProductFamilyProp.setValue(new LangStrings(manufacturerProductFamily));
        setManufacturerProductFamily(manufacturerProductFamilyProp);
    }

    /**
     * Gets 2nd level of a 3 level manufacturer specific product hierarchy Note:
     * mandatory property according to EU Machine Directive 2006/42/EC.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public IMultiLanguageProperty getManufacturerProductFamily() {
        return MultiLanguageProperty.createAsFacade((Map<String, Object>) getSubmodelElement(MANUFACTURERPRODUCTFAMILYID));
    }

    public void setCompanyLogo(File companyLogo) {
        addSubmodelElement(companyLogo);
    }

    public void setCompanyLogo(String fileUrl) {
        File companyLogoFile = new File("image/png");
        companyLogoFile.setIdShort(COMPANYLOGOID);
        companyLogoFile.setValue(fileUrl);
        companyLogoFile.setSemanticId(new Reference(new Key(KeyElements.CONCEPTDESCRIPTION, false, "https://www.hsu-hh.de/aut/aas/companylogo", IdentifierType.IRDI)));
        setCompanyLogo(companyLogoFile);
    }

    @SuppressWarnings("unchecked")
    public File getCompanyLogo() {
        return File.createAsFacade((Map<String, Object>) getSubmodelElement(COMPANYLOGOID));
    }

    /**
     * Sets unique combination of numbers and letters used to identify the device
     * once it has been manufactured
     *
     * @param serialNumber
     */
    public void setSerialNumber(Property serialNumber) {
        addSubmodelElement(serialNumber);
    }

    /**
     * Sets unique combination of numbers and letters used to identify the device
     * once it has been manufactured
     *
     * @param serialNumber
     */
    public void setSerialNumber(String serialNumber) {
        Property serialNumberProp = new Property(SERIALNUMBERID, ValueType.String);
        serialNumberProp.setSemanticId(new Reference(new Key(KeyElements.CONCEPTDESCRIPTION, false, "0173-1#02-AAM556#002", IdentifierType.IRDI)));
        serialNumberProp.setValue(serialNumber);
        setSerialNumber(serialNumberProp);
    }

    /**
     * Gets unique combination of numbers and letters used to identify the device
     * once it has been manufactured
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public IProperty getSerialNumber() {
        return Property.createAsFacade((Map<String, Object>) getSubmodelElement(SERIALNUMBERID));
    }

    /**
     * Sets the complete version information consisting of Major Version, Minor Version, Revision and Build Number
     *
     * @param version
     */
    public void setVersion(Property version) {
        addSubmodelElement(version);
    }

    /**
     * Sets the complete version information consisting of Major Version, Minor Version, Revision and Build Number
     *
     * @param version
     */
    public void setVersion(String version) {
        Property versionProp = new Property(VERSIONID, ValueType.String);
        versionProp.setSemanticId(new Reference(new Key(KeyElements.CONCEPTDESCRIPTION, false, "https://admin-shell.io/zvei/nameplate/1/0/NameplateSoftware/Version", IdentifierType.IRDI)));
        versionProp.setValue(version);
        setVersion(versionProp);
    }

    /**
     * Gets the complete version information consisting of Major Version, Minor Version, Revision and Build Number
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public IProperty getVersion() {
        return Property.createAsFacade((Map<String, Object>) getSubmodelElement(VERSIONID));
    }

    /**
     * Sets the type of installation package
     */
    public void setInstallerType(Property installerType) {
        addSubmodelElement(installerType);
    }

    /**
     * Sets the type of installation package
     */
    public void setInstallerType(String installerType) {
        Property installerTypeProp = new Property(INSTALLERTYPEID, ValueType.String);
        installerTypeProp.setSemanticId(new Reference(new Key(KeyElements.CONCEPTDESCRIPTION, false, "https://admin-shell.io/zvei/nameplate/1/0/NameplateSoftware/InstallerType", IdentifierType.IRDI)));
        installerTypeProp.setValue(installerType);
        setInstallerType(installerTypeProp);
    }

    /**
     * Gets the type of installation package
     */
    @SuppressWarnings("unchecked")
    public IProperty getInstallerType() {
        return Property.createAsFacade((Map<String, Object>) getSubmodelElement(INSTALLERTYPEID));
    }
}
