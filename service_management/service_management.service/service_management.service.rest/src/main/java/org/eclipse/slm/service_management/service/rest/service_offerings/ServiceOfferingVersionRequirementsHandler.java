package org.eclipse.slm.service_management.service.rest.service_offerings;

import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.eclipse.slm.service_management.model.offerings.RequirementLogicType;
import org.eclipse.slm.service_management.model.offerings.RequirementProperty;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirement;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirementLogic;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.reference.IKey;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ServiceOfferingVersionRequirementsHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingVersionRequirementsHandler.class);
    private final ConnectedAssetAdministrationShellManager aasManager;

    ServiceOfferingVersionRequirementsHandler(
            @Value("${basyx.aas-registry.url}") String aasRegistryUrl) {
        IAASRegistry registry = new AASRegistryProxy(aasRegistryUrl);
        this.aasManager = new ConnectedAssetAdministrationShellManager(registry);
    }

    public boolean isRequirementFulfilledByResource(ServiceRequirement requirementDTO, UUID resourceId) {
        IIdentifier iIdentifier = new CustomId(resourceId.toString());
        Map<String, ISubmodel> submodelMap;
        try {
            submodelMap = aasManager.retrieveSubmodels(iIdentifier);
        } catch (org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException e) {
            return false;
        }
        List<Submodel> submodels = new ArrayList<>();
        for (Map.Entry<String, ISubmodel> entry: submodelMap.entrySet()) {
            try {
                Submodel submodel = ((ConnectedSubmodel) entry.getValue()).getLocalCopy();
                submodels.add(submodel);
            } catch(ResourceNotFoundException e) {
                LOG.warn("Submodel not available anymore. Error Msg: " + e.getMessage());
            }
        }
        return isRequirementFulfilledBySubmodels(requirementDTO, submodels);
    }

    public static boolean isRequirementFulfilledBySubmodels(ServiceRequirement requirementDTO, List<Submodel> submodels) {
        return requirementDTO.getLogics().stream().allMatch(logic -> isLogicFulfilledBySubmodels(logic, submodels));
    }

    public static boolean isLogicFulfilledBySubmodels(ServiceRequirementLogic logic, List<Submodel> submodels) {
        List<Boolean> propertiesFulfilled = new ArrayList<>();
        for (RequirementProperty requirementProperty : logic.getProperties()) {
            boolean propertyFulfilled = false;
            for (Submodel submodel : submodels) {
                propertyFulfilled = isRequirementPropertyFulfilledBySubmodel(requirementProperty, submodel);
                if (propertyFulfilled) {
                    break;
                }
            }
            propertiesFulfilled.add(propertyFulfilled);
        }
        if (logic.getType() == RequirementLogicType.ALL) {
            return propertiesFulfilled.stream().allMatch(Boolean::valueOf);
        } else if (logic.getType() == RequirementLogicType.ANY) {
            return propertiesFulfilled.stream().anyMatch(Boolean::valueOf);
        }
        return false;
    }

    public static boolean isRequirementPropertyFulfilledBySubmodel(RequirementProperty requirementProperty, Submodel submodel) {
        if (!isCorrectParentSubmodel(requirementProperty, submodel)) {
            return false;
        }
        Property property = findPropertyInSubmodel(submodel, requirementProperty.getSemanticId());
        return property != null && property.getValue().equals(requirementProperty.getValue());
    }

    public static boolean isCorrectParentSubmodel(RequirementProperty property, Submodel submodel) {
        for (String parentSemanticId: property.getParentSubmodelsSemanticIds()) {
            for (IKey key: submodel.getSemanticId().getKeys()) {
                if (key.getValue().equals(parentSemanticId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Property findPropertyInSubmodel(Submodel submodel, String semanticId) {
        for (Map.Entry<String, ISubmodelElement> entry: submodel.getSubmodelElements().entrySet()) {
            ISubmodelElement iSubmodelElement = entry.getValue();
            if (iSubmodelElement.getClass().equals(SubmodelElementCollection.class)) {
                Property property = findPropertyInSubmodelElementCollection((SubmodelElementCollection) iSubmodelElement, semanticId);
                if (property != null) {
                    return property;
                }
            } else if (iSubmodelElement.getClass().equals(Property.class)) {
                Property property = (Property) iSubmodelElement;
                System.out.println("property "+property);
                var keys = property.getSemanticId().getKeys();
                if (keys.size() > 0 && keys.get(0).getValue().equals(semanticId)) {
                    return property;
                }
            }
        }
        return null;
    }

    public static Property findPropertyInSubmodelElementCollection(SubmodelElementCollection smc, String semanticId) {
        for (Map.Entry<String, ISubmodelElement> entry: smc.getSubmodelElements().entrySet()) {
            ISubmodelElement iSubmodelElement = entry.getValue();
            if (iSubmodelElement.getClass().equals(SubmodelElementCollection.class)) {
                Property property = findPropertyInSubmodelElementCollection((SubmodelElementCollection) iSubmodelElement, semanticId);
                if (property != null) {
                    return property;
                }
            } else if (iSubmodelElement.getClass().equals(Property.class)) {
                Property property = (Property) iSubmodelElement;
                var keys = property.getSemanticId().getKeys();
                if (keys.size() > 0 && keys.get(0).getValue().equals(semanticId)) {
                    return property;
                }
            }
        }
        return null;
    }
}
