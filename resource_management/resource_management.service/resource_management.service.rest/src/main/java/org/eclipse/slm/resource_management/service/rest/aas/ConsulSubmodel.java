package org.eclipse.slm.resource_management.service.rest.aas;

import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;

import java.util.Collections;
import java.util.Map;

public class ConsulSubmodel extends Submodel {
    public static final String SUBMODELID = "ConsulInfo";
    public static final String IPID = "IP";
    public static final String HOSTNAMEID = "HostName";
    public static final String SEMANTICID_IRI = "https://fab-os.org/ConsulInfo";
    public static final Reference SEMANTICID = new Reference(Collections.singletonList(new Key(KeyElements.CONCEPTDESCRIPTION, false, SEMANTICID_IRI, KeyType.IRI)));

    public ConsulSubmodel(IIdentifier identifier) {
        this(identifier, null, null);
    }

    public ConsulSubmodel(IIdentifier identifier, String ip, String hostName) {
        this(SUBMODELID, identifier, ip, hostName);
    }

    public ConsulSubmodel(String idShort, IIdentifier identifier, String ip, String hostName) {
        super(idShort, identifier);
        setSemanticId(SEMANTICID);
        setIP(ip);
        setHostName(hostName);
    }

    public void setIP(Property ip) {
        addSubmodelElement(ip);
    }

    public void setIP(String ip) {
        Property ipProp = new Property(IPID, ValueType.String);
        ipProp.setSemanticId(new Reference(Collections.singletonList(new Key(KeyElements.PROPERTY, false, SEMANTICID_IRI + "/" + IPID, KeyType.IRI))));
        ipProp.setValue(ip);
        setIP(ipProp);
    }

    @SuppressWarnings("unchecked")
    public IProperty getIP() {
        return Property.createAsFacade((Map<String, Object>) getSubmodelElement(IPID));
    }

    public void setHostName(Property hostName) {
        addSubmodelElement(hostName);
    }

    public void setHostName(String hostName) {
        Property hostNameProp = new Property(HOSTNAMEID, ValueType.String);
        hostNameProp.setSemanticId(new Reference(Collections.singletonList(new Key(KeyElements.PROPERTY, false, SEMANTICID_IRI + "/" + HOSTNAMEID, KeyType.IRI))));
        hostNameProp.setValue(hostName);
        setHostName(hostNameProp);
    }

    @SuppressWarnings("unchecked")
    public IProperty getHostName() {
        return Property.createAsFacade((Map<String, Object>) getSubmodelElement(HOSTNAMEID));
    }
}
