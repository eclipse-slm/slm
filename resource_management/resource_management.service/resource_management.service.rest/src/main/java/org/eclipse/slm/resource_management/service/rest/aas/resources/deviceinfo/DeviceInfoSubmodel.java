package org.eclipse.slm.resource_management.service.rest.aas.resources.deviceinfo;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.slm.resource_management.model.resource.BasicResource;

public class DeviceInfoSubmodel extends DefaultSubmodel {
    public static final String SUBMODEL_ID_SHORT = "DeviceInfo";
    public static final String SEMANTIC_ID_VALUE = "https://eclipse.dev/slm/aas/sm/DeviceInfo";
    public static final Reference SEMANTIC_ID = new DefaultReference.Builder()
            .type(ReferenceTypes.EXTERNAL_REFERENCE)
            .keys(new DefaultKey.Builder()
                    .type(KeyTypes.CONCEPT_DESCRIPTION)
                    .value(SEMANTIC_ID_VALUE).build()).build();

    private BasicResource resource;

    public DeviceInfoSubmodel(BasicResource resource) {
        this.resource = resource;

        this.setId(SUBMODEL_ID_SHORT + "-" + resource.getId());
        this.setIdShort(SUBMODEL_ID_SHORT);
        this.setSemanticId(SEMANTIC_ID);

        addIdProperty(resource);
        addIpProperty(resource);
        addHostnameProperty(resource);
        addAssetIdProperty(resource);
        addFirmwareVersionProperty(resource);
    }

    public void addIdProperty(BasicResource resource) {
        var idProp = new DefaultProperty.Builder()
                .idShort("Id")
                .valueType(DataTypeDefXsd.STRING)
                .value(resource.getId().toString()).build();
        this.submodelElements.add(idProp);
    }

    public void addIpProperty(BasicResource resource) {
        var ipProp = new DefaultProperty.Builder()
                .idShort("IP")
                .valueType(DataTypeDefXsd.STRING)
                .value(resource.getIp()).build();
        this.submodelElements.add(ipProp);
    }

    public void addHostnameProperty(BasicResource resource) {
        var hostnameProp = new DefaultProperty.Builder()
                .idShort("Hostname")
                .valueType(DataTypeDefXsd.STRING)
                .value(resource.getHostname()).build();
        this.submodelElements.add(hostnameProp);
    }

    public void addAssetIdProperty(BasicResource resource) {
        var hostnameProp = new DefaultProperty.Builder()
                .idShort("AssetId")
                .valueType(DataTypeDefXsd.STRING)
                .value(resource.getAssetId()).build();
        this.submodelElements.add(hostnameProp);
    }

    public void addFirmwareVersionProperty(BasicResource resource) {
        var hostnameProp = new DefaultProperty.Builder()
                .idShort("FirmwareVersion")
                .valueType(DataTypeDefXsd.STRING)
                .value(resource.getFirmwareVersion()).build();
        this.submodelElements.add(hostnameProp);
    }
}
