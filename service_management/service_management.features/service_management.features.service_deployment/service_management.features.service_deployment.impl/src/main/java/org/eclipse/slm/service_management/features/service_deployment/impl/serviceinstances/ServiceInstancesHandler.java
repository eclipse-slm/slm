package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.model.catalog.Service;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.service_management.features.service_deployment.api.AvailableServiceInstanceVersionChange;
import org.eclipse.slm.service_management.features.service_deployment.api.AvailableServiceInstanceVersionChangeType;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.CapabilityServiceNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.impl.deployment.ServiceOrderJpaRepository;
import org.eclipse.slm.service_management.features.service_deployment.impl.undeployment.ServiceUndeploymentHandler;
import org.eclipse.slm.service_management.features.service_deployment.impl.update.ServiceUpdateHandler;
import org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceRuntimeException;
import org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferingversions.ServiceOfferingVersionHandler;
import org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOfferingVersion;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.options.ServiceOptionWithCurrentValue;
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstanceDetails;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceGroupNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.services.exceptions.ServiceInstanceUpdateException;
import org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferings.ServiceOfferingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.net.ssl.SSLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ServiceInstancesHandler {

    private final static Logger LOG = LoggerFactory.getLogger(ServiceInstancesHandler.class);

    private final ConsulClientFactory consulClientFactory;

    private final ConsulClient consulAdminClient;

    private final ServiceUndeploymentHandler serviceUndeploymentHandler;

    private final ServiceUpdateHandler serviceUpdateHandler;

    private final ServiceOfferingVersionHandler serviceOfferingVersionHandler;

    private final ServiceOfferingHandler serviceOfferingHandler;

    private final ServiceOrderJpaRepository serviceOrderJpaRepository;

    private final ServiceInstancesConsulClient serviceInstancesConsulClient;

    private final ServiceInstanceGroupJpaRepository serviceInstanceGroupJpaRepository;

    public ServiceInstancesHandler(ConsulClientFactory consulClientFactory,
                                   ServiceUndeploymentHandler serviceUndeploymentHandler,
                                   ServiceUpdateHandler serviceUpdateHandler, ServiceOfferingVersionHandler serviceOfferingVersionHandler,
                                   ServiceOfferingHandler serviceOfferingHandler, ServiceOrderJpaRepository serviceOrderJpaRepository, ServiceInstancesConsulClient serviceInstancesConsulClient, ServiceInstanceGroupJpaRepository serviceInstanceGroupJpaRepository, ObjectMapper objectMapper) {
        this.consulClientFactory = consulClientFactory;
        this.consulAdminClient = consulClientFactory.createAdminClient();
        this.serviceUndeploymentHandler = serviceUndeploymentHandler;
        this.serviceUpdateHandler = serviceUpdateHandler;
        this.serviceOfferingVersionHandler = serviceOfferingVersionHandler;
        this.serviceOfferingHandler = serviceOfferingHandler;
        this.serviceOrderJpaRepository = serviceOrderJpaRepository;
        this.serviceInstancesConsulClient = serviceInstancesConsulClient;
        this.serviceInstanceGroupJpaRepository = serviceInstanceGroupJpaRepository;
    }

    public List<ServiceInstance> getServiceInstancesOfUser(JwtAuthenticationToken jwtAuthenticationToken) throws ConsulLoginFailedException {
        Map<String,List<String>> allCatalogServicesOfUser = this.consulAdminClient.services().getServices(
                
        );

        var deployedServicesOfUser = allCatalogServicesOfUser.entrySet().stream()
                .filter(entry -> entry.getValue().contains("service"))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        var deployedServicesWithDetails = this.consulAdminClient.services()
                .getServicesByName( deployedServicesOfUser.keySet());
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

    public ServiceInstance getServiceInstanceOfUser(UUID serviceInstanceId, JwtAuthenticationToken jwtAuthenticationToken)
            throws ConsulLoginFailedException, ServiceInstanceNotFoundException {

        var consulCatalogServiceOptional = this.consulAdminClient.services().getServiceById(
                 serviceInstanceId
        );

        if (consulCatalogServiceOptional.isPresent()) {
            var serviceInstance = this.convertConsulServiceToServiceInstance(consulCatalogServiceOptional.get());
            return serviceInstance;
        }
        else {
            throw new ServiceInstanceNotFoundException(serviceInstanceId);
        }
    }

    public void deleteServiceInstanceOfUser(UUID serviceInstanceId, JwtAuthenticationToken jwtAuthenticationToken)
            throws ConsulLoginFailedException, ServiceInstanceNotFoundException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, SSLException, CapabilityServiceNotFoundException {

        var consulServiceName = ServiceInstancesConsulClient.getServiceInstancePolicyName(serviceInstanceId);
        var optionalConsulService = this.consulAdminClient.services()
                .getServiceByName( consulServiceName);

        if (optionalConsulService.isPresent()) {
            if (!optionalConsulService.get().isEmpty()) {
                this.serviceUndeploymentHandler.deleteService(jwtAuthenticationToken, optionalConsulService.get());
            }
            else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service instance '" + serviceInstanceId + "' known, but not sub-services registered");
            }
        }
        else {
            throw new ServiceInstanceNotFoundException(serviceInstanceId);
        }
    }

    public List<AvailableServiceInstanceVersionChange> getAvailableVersionChangesForServiceInstance(
            UUID serviceInstanceId,
            JwtAuthenticationToken jwtAuthenticationToken)
            throws ConsulLoginFailedException, ServiceInstanceNotFoundException,
            ServiceOfferingNotFoundException {
        var serviceInstance = this.getServiceInstanceOfUser(serviceInstanceId, jwtAuthenticationToken);
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

    public void updateServiceInstanceToVersion(UUID serviceInstanceId, UUID targetServiceOfferingVersionId,
                                               JwtAuthenticationToken jwtAuthenticationToken)
            throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException, ServiceInstanceUpdateException, SSLException,
            JsonProcessingException, ServiceOptionNotFoundException,  InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException {
        var serviceInstance = this.getServiceInstanceOfUser(serviceInstanceId, jwtAuthenticationToken);
        var serviceOffering = serviceOfferingHandler.getServiceOfferingById(serviceInstance.getServiceOfferingId());

        Optional<ServiceOfferingVersion> currentServiceOfferingVersionOptional;
        if ((currentServiceOfferingVersionOptional
                = serviceOffering.hasVersionWithId(targetServiceOfferingVersionId)).isPresent()) {
            serviceUpdateHandler.updateServiceInstance(jwtAuthenticationToken, serviceInstance, currentServiceOfferingVersionOptional.get());
        }
        else {
            throw new ServiceInstanceUpdateException("Service offering '" + serviceOffering.getId() +"' has no version " +
                    "with id '" + targetServiceOfferingVersionId + "'");
        }
    }

    private ServiceInstance convertConsulServiceToServiceInstance(Service consulService) {
        var serviceTags = consulService.getServiceTags();
        serviceTags = new ArrayList<>(new HashSet<>(serviceTags)); // Remove duplicates
        var serviceMetaData = consulService.getServiceMeta();

        var serviceInstance = ServiceInstance.Companion.ofMetaDataAndTags(serviceMetaData, serviceTags);
        return serviceInstance;
    }

    public List<ServiceOrder> getOrdersOfServiceInstance(UUID serviceInstanceId) {
        var orders = this.serviceOrderJpaRepository.findByServiceInstanceId(serviceInstanceId);
        return orders;
    }

    public ServiceInstanceDetails getServiceInstanceDetails(JwtAuthenticationToken jwtAuthenticationToken, UUID serviceInstanceId)
            throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, ServiceInstanceRuntimeException {

        var serviceInstance = this.getServiceInstanceOfUser(serviceInstanceId, jwtAuthenticationToken);
        var orders = this.getOrdersOfServiceInstance(serviceInstanceId);
        orders = orders.stream()
                .sorted(Comparator.comparing(ServiceOrder::getCreated))
                .collect(Collectors.toList());
        if (orders.isEmpty()) {
            throw new ServiceInstanceRuntimeException("Service instance '" + serviceInstanceId + "' has no orders, cannot determine details");
        }
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

    public void setGroupsForServiceInstance(JwtAuthenticationToken jwtAuthenticationToken, List<UUID> groupIds, UUID serviceInstanceId)
            throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceInstanceGroupNotFoundException {
        var serviceInstance = this.getServiceInstanceOfUser(serviceInstanceId, jwtAuthenticationToken);

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

