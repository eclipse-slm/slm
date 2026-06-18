package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.AvailableServiceInstanceVersionChange;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.CapabilityServiceNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstancesRestApi;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstancesRestApiConfig;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceRuntimeException;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceDetails;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceGroupNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceUpdateException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.SSLException;
import java.util.*;

@RestController
@RequestMapping(ServiceInstancesRestApiConfig.BASE_PATH)
@Tag(name = ServiceInstancesRestApiConfig.TAG)
public class ServiceInstancesRestController implements ServiceInstancesRestApi {

    private final ServiceInstancesHandler serviceInstancesHandler;

    @Autowired
    public ServiceInstancesRestController(ServiceInstancesHandler serviceInstancesHandler) {
        this.serviceInstancesHandler = serviceInstancesHandler;
    }

    @Override
    public ResponseEntity<List<ServiceInstance>> getServicesOfUser()
            throws ConsulLoginFailedException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var serviceInstances = this.serviceInstancesHandler.getServiceInstancesOfUser(jwtAuthenticationToken);

        return ResponseEntity.ok(serviceInstances);
    }

    @Override
    public ResponseEntity<Void> deleteServiceInstance(
        UUID serviceInstanceId)
            throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException,
            ServiceOfferingVersionNotFoundException, SSLException, CapabilityServiceNotFoundException {

        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        this.serviceInstancesHandler.deleteServiceInstanceOfUser(serviceInstanceId, jwtAuthenticationToken);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<AvailableServiceInstanceVersionChange>> getAvailableVersionChangesForServiceInstance(
            UUID serviceInstanceId
    ) throws ConsulLoginFailedException, ServiceInstanceNotFoundException,
            ServiceOfferingNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var availableVersionChanges = this.serviceInstancesHandler
                .getAvailableVersionChangesForServiceInstance(serviceInstanceId, jwtAuthenticationToken);

        return ResponseEntity.ok(availableVersionChanges);
    }

    @Override
    public ResponseEntity<Void> updateServiceInstanceToVersion(
            UUID serviceInstanceId,
            UUID targetServiceOfferingVersionId
    ) throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException,
            ServiceInstanceUpdateException, SSLException, JsonProcessingException, ServiceOptionNotFoundException,
            InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        this.serviceInstancesHandler.updateServiceInstanceToVersion(serviceInstanceId, targetServiceOfferingVersionId, jwtAuthenticationToken);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<ServiceOrder>> getOrdersOfServiceInstance(
            UUID serviceInstanceId
    ) {
        var orders = this.serviceInstancesHandler.getOrdersOfServiceInstance(serviceInstanceId);
        return ResponseEntity.ok(orders);
    }

    @Override
    public ResponseEntity<ServiceInstanceDetails> getServiceInstanceDetails(
            UUID serviceInstanceId
    ) throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, ServiceInstanceRuntimeException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var serviceInstanceDetails = this.serviceInstancesHandler
                .getServiceInstanceDetails(jwtAuthenticationToken, serviceInstanceId);

        return ResponseEntity.ok(serviceInstanceDetails);
    }

    @Override
    public ResponseEntity<Void> setGroupsOfServiceInstance(
            UUID serviceInstanceId,
            List<UUID> groupIds
    ) throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceInstanceGroupNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        this.serviceInstancesHandler.setGroupsForServiceInstance(jwtAuthenticationToken, groupIds, serviceInstanceId);

        return ResponseEntity.ok().build();
    }
}

