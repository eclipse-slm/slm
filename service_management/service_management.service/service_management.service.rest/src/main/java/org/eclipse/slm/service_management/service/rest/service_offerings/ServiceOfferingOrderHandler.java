package org.eclipse.slm.service_management.service.rest.service_offerings;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.parent.service_rest.controller.SystemVariableHandler;
import org.eclipse.slm.resource_management.model.capabilities.provider.ServiceHosterFilter;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityService;
import org.eclipse.slm.resource_management.model.consul.capability.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.model.consul.capability.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.model.resource.MatchingResourceDTO;
import org.eclipse.slm.resource_management.service.client.ResourceManagementApiClientInitializer;
import org.eclipse.slm.resource_management.service.client.handler.*;
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.options.DeploymentVariableType;
import org.eclipse.slm.service_management.service.rest.service_deployment.CapabilityServiceNotFoundException;
import org.eclipse.slm.service_management.service.rest.service_deployment.ServiceDeploymentHandler;
import org.eclipse.slm.service_management.model.offerings.ServiceOrder;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ServiceOfferingOrderHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingOrderHandler.class);

    private final ServiceOfferingHandler serviceOfferingHandler;

    private final ServiceOfferingVersionHandler serviceOfferingVersionHandler;

    private final ServiceDeploymentHandler serviceDeploymentHandler;

    private final ResourceManagementApiClientInitializer resourceManagementApiClientInitializer;

    private final ServiceOfferingVersionRequirementsHandler serviceOfferingVersionRequirementsHandler;

    private final SystemVariableHandler systemVariableHandler;

    private final ConsulNodesApiClient consulNodesApiClient;

    public ServiceOfferingOrderHandler(ServiceOfferingHandler serviceOfferingHandler,
                                       ServiceOfferingVersionHandler serviceOfferingVersionHandler,
                                       ServiceDeploymentHandler serviceDeploymentHandler,
                                       ResourceManagementApiClientInitializer resourceManagementApiClientInitializer,
                                       ServiceOfferingVersionRequirementsHandler serviceOfferingVersionRequirementsHandler,
                                       SystemVariableHandler systemVariableHandler,
                                       ConsulNodesApiClient consulNodesApiClient) {
        this.serviceOfferingHandler = serviceOfferingHandler;
        this.serviceOfferingVersionHandler = serviceOfferingVersionHandler;
        this.serviceDeploymentHandler = serviceDeploymentHandler;
        this.resourceManagementApiClientInitializer = resourceManagementApiClientInitializer;
        this.serviceOfferingVersionRequirementsHandler = serviceOfferingVersionRequirementsHandler;
        this.systemVariableHandler = systemVariableHandler;
        this.consulNodesApiClient = consulNodesApiClient;
    }

    public void orderServiceOfferingById(UUID serviceOfferingId, UUID serviceOfferingVersionId,
                                         ServiceOrder serviceOrder,
                                         UUID deploymentCapabilityServiceId, JwtAuthenticationToken jwtAuthenticationToken)
            throws SSLException, JsonProcessingException, ServiceOptionNotFoundException, ApiException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException, ConsulLoginFailedException {
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

                if (serviceOption.getValueType().equals(ServiceOptionValueType.SYSTEM_VARIABLE) ||
                        serviceOption.getValueType().equals(ServiceOptionValueType.DEPLOYMENT_VARIABLE)) {
                    var optionalServiceOptionValue = serviceOrder.getServiceOptionValues()
                            .stream().filter(sov -> sov.getServiceOptionId().contentEquals(serviceOption.getId())).findAny();

                    if (optionalServiceOptionValue.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Service option value for '" + serviceOption.getKey() + "' is missing");
                    }
                    else {
                        switch (serviceOption.getValueType()) {
                            case SYSTEM_VARIABLE -> {
                                var templateVariableKey = optionalServiceOptionValue.get().getValue().toString();
                                var templateVariableValue = systemVariableHandler.getValueForSystemVariable(templateVariableKey);
                                optionalServiceOptionValue.get().setValue(templateVariableValue);
                            }

                            case DEPLOYMENT_VARIABLE -> {
                                var deploymentVariableTypeString = optionalServiceOptionValue.get().getValue().toString();
                                var deploymentVariable = DeploymentVariableType.valueOf(deploymentVariableTypeString);
                                switch (deploymentVariable) {
                                    case TARGET_RESOURCE_ID -> {
                                        var resourceManagementApiClient = resourceManagementApiClientInitializer.init(jwtAuthenticationToken);
                                        var capabilityProvidersRestControllerApi = new CapabilityProvidersRestControllerApi(resourceManagementApiClient);

                                        var serviceHosterFilter = new ServiceHosterFilter.Builder()
                                                .capabilityServiceId(deploymentCapabilityServiceId)
                                                .build();
                                        var serviceHosters = capabilityProvidersRestControllerApi.getServiceHosters("fabos", serviceHosterFilter);
                                        var resourceId = this.getResourceIdOfServiceHoster(serviceHosters.get(0).getCapabilityService());
                                        optionalServiceOptionValue.get().setValue(resourceId);
                                    }

                                    case TARGET_RESOURCE_IP -> {
                                        var resourceManagementApiClient = resourceManagementApiClientInitializer.init(jwtAuthenticationToken);
                                        var capabilityProvidersRestControllerApi = new CapabilityProvidersRestControllerApi(resourceManagementApiClient);

                                        var serviceHosterFilter = new ServiceHosterFilter.Builder()
                                                .capabilityServiceId(deploymentCapabilityServiceId)
                                                .build();
                                        var serviceHosters = capabilityProvidersRestControllerApi.getServiceHosters("fabos", serviceHosterFilter);
                                        var resourceIp = this.getResourceIpOfServiceHoster(serviceHosters.get(0).getCapabilityService());
                                        optionalServiceOptionValue.get().setValue(resourceIp);
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

        this.serviceDeploymentHandler.deployServiceOfferingToResource(jwtAuthenticationToken, deploymentCapabilityServiceId, serviceOfferingVersion, serviceOrder);
    }

    public List<MatchingResourceDTO> getCapabilityServicesMatchingServiceRequirements(UUID serviceOfferingId,
                                                                                      UUID serviceOfferingVersionId,
                                                                                      JwtAuthenticationToken jwtAuthenticationToken)
            throws ApiException, SSLException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        var serviceOffering = this.serviceOfferingHandler.getServiceOfferingById(serviceOfferingId);
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .getServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersionId);

        var serviceOfferingDeploymentType = serviceOfferingVersion.getDeploymentDefinition().getDeploymentType();
        var resourceManagementApiClient = resourceManagementApiClientInitializer.init(jwtAuthenticationToken);

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
                                .allMatch(requirementDTO -> serviceOfferingVersionRequirementsHandler
                                        .isRequirementFulfilledByResource(requirementDTO, resourceId, jwtAuthenticationToken));
                    }
                    else {
                        return true;
                    }
                }).collect(Collectors.toList());

        var matchingResources = new ArrayList<MatchingResourceDTO>();
        for (var serviceHoster : serviceHosters) {
            var resourceId = this.getResourceIdOfServiceHoster(serviceHoster.getCapabilityService());

            var matchingResource = new MatchingResourceDTO(
                    resourceId,
                    serviceHoster.getCapabilityService().getId(),
                    serviceHoster.getCapabilityService() instanceof MultiHostCapabilityService);
            matchingResources.add(matchingResource);
        }

        return matchingResources;
    }

    private UUID getResourceIdOfServiceHoster(CapabilityService capabilityService) {
        UUID resourceId;
        if (capabilityService instanceof SingleHostCapabilityService) {
            resourceId = ((SingleHostCapabilityService)capabilityService).getConsulNodeId();
        }
        else {
            resourceId = capabilityService.getId();
        }

        return resourceId;
    }

    private String getResourceIpOfServiceHoster(CapabilityService capabilityService) throws ConsulLoginFailedException {
        var resourceId = this.getResourceIdOfServiceHoster(capabilityService);
        Optional<Node> optionalNode = consulNodesApiClient.getNodeById(new ConsulCredential(), resourceId);

        String resourceIp = "N/A";
        if (optionalNode.isPresent()) {
            resourceIp = optionalNode.get().getAddress();

        }

        return resourceIp;
    }
}
