package org.eclipse.slm.resource_management.service.rest.aas.resources.consul;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;

public class ConsulSubmodel extends DefaultSubmodel {
    public static final String SUBMODELID = "ConsulInfo";
    public static final String IPID = "IP";
    public static final String HOSTNAMEID = "HostName";
    public static final Reference SEMANTIC_ID = new DefaultReference.Builder().keys(
            new DefaultKey.Builder()
                    .type(KeyTypes.CONCEPT_DESCRIPTION)
                    .value("https://fab-os.org/ConsulInfo").build()).build();

    public ConsulSubmodel(String resourceId) {
        this(resourceId, null, null);
    }

    public ConsulSubmodel(String id, String ip, String hostName) {
        this(SUBMODELID, id, ip, hostName);
    }

    public ConsulSubmodel(String idShort, String id, String ip, String hostName) {
        super();
        this.id = id;
        this.idShort = idShort;
        setSemanticId(SEMANTIC_ID);
        addIpProperty(ip);
        addHostnameProperty(hostName);
    }

    public void addIpProperty(String ip) {
        var ipProp = new DefaultProperty.Builder()
                .idShort(IPID)
                .valueType(DataTypeDefXsd.STRING)
                .value(ip).build();
        this.submodelElements.add(ipProp);
    }

    public void addHostnameProperty(String hostName) {
        var hostnameProp = new DefaultProperty.Builder()
                .idShort(HOSTNAMEID)
                .valueType(DataTypeDefXsd.STRING)
                .value(hostName).build();
        this.submodelElements.add(hostnameProp);
    }
}
