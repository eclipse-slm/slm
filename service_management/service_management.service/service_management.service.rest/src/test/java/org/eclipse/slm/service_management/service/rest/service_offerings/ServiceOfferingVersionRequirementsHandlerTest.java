package org.eclipse.slm.service_management.service.rest.service_offerings;

import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.slm.common.aas.clients.AasRepositoryClientFactory;
import org.eclipse.slm.common.aas.clients.SubmodelRegistryClientFactory;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirementLogic;
import org.eclipse.slm.service_management.model.offerings.RequirementLogicType;
import org.eclipse.slm.service_management.model.offerings.RequirementProperty;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
public class ServiceOfferingVersionRequirementsHandlerTest {

    public static final String PARENT_SEMANTIC_ID = "parentSemanticId";
    public static final String SEMANTIC_ID = "semanticId";
    public static final String VALUE = "value";

    private ServiceOfferingVersionRequirementsHandler serviceOfferingVersionRequirementsHandler;

    @BeforeEach
    public void ServiceOfferingVersionRequirementsHandlerTest() {
        var aasRepositoryClientFactory = new AasRepositoryClientFactory(null, null);
        var submodelRegistryClientFactory = new SubmodelRegistryClientFactory(null, null);
        this.serviceOfferingVersionRequirementsHandler = new ServiceOfferingVersionRequirementsHandler(aasRepositoryClientFactory, submodelRegistryClientFactory);
    }

    @Test
    void emptyRequirementFulfilled() {
        var requirement = new ServiceRequirement();
        boolean fulfilled = this.serviceOfferingVersionRequirementsHandler
                .isRequirementFulfilledBySubmodels(requirement, new ArrayList<>());
        assertThat(fulfilled).isTrue();
    }

    @Test
    void requirementNotFulfilled() {
        ServiceRequirement requirement = createSimpleRequirement();
        boolean fulfilled = this.serviceOfferingVersionRequirementsHandler
                .isRequirementFulfilledBySubmodels(requirement, new ArrayList<>());
        assertThat(fulfilled).isFalse();
    }

    @Test
    void simpleRequirementFulfilled() {
        var submodel = new DefaultSubmodel();
        submodel.setId("SubmodelId");
        submodel.setSemanticId(new DefaultReference.Builder().keys(
                        new DefaultKey.Builder()
                                .type(KeyTypes.CONCEPT_DESCRIPTION)
                                .value(PARENT_SEMANTIC_ID)
                                .build()
                )
                .build());
        var property = new DefaultProperty.Builder()
                .idShort("idShort")
                .value(VALUE)
                .semanticId(new DefaultReference.Builder().keys(
                        new DefaultKey.Builder()
                                .type(KeyTypes.CONCEPT_DESCRIPTION)
                                .value(SEMANTIC_ID)
                                .build()
                        ).build()
                ).build();

        submodel.setSubmodelElements(List.of(property));
        ServiceRequirement requirement = createSimpleRequirement();
        boolean fulfilled = this.serviceOfferingVersionRequirementsHandler.isRequirementFulfilledBySubmodels(requirement, Collections.singletonList(submodel));
        assertThat(fulfilled).isTrue();
    }

    @Test
    void identifyCorrectParentSubmodel() {
        var submodel = new DefaultSubmodel();
        submodel.setId("SubmodelId");
        var keys = new ArrayList<Key>();
        keys.add(new DefaultKey.Builder().type(KeyTypes.CONCEPT_DESCRIPTION).value("WRONG_PARENT_SEMANTIC_ID").build());
        keys.add(new DefaultKey.Builder().type(KeyTypes.CONCEPT_DESCRIPTION).value(PARENT_SEMANTIC_ID).build());
        submodel.setSemanticId(new DefaultReference.Builder().keys(keys).build());
        var property = new RequirementProperty();
        property.setParentSubmodelsSemanticIds(Collections.singletonList(PARENT_SEMANTIC_ID));
        boolean isCorrectParentSubmodel = this.serviceOfferingVersionRequirementsHandler.isCorrectParentSubmodel(property, submodel);
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
