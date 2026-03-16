package org.eclipse.slm.service_management.service.app.service_instances;

import org.eclipse.slm.service_management.model.services.ServiceInstanceGroup;
import org.eclipse.slm.service_management.persistence.api.ServiceInstanceGroupJpaRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(ServiceInstancesGroupsRestApiConfig.BASE_PATH)
@Tag(name = ServiceInstancesGroupsRestApiConfig.TAG)
public class ServiceInstancesGroupsRestController implements ServiceInstancesGroupsRestApi {

    private final ServiceInstancesHandler serviceInstancesHandler;

    private final ServiceInstanceGroupJpaRepository serviceInstanceGroupJpaRepository;

    @Autowired
    public ServiceInstancesGroupsRestController(ServiceInstancesHandler serviceInstancesHandler, ServiceInstanceGroupJpaRepository serviceInstanceGroupJpaRepository) {
        this.serviceInstancesHandler = serviceInstancesHandler;
        this.serviceInstanceGroupJpaRepository = serviceInstanceGroupJpaRepository;
    }

    @Override
    public ResponseEntity<List<ServiceInstanceGroup>> getServiceInstanceGroups(
            Optional<UUID> filterById
    ) {
        if(filterById.isPresent()) {
            var optionalLocation = serviceInstanceGroupJpaRepository.findById(filterById.get());
            return ResponseEntity.ok(Arrays.asList(optionalLocation.get()));
        } else {
            return ResponseEntity.ok(serviceInstanceGroupJpaRepository.findAll());
        }
    }

    @Override
    public ResponseEntity<ServiceInstanceGroup> createServiceInstanceGroup(
            ServiceInstanceGroup group
    ){
        group.setId(UUID.randomUUID());
        group = serviceInstanceGroupJpaRepository.save(group);

        return ResponseEntity.ok(group);
    }

    @Override
    public ResponseEntity<ServiceInstanceGroup> createOrUpdateServiceInstanceGroup(
            UUID serviceInstanceGroupId,
            ServiceInstanceGroup group
    ){
        group.setId(serviceInstanceGroupId);
        group = serviceInstanceGroupJpaRepository.save(group);

        return ResponseEntity.ok(group);
    }

    @Override
    public ResponseEntity<Void> deleteServiceInstanceGroup(
            UUID id
    ) {
        serviceInstanceGroupJpaRepository.deleteById(id);

        return ResponseEntity.ok().build();
    }
}
