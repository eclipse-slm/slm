package org.eclipse.slm.service_management.service.rest.service_offerings;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.resource_management.model.resource.ResourceAas;
import org.eclipse.slm.service_management.model.offerings.RequirementLogicType;
import org.eclipse.slm.service_management.model.offerings.RequirementProperty;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirement;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirementLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class ServiceOfferingVersionRequirementsHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingVersionRequirementsHandler.class);

    private final AasRepositoryClient aasRepositoryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    private final SubmodelRepositoryClient submodelRepositoryClient;

    public ServiceOfferingVersionRequirementsHandler(AasRegistryClient aasRegistryClient,
                                              AasRepositoryClient aasRepositoryClient,
                                              SubmodelRegistryClient submodelRegistryClient,
                                              SubmodelRepositoryClient submodelRepositoryClient) {
        this.aasRepositoryClient = aasRepositoryClient;
        this.submodelRegistryClient = submodelRegistryClient;
        this.submodelRepositoryClient = submodelRepositoryClient;
    }

    public boolean isRequirementFulfilledByResource(ServiceRequirement serviceRequirement,
                                                    UUID resourceId,
                                                    JwtAuthenticationToken jwtAuthenticationToken) {
        var resourceAas = this.aasRepositoryClient.getAas(ResourceAas.createAasIdFromResourceId(resourceId));

        var resourceSubmodels = new ArrayList<Submodel>();
        for (var submodelRef: resourceAas.getSubmodels()) {
            var submodelDescriptor = this.submodelRegistryClient.findSubmodelDescriptor(submodelRef.getKeys().get(0).getValue());
            var submodelEndpoint = submodelDescriptor.get().getEndpoints().get(0).getProtocolInformation().getHref();

            if (submodelEndpoint.contains("/submodels/")) {
                var scopedSubmodelRepositoryClient = SubmodelRepositoryClient.FromSubmodelDescriptor(submodelDescriptor.get(), jwtAuthenticationToken);
                var submodel = scopedSubmodelRepositoryClient.getSubmodel(submodelDescriptor.get().getId());
                if (submodel != null) {
                    resourceSubmodels.add(submodel);
                }
            }

            if (submodelEndpoint.contains("/submodel")) {
                var submodelServiceClient = SubmodelServiceClient.FromSubmodelDescriptor(submodelDescriptor.get(), jwtAuthenticationToken);
                var submodelOptional = submodelServiceClient.getSubmodel();
                submodelOptional.ifPresent(resourceSubmodels::add);
            }
        }

        // Check if requirement logic is fulfilled by submodels
        return this.isRequirementFulfilledBySubmodels(serviceRequirement, resourceSubmodels);

    }

    public boolean isRequirementFulfilledBySubmodels(ServiceRequirement serviceRequirement, List<Submodel> submodels) {
        return serviceRequirement.getLogics().stream().allMatch(logic -> isLogicFulfilledBySubmodels(logic, submodels));
    }

    public boolean isLogicFulfilledBySubmodels(ServiceRequirementLogic logic, List<Submodel> submodels) {
        var propertiesFulfilled = new ArrayList<Boolean>();
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

    public boolean isRequirementPropertyFulfilledBySubmodel(RequirementProperty requirementProperty, Submodel submodel) {
        if (!isCorrectParentSubmodel(requirementProperty, submodel)) {
            return false;
        }
        var property = findPropertyInSubmodel(submodel, requirementProperty.getSemanticId());
        return property != null && property.getValue().equals(requirementProperty.getValue());
    }

    public boolean isCorrectParentSubmodel(RequirementProperty property, Submodel submodel) {
        for (String parentSemanticId: property.getParentSubmodelsSemanticIds()) {
            if (submodel.getSemanticId() != null) {
                for (var key : submodel.getSemanticId().getKeys()) {
                    if (key.getValue().equals(parentSemanticId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Property findPropertyInSubmodel(Submodel submodel, String semanticId) {
        for (var submodelElement : submodel.getSubmodelElements()) {
            if (submodelElement instanceof SubmodelElementCollection) {
                Property property = findPropertyInSubmodelElementCollection((SubmodelElementCollection) submodelElement, semanticId);
                if (property != null) {
                    return property;
                }
            } else if (submodelElement instanceof Property) {
                Property property = (Property) submodelElement;
                var keys = property.getSemanticId().getKeys();
                if (keys.size() > 0 && keys.get(0).getValue().equals(semanticId)) {
                    return property;
                }
            }
        }
        return null;
    }

    public Property findPropertyInSubmodelElementCollection(SubmodelElementCollection smc, String semanticId) {
        for (var submodelElement : smc.getValue()) {
            if (submodelElement instanceof SubmodelElementCollection) {
                Property property = findPropertyInSubmodelElementCollection((SubmodelElementCollection) submodelElement, semanticId);
                if (property != null) {
                    return property;
                }
            } else if (submodelElement instanceof Property) {
                var property = (Property) submodelElement;
                var keys = property.getSemanticId().getKeys();
                if (keys.size() > 0 && keys.get(0).getValue().equals(semanticId)) {
                    return property;
                }
            }
        }
        return null;
    }
}
