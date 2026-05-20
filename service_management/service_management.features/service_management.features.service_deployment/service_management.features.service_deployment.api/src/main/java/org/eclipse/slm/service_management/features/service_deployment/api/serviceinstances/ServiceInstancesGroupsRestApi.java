package org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstanceGroup;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceInstancesGroupsRestApi {

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get service instance groups")
    ResponseEntity<List<ServiceInstanceGroup>> getServiceInstanceGroups(
            @RequestParam(name = "filterById", required = false) Optional<UUID> filterById
    );

    @RequestMapping(value = "", method = RequestMethod.POST)
    @Operation(summary = "Create service instance group")
    ResponseEntity<ServiceInstanceGroup> createServiceInstanceGroup(
            @RequestBody ServiceInstanceGroup group
    );

    @RequestMapping(value = "{serviceInstanceGroupId}", method = RequestMethod.PUT)
    @Operation(summary = "Create or update service instance group")
    ResponseEntity<ServiceInstanceGroup> createOrUpdateServiceInstanceGroup(
            @PathVariable(name = "serviceInstanceGroupId") UUID serviceInstanceGroupId,
            @RequestBody ServiceInstanceGroup group
    );

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @Operation(summary = "Delete service instance group")
    ResponseEntity<Void> deleteServiceInstanceGroup(
            @RequestParam(name = "id") UUID id
    );
}

