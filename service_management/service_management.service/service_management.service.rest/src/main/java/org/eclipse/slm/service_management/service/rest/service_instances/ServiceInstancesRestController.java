package org.eclipse.slm.service_management.service.rest.service_instances;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.service.client.handler.ApiException;
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.service.rest.service_deployment.CapabilityServiceNotFoundException;
import org.eclipse.slm.service_management.model.offerings.ServiceOrder;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.services.ServiceInstance;
import org.eclipse.slm.service_management.model.services.ServiceInstanceDetails;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceGroupNotFoundException;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceNotFoundException;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceUpdateException;
import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.service_management.service.rest.service_instances.AvailableServiceInstanceVersionChange;
import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.SSLException;
import java.util.*;

@RestController
@RequestMapping("/services/instances")
public class ServiceInstancesRestController {

    private final ServiceInstancesHandler serviceInstancesHandler;

    @Autowired
    public ServiceInstancesRestController(ServiceInstancesHandler serviceInstancesHandler) {
        this.serviceInstancesHandler = serviceInstancesHandler;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get all services of user")
    public ResponseEntity<List<ServiceInstance>> getServicesOfUser()
            throws ConsulLoginFailedException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var serviceInstances = this.serviceInstancesHandler.getServiceInstancesOfUser(jwtAuthenticationToken);

        return ResponseEntity.ok(serviceInstances);
    }

    @RequestMapping(value = "/{serviceInstanceId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete a service instance")
    public ResponseEntity deleteServiceInstance(
        @PathVariable(name = "serviceInstanceId") UUID serviceInstanceId)
            throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException,
            ServiceOfferingVersionNotFoundException, SSLException, ApiException, CapabilityServiceNotFoundException {

        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        this.serviceInstancesHandler.deleteServiceInstanceOfUser(serviceInstanceId, jwtAuthenticationToken);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{serviceInstanceId}/versions", method = RequestMethod.GET)
    @Operation(summary = "Get available version changes for service instance")
    public ResponseEntity<List<AvailableServiceInstanceVersionChange>> getAvailableVersionChangesForServiceInstance(
            @PathVariable(name = "serviceInstanceId") UUID serviceInstanceId
    ) throws ConsulLoginFailedException, ServiceInstanceNotFoundException,
            ServiceOfferingNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var availableVersionChanges = this.serviceInstancesHandler
                .getAvailableVersionChangesForServiceInstance(serviceInstanceId, jwtAuthenticationToken);

        return ResponseEntity.ok(availableVersionChanges);
    }

    @RequestMapping(value = "/{serviceInstanceId}/versions", method = RequestMethod.POST)
    @Operation(summary = "Change service instance to version")
    public ResponseEntity updateServiceInstanceToVersion(
            @PathVariable(name = "serviceInstanceId") UUID serviceInstanceId,
            @RequestParam(name = "targetServiceOfferingVersionId") UUID targetServiceOfferingVersionId
    ) throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException,
            ServiceInstanceUpdateException, SSLException, JsonProcessingException, ServiceOptionNotFoundException,
            ApiException, InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        this.serviceInstancesHandler.updateServiceInstanceToVersion(serviceInstanceId, targetServiceOfferingVersionId, jwtAuthenticationToken);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{serviceInstanceId}/orders", method = RequestMethod.GET)
    @Operation(summary = "Get orders of service instance")
    public ResponseEntity<List<ServiceOrder>> getOrdersOfServiceInstance(
            @PathVariable(name = "serviceInstanceId") UUID serviceInstanceId
    ) {
        var orders = this.serviceInstancesHandler.getOrdersOfServiceInstance(serviceInstanceId);
        return ResponseEntity.ok(orders);
    }

    @RequestMapping(value = "/{serviceInstanceId}/details", method = RequestMethod.GET)
    @Operation(summary = "Get details of service instance")
    public ResponseEntity<ServiceInstanceDetails> getServiceInstanceDetails(
            @PathVariable(name = "serviceInstanceId") UUID serviceInstanceId
    ) throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var serviceInstanceDetails = this.serviceInstancesHandler
                .getServiceInstanceDetails(jwtAuthenticationToken, serviceInstanceId);

        return ResponseEntity.ok(serviceInstanceDetails);
    }

    @RequestMapping(value = "/{serviceInstanceId}/groups", method = RequestMethod.PUT)
    @Operation(summary = "Set groups for service instance")
    public ResponseEntity setGroupsOfServiceInstance(
            @PathVariable(name = "serviceInstanceId") UUID serviceInstanceId,
            @RequestBody List<UUID> groupIds
    ) throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceInstanceGroupNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        this.serviceInstancesHandler.setGroupsForServiceInstance(jwtAuthenticationToken, groupIds, serviceInstanceId);

        return ResponseEntity.ok().build();
    }
}
