package org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.requirements;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.slm.common.aas.repositories.submodels.SubmodelUtils;
import org.eclipse.slm.service_management.model.offerings.RequirementProperty;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirement;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirementLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RequirementsSubmodel extends DefaultSubmodel {
    public static final String SUBMODEL_ID_PREFIX = "Requirements";

    public static final String SEMANTIC_ID_VALUE = "http://eclipse.dev/slm/aas/sm/Requirements";
    public static final Reference SEMANTIC_ID = new DefaultReference.Builder().keys(
            new DefaultKey.Builder()
                    .type(KeyTypes.CONCEPT_DESCRIPTION)
                    .value(SEMANTIC_ID_VALUE).build()).build();

    public RequirementsSubmodel(ServiceOfferingVersion serviceOfferingVersion)  {
        super();
        this.id = RequirementsSubmodel.getSubmodelIdForServiceOfferingVersionId(serviceOfferingVersion.getId());
        this.idShort = RequirementsSubmodel.getSubmodelIdShortForServiceOfferingVersion(serviceOfferingVersion);
        this.setSemanticId(SEMANTIC_ID);

        for (ServiceRequirement requirement : serviceOfferingVersion.getServiceRequirements()){
            var requirementSMC = createRequirementSMC(requirement);
            this.setSubmodelElements(List.of(requirementSMC));
        }
    }

    private SubmodelElementCollection createRequirementSMC(ServiceRequirement requirement) {
        var requirementSMC = new DefaultSubmodelElementCollection.Builder()
                .idShort(requirement.getName());

        var submodelElements = new ArrayList<SubmodelElement>();
        int logicNum = 0;
        for (ServiceRequirementLogic logic : requirement.getLogics()) {
            String logicIdShort = "Logic" + ++logicNum + "_" + logic.getType();
            SubmodelElementCollection logicSMC = createLogicSMC(logicIdShort, logic);
            submodelElements.add(logicSMC);
        }

        requirementSMC.value(submodelElements);

        return requirementSMC.build();
    }

    private SubmodelElementCollection createLogicSMC(String idShort, ServiceRequirementLogic logic) {
        var logicSMC = new DefaultSubmodelElementCollection.Builder()
                .idShort(idShort)
                .semanticId(SubmodelUtils.generateSemanticId("http://eclipse.dev/slm/aas/sm/Requirements/Logic/"+logic.getType()));
        var submodelElements = new ArrayList<SubmodelElement>();
        for (RequirementProperty requirementProperty : logic.getProperties()) {
            Property property = createProperty(requirementProperty);
            submodelElements.add(property);
        }

        logicSMC.value(submodelElements);

        return logicSMC.build();
    }

    public static Property createProperty(RequirementProperty requirementProperty) {
        var property = new DefaultProperty.Builder()
                        .idShort(requirementProperty.getName())
                        .semanticId(SubmodelUtils.generateSemanticId(requirementProperty.getSemanticId()))
                        .value(requirementProperty.getValue());
        var qualifiers = new ArrayList<Qualifier>();
        for (String parentSMSemanticId : requirementProperty.getParentSubmodelsSemanticIds()) {
            var qualifier = new DefaultQualifier.Builder()
                    .type("string")
                    .value(parentSMSemanticId)
                    .build();
            qualifiers.add(qualifier);
        }
        property.qualifiers(qualifiers);

        return property.build();
    }

    public static String getSubmodelIdForServiceOfferingVersionId(UUID serviceOfferingVersionId) {
        return RequirementsSubmodel.SUBMODEL_ID_PREFIX + "-" + serviceOfferingVersionId;
    }

    public static String getSubmodelIdShortForServiceOfferingVersion(ServiceOfferingVersion serviceOfferingVersion) {
        return RequirementsSubmodel.SUBMODEL_ID_PREFIX
                + "-" + serviceOfferingVersion.getServiceOffering().getName()
                + "-" + serviceOfferingVersion.getVersion();
    }
}
