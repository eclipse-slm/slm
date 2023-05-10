package org.eclipse.slm.service_management.service.rest.service_offerings;

import org.eclipse.slm.service_management.model.offerings.ServiceRequirementLogic;
import org.eclipse.slm.service_management.model.offerings.RequirementLogicType;
import org.eclipse.slm.service_management.model.offerings.RequirementProperty;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirement;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.submodel.metamodel.api.reference.IKey;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
public class ServiceOfferingVersionRequirementsHandlerTest {

    static final String PARENT_SEMANTIC_ID = "parentSemanticId";
    static final String SEMANTIC_ID = "semanticId";
    static final String VALUE = "value";

    @Test
    void emptyRequirementFulfilled() {
        ServiceRequirement requirement = new ServiceRequirement();
        boolean fulfilled = ServiceOfferingVersionRequirementsHandler.isRequirementFulfilledBySubmodels(requirement, new ArrayList<>());
        assertThat(fulfilled).isTrue();
    }

    @Test
    void requirementNotFulfilled() {
        ServiceRequirement requirement = createSimpleRequirement();
        boolean fulfilled = ServiceOfferingVersionRequirementsHandler.isRequirementFulfilledBySubmodels(requirement, new ArrayList<>());
        assertThat(fulfilled).isFalse();
    }

    @Test
    void simpleRequirementFulfilled() {
        Submodel submodel = new Submodel();
        submodel.setIdentification(new CustomId("SubmodelId"));
        submodel.setSemanticId(new Reference(Collections.singletonList(new Key(KeyElements.CONCEPTDESCRIPTION, false, PARENT_SEMANTIC_ID, KeyType.IRI))));
        Property property = new Property("idShort", VALUE);
        property.setSemanticId(new Reference(Collections.singletonList(new Key(KeyElements.CONCEPTDESCRIPTION, false, SEMANTIC_ID, KeyType.IRI))));
        submodel.addSubmodelElement(property);
        ServiceRequirement requirement = createSimpleRequirement();
        boolean fulfilled = ServiceOfferingVersionRequirementsHandler.isRequirementFulfilledBySubmodels(requirement, Collections.singletonList(submodel));
        assertThat(fulfilled).isTrue();
    }

    @Test
    void identifyCorrectParentSubmodel() {
        Submodel submodel = new Submodel();
        submodel.setIdentification(new CustomId("SubmodelId"));
        List<IKey> keys = new ArrayList<>();
        keys.add(new Key(KeyElements.CONCEPTDESCRIPTION, false, "WRONG_PARENT_SEMANTIC_ID", KeyType.IRI));
        keys.add(new Key(KeyElements.CONCEPTDESCRIPTION, false, PARENT_SEMANTIC_ID, KeyType.IRI));
        submodel.setSemanticId(new Reference(keys));
        RequirementProperty property = new RequirementProperty();
        property.setParentSubmodelsSemanticIds(Collections.singletonList(PARENT_SEMANTIC_ID));
        boolean isCorrectParentSubmodel = ServiceOfferingVersionRequirementsHandler.isCorrectParentSubmodel(property, submodel);
        assertThat(isCorrectParentSubmodel).isTrue();
    }

    ServiceRequirement createSimpleRequirement() {
        ServiceRequirement requirement = new ServiceRequirement();
        ServiceRequirementLogic logic = new ServiceRequirementLogic();
        logic.setType(RequirementLogicType.ANY);
        RequirementProperty property = new RequirementProperty();
        property.setParentSubmodelsSemanticIds(Collections.singletonList(PARENT_SEMANTIC_ID));
        property.setSemanticId(SEMANTIC_ID);
        property.setName("TestProperty");
        property.setValue(VALUE);
        logic.setProperties(Collections.singletonList(property));
        requirement.setLogics(Collections.singletonList(logic));
        return requirement;
    }

}
