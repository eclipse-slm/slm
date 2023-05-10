package org.eclipse.slm.service_management.service.rest.aas;

import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;

import java.util.Collections;

public class RequirementsSubmodel extends Submodel {
    public static final String SUBMODELID = "Requirements";
    public static final String SEMANTICID_IRI = "https://fab-os.org/Requirements";
    public static final Reference SEMANTICID = new Reference(Collections.singletonList(new Key(KeyElements.CONCEPTDESCRIPTION, false, SEMANTICID_IRI, KeyType.IRI)));

    public RequirementsSubmodel(IIdentifier identifier) {
        super(SUBMODELID, identifier);
        this.setSemanticId(SEMANTICID);
    }
}
