package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmUser;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstancesGroupsRestApi;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstancesGroupsRestApiConfig;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceGroup;
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
@AuthorizedAsSlmUser
public class ServiceInstancesGroupsRestController implements ServiceInstancesGroupsRestApi {

    private final ServiceInstanceGroupJpaRepository serviceInstanceGroupJpaRepository;

    @Autowired
    public ServiceInstancesGroupsRestController(ServiceInstanceGroupJpaRepository serviceInstanceGroupJpaRepository) {
        this.serviceInstanceGroupJpaRepository = serviceInstanceGroupJpaRepository;
    }

    @Override
    public ResponseEntity<List<ServiceInstanceGroup>> getServiceInstanceGroups(Optional<UUID> filterById) {
        if(filterById.isPresent()) {
            var optionalLocation = serviceInstanceGroupJpaRepository.findById(filterById.get());
            return ResponseEntity.ok(Arrays.asList(optionalLocation.get()));
        } else {
            return ResponseEntity.ok(serviceInstanceGroupJpaRepository.findAll());
        }
    }

    @Override
    public ResponseEntity<ServiceInstanceGroup> createServiceInstanceGroup(ServiceInstanceGroup group){
        group.setId(UUID.randomUUID());
        group = serviceInstanceGroupJpaRepository.save(group);

        return ResponseEntity.ok(group);
    }

    @Override
    public ResponseEntity<ServiceInstanceGroup> createOrUpdateServiceInstanceGroup(UUID serviceInstanceGroupId, ServiceInstanceGroup group){
        group.setId(serviceInstanceGroupId);
        group = serviceInstanceGroupJpaRepository.save(group);

        return ResponseEntity.ok(group);
    }

    @Override
    public ResponseEntity<Void> deleteServiceInstanceGroup(UUID id) {
        serviceInstanceGroupJpaRepository.deleteById(id);

        return ResponseEntity.ok().build();
    }
}

