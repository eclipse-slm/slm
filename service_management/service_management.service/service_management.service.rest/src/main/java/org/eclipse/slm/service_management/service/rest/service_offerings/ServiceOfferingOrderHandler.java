package org.eclipse.slm.service_management.service.rest.service_offerings;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.parent.service_rest.controller.TemplateVariableHandler;
import org.eclipse.slm.resource_management.model.capabilities.provider.ServiceHosterFilter;
import org.eclipse.slm.resource_management.model.consul.capability.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.model.consul.capability.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.model.resource.MatchingResourceDTO;
import org.eclipse.slm.resource_management.service.client.ResourceManagementApiClientInitializer;
import org.eclipse.slm.resource_management.service.client.handler.*;
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.service.rest.service_deployment.CapabilityServiceNotFoundException;
import org.eclipse.slm.service_management.service.rest.service_deployment.ServiceDeploymentHandler;
import org.eclipse.slm.service_management.model.offerings.ServiceOrder;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionValueType;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ServiceOfferingOrderHandler {

    private static final Logger Log = LoggerFactory.getLogger(ServiceOfferingOrderHandler.class);

    private final ServiceOfferingHandler serviceOfferingHandler;

    private final ServiceOfferingVersionHandler serviceOfferingVersionHandler;

    private final ServiceDeploymentHandler serviceDeploymentHandler;

    private final ResourceManagementApiClientInitializer resourceManagementApiClientInitializer;

    private final ServiceOfferingVersionRequirementsHandler serviceOfferingVersionRequirementsHandler;

    private final TemplateVariableHandler templateVariableHandler;

    public ServiceOfferingOrderHandler(ServiceOfferingHandler serviceOfferingHandler,
                                       ServiceOfferingVersionHandler serviceOfferingVersionHandler,
                                       ServiceDeploymentHandler serviceDeploymentHandler,
                                       ResourceManagementApiClientInitializer resourceManagementApiClientInitializer,
                                       ServiceOfferingVersionRequirementsHandler serviceOfferingVersionRequirementsHandler,
                                       TemplateVariableHandler templateVariableHandler
    ) {
        this.serviceOfferingHandler = serviceOfferingHandler;
        this.serviceOfferingVersionHandler = serviceOfferingVersionHandler;
        this.serviceDeploymentHandler = serviceDeploymentHandler;
        this.resourceManagementApiClientInitializer = resourceManagementApiClientInitializer;
        this.serviceOfferingVersionRequirementsHandler = serviceOfferingVersionRequirementsHandler;
        this.templateVariableHandler = templateVariableHandler;
    }

    public void orderServiceOfferingById(UUID serviceOfferingId, UUID serviceOfferingVersionId,
                                         ServiceOrder serviceOrder,
                                         UUID deploymentCapabilityServiceId, KeycloakPrincipal keycloakPrincipal)
            throws SSLException, JsonProcessingException, ServiceOptionNotFoundException, ApiException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException {
        var serviceOffering = this.serviceOfferingHandler.getServiceOfferingById(serviceOfferingId);
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .getServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersionId);
        // Check if values for all required service options are provided and get values for template variables
        for (var serviceOptionCategory : serviceOfferingVersion.getServiceOptionCategories()) {
            for (var serviceOption : serviceOptionCategory.getServiceOptions()) {
                if (serviceOption.getRequired()) {
                    var optionalServiceOptionValue = serviceOrder.getServiceOptionValues()
                            .stream().filter(sov -> sov.getServiceOptionId().contentEquals(serviceOption.getId())).findAny();
                    if (optionalServiceOptionValue.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Value for required service option '" + serviceOption.getKey() + "' is missing");
                    }
                }

                if (serviceOption.getValueType().equals(ServiceOptionValueType.TEMPLATE_VARIABLE)) {
                    var optionalServiceOptionValue = serviceOrder.getServiceOptionValues()
                            .stream().filter(sov -> sov.getServiceOptionId().contentEquals(serviceOption.getId())).findAny();

                    if (optionalServiceOptionValue.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Service option value for '" + serviceOption.getKey() + "' is missing");
                    }
                    else {
                        var templateVariableKey = optionalServiceOptionValue.get().getValue().toString();
                        var templateVariableValue = templateVariableHandler.getValueForTemplateVariable(templateVariableKey);
                        optionalServiceOptionValue.get().setValue(templateVariableValue);
                    }
                }
            }
        }

        this.serviceDeploymentHandler.deployServiceOfferingToResource(keycloakPrincipal, deploymentCapabilityServiceId, serviceOfferingVersion, serviceOrder);
    }

    public List<MatchingResourceDTO> getCapabilityServicesMatchingServiceRequirements(UUID serviceOfferingId,
                                                                                      UUID serviceOfferingVersionId,
                                                                                      KeycloakPrincipal keycloakPrincipal)
            throws ApiException, SSLException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        var serviceOffering = this.serviceOfferingHandler.getServiceOfferingById(serviceOfferingId);
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .getServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersionId);

        var serviceOfferingDeploymentType = serviceOfferingVersion.getDeploymentDefinition().getDeploymentType();
        var resourceManagementApiClient = resourceManagementApiClientInitializer.init(keycloakPrincipal);

        var capabilityProvidersRestControllerApi = new CapabilityProvidersRestControllerApi(resourceManagementApiClient);

        var serviceHosterFilter = new ServiceHosterFilter.Builder()
                .supportedDeploymentType(serviceOfferingDeploymentType)
                .build();
        var serviceHosters = capabilityProvidersRestControllerApi.getServiceHosters("fabos", serviceHosterFilter);

        serviceHosters = serviceHosters.stream()
                .filter(sh -> {
                    if (sh.getCapabilityService() instanceof SingleHostCapabilityService) {
                        var singleHostCapabilityService = (SingleHostCapabilityService)sh.getCapabilityService();
                        var resourceId = singleHostCapabilityService.getConsulNodeId();

                        return serviceOfferingVersion.getServiceRequirements()
                                .stream()
                                .allMatch(requirementDTO -> serviceOfferingVersionRequirementsHandler.isRequirementFulfilledByResource(requirementDTO, resourceId));
                    }
                    else {
                        return true;
                    }
                }).collect(Collectors.toList());

        var matchingResources = new ArrayList<MatchingResourceDTO>();
        for (var serviceHoster : serviceHosters) {
            UUID resourceId;
            if (serviceHoster.getCapabilityService() instanceof SingleHostCapabilityService) {
                resourceId = ((SingleHostCapabilityService)serviceHoster.getCapabilityService()).getConsulNodeId();
            }
            else {
                resourceId = serviceHoster.getCapabilityService().getId();
            }

            var matchingResource = new MatchingResourceDTO(
                    resourceId,
                    serviceHoster.getCapabilityService().getId(),
                    serviceHoster.getCapabilityService() instanceof MultiHostCapabilityService);
            matchingResources.add(matchingResource);
        }

        return matchingResources;
    }
}
