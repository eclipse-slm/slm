package org.eclipse.slm.service_management.service.rest.aas;

import org.eclipse.slm.service_management.model.offerings.ServiceRequirementLogic;
import org.eclipse.slm.service_management.model.offerings.RequirementLogicType;
import org.eclipse.slm.service_management.model.offerings.RequirementProperty;
import org.eclipse.basyx.submodel.metamodel.api.qualifier.qualifiable.IConstraint;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.qualifiable.Qualifier;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequirementsConverterTest {
    @Test
    void convertSimpleRequirementProperty() {
        RequirementProperty requirementProperty = new RequirementProperty();
        requirementProperty.setValue("testValue");
        requirementProperty.setName("testName");
        requirementProperty.setSemanticId("testSemanticId");
        requirementProperty.setParentSubmodelsSemanticIds(Arrays.asList("parentSemanticId1", "parentSemanticId2"));
        Property property = RequirementsConverter.createProperty(requirementProperty);
        assertPropertiesEqual(requirementProperty, property);
    }

    @Test
    void convertSimpleRequirementLogic() {
        RequirementProperty requirementProperty = new RequirementProperty();
        requirementProperty.setValue("testValue");
        requirementProperty.setName("testName");
        requirementProperty.setSemanticId("testSemanticId");
        requirementProperty.setParentSubmodelsSemanticIds(Arrays.asList("parentSemanticId1", "parentSemanticId2"));

        ServiceRequirementLogic requirementLogic = new ServiceRequirementLogic();
        requirementLogic.setProperties(Collections.singletonList(requirementProperty));
        requirementLogic.setType(RequirementLogicType.ANY);

        SubmodelElementCollection logicSMC = RequirementsConverter.createLogicSMC("identifier", requirementLogic);
        assertLogicsEqual(requirementLogic, logicSMC);
    }

    static void assertLogicsEqual(ServiceRequirementLogic requirementLogic, SubmodelElementCollection logicSMC) {
        String logicSMCType = logicSMC.getSemanticId().getKeys().get(0).getValue().replace("https://fab-os.org/Requirements/Logic/", "");
        assertEquals(logicSMCType, requirementLogic.getType().toString());
        List<RequirementProperty> requirementProperties = requirementLogic.getProperties();
        int propertyIndex = 0;
        for (Map.Entry<String, ISubmodelElement> propertyEntry : logicSMC.getSubmodelElements().entrySet()) {
            RequirementProperty requirementProperty = requirementProperties.get(propertyIndex);
            Property property = (Property) propertyEntry.getValue();
            assertPropertiesEqual(requirementProperty, property);
            propertyIndex ++;
        }
    }

    static void assertPropertiesEqual(RequirementProperty requirementProperty, Property property) {
        assertEquals(requirementProperty.getName(), property.getIdShort());
        assertEquals(requirementProperty.getSemanticId(), property.getSemanticId().getKeys().get(0).getValue());
        List<String> proSemIds = new ArrayList<>();
        List<String> reqSemIds = requirementProperty.getParentSubmodelsSemanticIds();
        for (IConstraint iConstraint : property.getQualifiers()) {
            Qualifier qualifier = (Qualifier) iConstraint;
            proSemIds.add(qualifier.getValue().toString());
        }
        assertTrue(proSemIds.size() == reqSemIds.size() && proSemIds.containsAll(reqSemIds) && reqSemIds.containsAll(proSemIds));
    }
}
