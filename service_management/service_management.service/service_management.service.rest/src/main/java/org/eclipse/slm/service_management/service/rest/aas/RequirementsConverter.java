package org.eclipse.slm.service_management.service.rest.aas;

import org.eclipse.slm.service_management.model.offerings.RequirementProperty;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirement;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirementLogic;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.api.qualifier.qualifiable.IConstraint;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.map.identifier.Identifier;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.qualifiable.Qualifier;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.eclipse.slm.service_management.service.rest.aas.RequirementsSubmodel.SUBMODELID;

public class RequirementsConverter {

    private static final String DELIMITER = "-";

    public static RequirementsSubmodel createRequirementsSubmodel(ServiceOfferingVersion serviceOfferingVersion) {
        Identifier identifier = getRequirementsSubmodelIdentifierFromServiceOfferingVersionId(serviceOfferingVersion.getId());
        RequirementsSubmodel requirementsSM = new RequirementsSubmodel(identifier);

        for (ServiceRequirement requirement : serviceOfferingVersion.getServiceRequirements()){
            SubmodelElementCollection requirementSMC = createRequirementSMC(requirement);
            requirementsSM.addSubmodelElement(requirementSMC);
        }

        return requirementsSM;
    }

    public static SubmodelElementCollection createRequirementSMC(ServiceRequirement requirement) {
        SubmodelElementCollection requirementSMC = new SubmodelElementCollection();
        requirementSMC.setIdShort(requirement.getName());
        int logicNum = 0;
        for (ServiceRequirementLogic logic : requirement.getLogics()) {
            String logicIdShort = "Logic" + ++logicNum + "_" + logic.getType();
            SubmodelElementCollection logicSMC = createLogicSMC(logicIdShort, logic);
            requirementSMC.addSubmodelElement(logicSMC);
        }
        return requirementSMC;
    }

    public static SubmodelElementCollection createLogicSMC(String idShort, ServiceRequirementLogic logic) {
        SubmodelElementCollection logicSMC = new SubmodelElementCollection();
        logicSMC.setIdShort(idShort);
        logicSMC.setSemanticId(new Reference(Collections.singletonList(new Key(KeyElements.CONCEPTDESCRIPTION, false, "https://fab-os.org/Requirements/Logic/"+logic.getType(), KeyType.IRI))));
        for (RequirementProperty requirementProperty : logic.getProperties()) {
            Property property = createProperty(requirementProperty);
            logicSMC.addSubmodelElement(property);
        }
        return logicSMC;
    }

    public static Property createProperty(RequirementProperty requirementProperty) {
        Property property = new Property();
        property.setIdShort(requirementProperty.getName());
        property.setSemanticId(new Reference(new Key(KeyElements.PROPERTY, false, requirementProperty.getSemanticId(), IdentifierType.IRDI)));
        property.setValue(requirementProperty.getValue());
        List<IConstraint> qualifiers = new ArrayList<>();
        for (String parentSMSemanticId : requirementProperty.getParentSubmodelsSemanticIds()) {
            Qualifier qualifier = new Qualifier();
            qualifier.setType("string");
            qualifier.setValue(parentSMSemanticId);
            qualifiers.add(qualifier);
        }
        property.setQualifiers(qualifiers);
        return property;
    }

    public static Identifier getRequirementsSubmodelIdentifierFromServiceOfferingVersionId(UUID uuid) {
        return new CustomId(SUBMODELID + DELIMITER + uuid);
    }
}
