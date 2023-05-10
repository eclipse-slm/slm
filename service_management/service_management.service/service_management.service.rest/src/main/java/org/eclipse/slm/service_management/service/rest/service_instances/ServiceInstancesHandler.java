package org.eclipse.slm.service_management.service.rest.service_instances;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.service.client.handler.ApiException;
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.service.rest.service_deployment.CapabilityServiceNotFoundException;
import org.eclipse.slm.service_management.service.rest.service_deployment.ServiceUndeploymentHandler;
import org.eclipse.slm.service_management.service.rest.service_deployment.ServiceUpdateHandler;
import org.eclipse.slm.service_management.service.rest.service_offerings.ServiceOfferingVersionHandler;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.ServiceOrder;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionWithCurrentValue;
import org.eclipse.slm.service_management.model.services.ServiceInstance;
import org.eclipse.slm.service_management.model.services.ServiceInstanceDetails;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceGroupNotFoundException;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceNotFoundException;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceUpdateException;
import org.eclipse.slm.service_management.persistence.api.ServiceInstanceGroupJpaRepository;
import org.eclipse.slm.service_management.persistence.api.ServiceOrderJpaRepository;
import org.eclipse.slm.service_management.service.rest.service_instances.AvailableServiceInstanceVersionChange;
import org.eclipse.slm.service_management.service.rest.service_instances.AvailableServiceInstanceVersionChangeType;
import org.eclipse.slm.service_management.service.rest.service_offerings.ServiceOfferingHandler;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.net.ssl.SSLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ServiceInstancesHandler {

    public final static Logger LOG = LoggerFactory.getLogger(ServiceInstancesHandler.class);

    private final ConsulServicesApiClient consulServicesApiClient;

    private final ServiceUndeploymentHandler serviceUndeploymentHandler;

    private final ServiceUpdateHandler serviceUpdateHandler;

    private final ServiceOfferingVersionHandler serviceOfferingVersionHandler;

    private final ServiceOfferingHandler serviceOfferingHandler;

    private final ServiceOrderJpaRepository serviceOrderJpaRepository;

    private final ServiceInstancesConsulClient serviceInstancesConsulClient;

    private final ServiceInstanceGroupJpaRepository serviceInstanceGroupJpaRepository;

    private final ObjectMapper objectMapper;

    public ServiceInstancesHandler(ConsulServicesApiClient consulServicesApiClient,
                                   ServiceUndeploymentHandler serviceUndeploymentHandler,
                                   ServiceUpdateHandler serviceUpdateHandler, ServiceOfferingVersionHandler serviceOfferingVersionHandler,
                                   ServiceOfferingHandler serviceOfferingHandler, ServiceOrderJpaRepository serviceOrderJpaRepository, ServiceInstancesConsulClient serviceInstancesConsulClient, ServiceInstanceGroupJpaRepository serviceInstanceGroupJpaRepository, ObjectMapper objectMapper) {
        this.consulServicesApiClient = consulServicesApiClient;
        this.serviceUndeploymentHandler = serviceUndeploymentHandler;
        this.serviceUpdateHandler = serviceUpdateHandler;
        this.serviceOfferingVersionHandler = serviceOfferingVersionHandler;
        this.serviceOfferingHandler = serviceOfferingHandler;
        this.serviceOrderJpaRepository = serviceOrderJpaRepository;
        this.serviceInstancesConsulClient = serviceInstancesConsulClient;
        this.serviceInstanceGroupJpaRepository = serviceInstanceGroupJpaRepository;
        this.objectMapper = objectMapper;
    }

    public List<ServiceInstance> getServiceInstancesOfUser(KeycloakPrincipal keycloakPrincipal) throws ConsulLoginFailedException {
        Map<String,List<String>> allCatalogServicesOfUser = this.consulServicesApiClient.getServices(
                new ConsulCredential(keycloakPrincipal)
        );

        var deployedServicesOfUser = allCatalogServicesOfUser.entrySet().stream()
                .filter(entry -> entry.getValue().contains("service"))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        var deployedServicesWithDetails = this.consulServicesApiClient
                .getServicesByName(new ConsulCredential(keycloakPrincipal), deployedServicesOfUser.keySet());
        var serviceInstances = new ArrayList<ServiceInstance>();
        for (var consulService : deployedServicesWithDetails.values())
        {
            if (consulService.size() > 0) {
                var serviceInstance = this.convertConsulServiceToServiceInstance(consulService.get(0));
                serviceInstances.add(serviceInstance);
            }
        }

        return serviceInstances;
    }

    public ServiceInstance getServiceInstanceOfUser(UUID serviceInstanceId, KeycloakPrincipal keycloakPrincipal)
            throws ConsulLoginFailedException, ServiceInstanceNotFoundException {

        var consulCatalogServiceOptional = this.consulServicesApiClient.getServiceById(
                new ConsulCredential(keycloakPrincipal), serviceInstanceId
        );

        if (consulCatalogServiceOptional.isPresent()) {
            var serviceInstance = this.convertConsulServiceToServiceInstance(consulCatalogServiceOptional.get());
            return serviceInstance;
        }
        else {
            throw new ServiceInstanceNotFoundException(serviceInstanceId);
        }
    }

    public void deleteServiceInstanceOfUser(UUID serviceInstanceId, KeycloakPrincipal keycloakPrincipal)
            throws ConsulLoginFailedException, ServiceInstanceNotFoundException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, SSLException, ApiException, CapabilityServiceNotFoundException {

        var optionalConsulService = this.consulServicesApiClient
                .getServiceByName(new ConsulCredential(keycloakPrincipal), "service_" + serviceInstanceId);

        if (optionalConsulService.isPresent()) {
            if (!optionalConsulService.get().isEmpty()) {
                this.serviceUndeploymentHandler.deleteService(keycloakPrincipal, optionalConsulService.get());
            }
            else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service instance '" + serviceInstanceId + "' known, but not sub-services registered");
            }
        }
        else {
            throw new ServiceInstanceNotFoundException(serviceInstanceId);
        }
    }

    public List<AvailableServiceInstanceVersionChange> getAvailableVersionChangesForServiceInstance(UUID serviceInstanceId, KeycloakPrincipal keycloakPrincipal)
            throws ConsulLoginFailedException, ServiceInstanceNotFoundException,
            ServiceOfferingNotFoundException {
        var serviceInstance = this.getServiceInstanceOfUser(serviceInstanceId, keycloakPrincipal);
        var serviceOffering = serviceOfferingHandler.getServiceOfferingById(serviceInstance.getServiceOfferingId());

        Optional<ServiceOfferingVersion> currentServiceOfferingVersionOptional;
        var availableServiceInstanceVersionChanges = new ArrayList<AvailableServiceInstanceVersionChange>();
        if ((currentServiceOfferingVersionOptional
                = serviceOffering.hasVersionWithId(serviceInstance.getServiceOfferingVersionId())).isPresent()) {
            var currentServiceOfferingVersion = currentServiceOfferingVersionOptional.get();

            for (var serviceOfferingVersion : serviceOffering.getVersions()) {
                AvailableServiceInstanceVersionChange availableServiceInstanceVersionChange;
                if (serviceOfferingVersion.getCreated().after(currentServiceOfferingVersion.getCreated())) {
                    availableServiceInstanceVersionChange = new AvailableServiceInstanceVersionChange(
                            serviceOfferingVersion.getId(), serviceOfferingVersion.getVersion(),
                            serviceOfferingVersion.getCreated(), AvailableServiceInstanceVersionChangeType.UP);
                    availableServiceInstanceVersionChanges.add(availableServiceInstanceVersionChange);
                }
                else if (serviceOfferingVersion.getCreated().before(currentServiceOfferingVersion.getCreated())) {
                    availableServiceInstanceVersionChange = new AvailableServiceInstanceVersionChange(
                            serviceOfferingVersion.getId(), serviceOfferingVersion.getVersion(),
                            serviceOfferingVersion.getCreated(), AvailableServiceInstanceVersionChangeType.DOWN);
                    availableServiceInstanceVersionChanges.add(availableServiceInstanceVersionChange);
                }

            }
        }

        availableServiceInstanceVersionChanges
                .sort(Comparator.comparing(AvailableServiceInstanceVersionChange::getVersionDate).reversed());

        return availableServiceInstanceVersionChanges;
    }

    public void updateServiceInstanceToVersion(UUID serviceInstanceId, UUID targetServiceOfferingVersionId, KeycloakPrincipal keycloakPrincipal)
            throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException, ServiceInstanceUpdateException, SSLException, JsonProcessingException, ServiceOptionNotFoundException, ApiException, InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException {
        var serviceInstance = this.getServiceInstanceOfUser(serviceInstanceId, keycloakPrincipal);
        var serviceOffering = serviceOfferingHandler.getServiceOfferingById(serviceInstance.getServiceOfferingId());

        Optional<ServiceOfferingVersion> currentServiceOfferingVersionOptional;
        if ((currentServiceOfferingVersionOptional
                = serviceOffering.hasVersionWithId(targetServiceOfferingVersionId)).isPresent()) {
            serviceUpdateHandler.updateServiceInstance(keycloakPrincipal, serviceInstance, currentServiceOfferingVersionOptional.get());
        }
        else {
            throw new ServiceInstanceUpdateException("Service offering '" + serviceOffering.getId() +"' has no version " +
                    "with id '" + targetServiceOfferingVersionId + "'");
        }
    }

    private ServiceInstance convertConsulServiceToServiceInstance(CatalogService consulCatalogService) {
        var serviceTags = consulCatalogService.getServiceTags();
        serviceTags = new ArrayList<>(new HashSet<>(serviceTags)); // Remove duplicates
        var serviceMetaData = consulCatalogService.getServiceMeta();

        var serviceInstance = ServiceInstance.Companion.ofMetaDataAndTags(serviceMetaData, serviceTags);
        return serviceInstance;
    }

    public List<ServiceOrder> getOrdersOfServiceInstance(UUID serviceInstanceId) {
        var orders = this.serviceOrderJpaRepository.findByServiceInstanceId(serviceInstanceId);
        return orders;
    }

    public ServiceInstanceDetails getServiceInstanceDetails(KeycloakPrincipal keycloakPrincipal, UUID serviceInstanceId)
            throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {

        var serviceInstance = this.getServiceInstanceOfUser(serviceInstanceId, keycloakPrincipal);
        var orders = this.getOrdersOfServiceInstance(serviceInstanceId);
        orders = orders.stream()
                .sorted(Comparator.comparing(ServiceOrder::getCreated))
                .collect(Collectors.toList());
        var firstOrder = orders.get(0);
        var lastOrder = orders.get(orders.size() - 1);

        var serviceOptionWithValues = new ArrayList<ServiceOptionWithCurrentValue>();
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .getServiceOfferingVersionById(serviceInstance.getServiceOfferingId(), serviceInstance.getServiceOfferingVersionId());
        for (var serviceOptionValue : lastOrder.getServiceOptionValues()) {
            for (var serviceOptionCategory : serviceOfferingVersion.getServiceOptionCategories()) {
                var serviceOptionOptional = serviceOptionCategory.getServiceOptions().stream()
                        .filter(so -> so.getId().equals(serviceOptionValue.getServiceOptionId())).findAny();
                if (serviceOptionOptional.isPresent()) {
                    serviceOptionWithValues.add(new ServiceOptionWithCurrentValue(serviceOptionOptional.get(), serviceOptionValue.getValue()));
                }
            }
        }

        var details = new ServiceInstanceDetails(
                serviceInstanceId,
                serviceInstance.getServiceOfferingId(),
                serviceInstance.getServiceOfferingVersionId(),
                firstOrder.getCreated(),
                lastOrder.getCreated(),
                serviceInstance.getTags(),
                serviceInstance.getGroupIds(),
                serviceInstance.getMetaData(),
                serviceOptionWithValues,
                lastOrder.getDeploymentCapabilityServiceId(),
                orders);

        return details;
    }

    public void setGroupsForServiceInstance(KeycloakPrincipal keycloakPrincipal, List<UUID> groupIds, UUID serviceInstanceId)
            throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceInstanceGroupNotFoundException {
        var serviceInstance = this.getServiceInstanceOfUser(serviceInstanceId, keycloakPrincipal);

        for (var groupId : groupIds) {
            var optionalGroup = serviceInstanceGroupJpaRepository.findById(groupId);
            if (optionalGroup.isEmpty()) {
                throw new ServiceInstanceGroupNotFoundException(groupId);
            }
        }
        serviceInstance.setGroupIds(groupIds);

        this.serviceInstancesConsulClient.updateConsulServiceForServiceInstance(serviceInstance);
    }
}
