package org.eclipse.slm.service_management.service.rest.service_instances;

import org.eclipse.slm.service_management.model.services.ServiceInstanceGroup;
import org.eclipse.slm.service_management.persistence.api.ServiceInstanceGroupJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/services/instances/groups")
public class ServiceInstancesGroupsRestController {

    private final ServiceInstancesHandler serviceInstancesHandler;

    private final ServiceInstanceGroupJpaRepository serviceInstanceGroupJpaRepository;

    @Autowired
    public ServiceInstancesGroupsRestController(ServiceInstancesHandler serviceInstancesHandler, ServiceInstanceGroupJpaRepository serviceInstanceGroupJpaRepository) {
        this.serviceInstancesHandler = serviceInstancesHandler;
        this.serviceInstanceGroupJpaRepository = serviceInstanceGroupJpaRepository;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get service instance groups")
    public ResponseEntity<List<ServiceInstanceGroup>> getServiceInstanceGroups(
            @RequestParam(name = "filterById", required = false) Optional<UUID> filterById
    ) {
        if(filterById.isPresent()) {
            var optionalLocation = serviceInstanceGroupJpaRepository.findById(filterById.get());
            return ResponseEntity.ok(Arrays.asList(optionalLocation.get()));
        } else {
            return ResponseEntity.ok(serviceInstanceGroupJpaRepository.findAll());
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @Operation(summary = "Create service instance group")
    public ResponseEntity<ServiceInstanceGroup> createServiceInstanceGroup(
            @RequestBody ServiceInstanceGroup group
    ){
        group.setId(UUID.randomUUID());
        group = serviceInstanceGroupJpaRepository.save(group);

        return ResponseEntity.ok(group);
    }

    @RequestMapping(value = "{serviceInstanceGroupId}", method = RequestMethod.PUT)
    @Operation(summary = "Create or update service instance group")
    public ResponseEntity<ServiceInstanceGroup> createOrUpdateServiceInstanceGroup(
            @PathVariable(name = "serviceInstanceGroupId") UUID serviceInstanceGroupId,
            @RequestBody ServiceInstanceGroup group
    ){
        group.setId(serviceInstanceGroupId);
        group = serviceInstanceGroupJpaRepository.save(group);

        return ResponseEntity.ok(group);
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @Operation(summary = "Delete service instance group")
    public ResponseEntity deleteServiceInstanceGroup(
            @RequestParam(name = "id") UUID id
    ) {
        serviceInstanceGroupJpaRepository.deleteById(id);

        return ResponseEntity.ok().build();
    }
}
