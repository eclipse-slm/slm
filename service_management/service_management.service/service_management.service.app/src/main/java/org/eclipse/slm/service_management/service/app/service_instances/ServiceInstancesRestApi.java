package org.eclipse.slm.service_management.service.app.service_instances;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.ServiceOrder;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.services.ServiceInstance;
import org.eclipse.slm.service_management.model.services.ServiceInstanceDetails;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceGroupNotFoundException;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceNotFoundException;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceRuntimeException;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceUpdateException;
import org.eclipse.slm.service_management.service.app.service_deployment.CapabilityServiceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.UUID;

public interface ServiceInstancesRestApi {

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get all services of user")
    ResponseEntity<List<ServiceInstance>> getServicesOfUser() throws ConsulLoginFailedException;

    @RequestMapping(value = "/{serviceInstanceId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete a service instance")
    ResponseEntity<Void> deleteServiceInstance(
            @PathVariable(name = "serviceInstanceId") UUID serviceInstanceId
    ) throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException,
            ServiceOfferingVersionNotFoundException, SSLException, CapabilityServiceNotFoundException;

    @RequestMapping(value = "/{serviceInstanceId}/versions", method = RequestMethod.GET)
    @Operation(summary = "Get available version changes for service instance")
    ResponseEntity<List<AvailableServiceInstanceVersionChange>> getAvailableVersionChangesForServiceInstance(
            @PathVariable(name = "serviceInstanceId") UUID serviceInstanceId
    ) throws ConsulLoginFailedException, ServiceInstanceNotFoundException, ServiceOfferingNotFoundException;

    @RequestMapping(value = "/{serviceInstanceId}/versions", method = RequestMethod.POST)
    @Operation(summary = "Change service instance to version")
    ResponseEntity<Void> updateServiceInstanceToVersion(
            @PathVariable(name = "serviceInstanceId") UUID serviceInstanceId,
            @RequestParam(name = "targetServiceOfferingVersionId") UUID targetServiceOfferingVersionId
    ) throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException,
            ServiceInstanceUpdateException, SSLException, JsonProcessingException, ServiceOptionNotFoundException,
            InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException;

    @RequestMapping(value = "/{serviceInstanceId}/orders", method = RequestMethod.GET)
    @Operation(summary = "Get orders of service instance")
    ResponseEntity<List<ServiceOrder>> getOrdersOfServiceInstance(
            @PathVariable(name = "serviceInstanceId") UUID serviceInstanceId
    );

    @RequestMapping(value = "/{serviceInstanceId}/details", method = RequestMethod.GET)
    @Operation(summary = "Get details of service instance")
    ResponseEntity<ServiceInstanceDetails> getServiceInstanceDetails(
            @PathVariable(name = "serviceInstanceId") UUID serviceInstanceId
    ) throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceOfferingNotFoundException,
            ServiceOfferingVersionNotFoundException, ServiceInstanceRuntimeException;

    @RequestMapping(value = "/{serviceInstanceId}/groups", method = RequestMethod.PUT)
    @Operation(summary = "Set groups for service instance")
    ResponseEntity<Void> setGroupsOfServiceInstance(
            @PathVariable(name = "serviceInstanceId") UUID serviceInstanceId,
            @RequestBody List<UUID> groupIds
    ) throws ServiceInstanceNotFoundException, ConsulLoginFailedException, ServiceInstanceGroupNotFoundException;
}
