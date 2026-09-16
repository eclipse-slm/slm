package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.slm.common.access.UserContext;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.AvailableServiceInstanceVersionChange;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.AvailableServiceInstanceVersionChangeType;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.CapabilityServiceNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.impl.deployment.ServiceOrderJpaRepository;
import org.eclipse.slm.service_management.features.service_deployment.impl.undeployment.ServiceUndeploymentHandler;
import org.eclipse.slm.service_management.features.service_deployment.impl.update.ServiceUpdateHandler;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceRuntimeException;
import org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferingversions.ServiceOfferingVersionHandler;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOfferingVersion;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.options.ServiceOptionWithCurrentValue;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceDetails;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceGroupNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceUpdateException;
import org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferings.ServiceOfferingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ServiceInstancesHandler {

    private final static Logger LOG = LoggerFactory.getLogger(ServiceInstancesHandler.class);

    private final ServiceUndeploymentHandler serviceUndeploymentHandler;

    private final ServiceUpdateHandler serviceUpdateHandler;

    private final ServiceOfferingVersionHandler serviceOfferingVersionHandler;

    private final ServiceOfferingHandler serviceOfferingHandler;

    private final ServiceOrderJpaRepository serviceOrderJpaRepository;

    private final ServiceInstancePersistence serviceInstancePersistence;

    private final ServiceInstanceGroupJpaRepository serviceInstanceGroupJpaRepository;

    public ServiceInstancesHandler(ServiceUndeploymentHandler serviceUndeploymentHandler,
                                   ServiceUpdateHandler serviceUpdateHandler, ServiceOfferingVersionHandler serviceOfferingVersionHandler,
                                   ServiceOfferingHandler serviceOfferingHandler, ServiceOrderJpaRepository serviceOrderJpaRepository, ServiceInstancePersistence serviceInstancePersistence, ServiceInstanceGroupJpaRepository serviceInstanceGroupJpaRepository, ObjectMapper objectMapper) {
        this.serviceUndeploymentHandler = serviceUndeploymentHandler;
        this.serviceUpdateHandler = serviceUpdateHandler;
        this.serviceOfferingVersionHandler = serviceOfferingVersionHandler;
        this.serviceOfferingHandler = serviceOfferingHandler;
        this.serviceOrderJpaRepository = serviceOrderJpaRepository;
        this.serviceInstancePersistence = serviceInstancePersistence;
        this.serviceInstanceGroupJpaRepository = serviceInstanceGroupJpaRepository;
    }

    public List<ServiceInstance> getServiceInstancesOfUser(JwtAuthenticationToken jwtAuthenticationToken) {
        var userContext = UserContext.fromJwt(jwtAuthenticationToken);
        return this.serviceInstancePersistence.getAccessible(userContext);
    }

    public ServiceInstance getServiceInstanceOfUser(UUID serviceInstanceId, JwtAuthenticationToken jwtAuthenticationToken)
            throws ServiceInstanceNotFoundException {
        var userContext = UserContext.fromJwt(jwtAuthenticationToken);
        return this.serviceInstancePersistence.getById(serviceInstanceId, userContext)
                .orElseThrow(() -> new ServiceInstanceNotFoundException(serviceInstanceId));
    }

    public void deleteServiceInstanceOfUser(UUID serviceInstanceId, JwtAuthenticationToken jwtAuthenticationToken)
            throws ServiceInstanceNotFoundException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, SSLException, CapabilityServiceNotFoundException {

        var userContext = UserContext.fromJwt(jwtAuthenticationToken);
        var serviceInstance = this.serviceInstancePersistence.getById(serviceInstanceId, userContext)
                .orElseThrow(() -> new ServiceInstanceNotFoundException(serviceInstanceId));

        this.serviceUndeploymentHandler.deleteService(jwtAuthenticationToken, serviceInstance);
    }

    public List<AvailableServiceInstanceVersionChange> getAvailableVersionChangesForServiceInstance(
            UUID serviceInstanceId,
            JwtAuthenticationToken jwtAuthenticationToken)
            throws ServiceInstanceNotFoundException,
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
            throws ServiceInstanceNotFoundException, ServiceOfferingNotFoundException, ServiceInstanceUpdateException, SSLException,
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

    public List<ServiceOrder> getOrdersOfServiceInstance(UUID serviceInstanceId) {
        var orders = this.serviceOrderJpaRepository.findByServiceInstanceId(serviceInstanceId);
        return orders;
    }

    public ServiceInstanceDetails getServiceInstanceDetails(JwtAuthenticationToken jwtAuthenticationToken, UUID serviceInstanceId)
            throws ServiceInstanceNotFoundException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, ServiceInstanceRuntimeException {

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
            throws ServiceInstanceNotFoundException, ServiceInstanceGroupNotFoundException {
        var serviceInstance = this.getServiceInstanceOfUser(serviceInstanceId, jwtAuthenticationToken);

        for (var groupId : groupIds) {
            var optionalGroup = serviceInstanceGroupJpaRepository.findById(groupId);
            if (optionalGroup.isEmpty()) {
                throw new ServiceInstanceGroupNotFoundException(groupId);
            }
        }
        serviceInstance.setGroupIds(groupIds);

        this.serviceInstancePersistence.update(serviceInstance);
    }
}

