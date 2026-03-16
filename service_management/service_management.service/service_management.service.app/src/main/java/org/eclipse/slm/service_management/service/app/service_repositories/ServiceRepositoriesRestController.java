package org.eclipse.slm.service_management.service.app.service_repositories;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepository;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepositoryCreateResponse;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepositoryDTOApiRead;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorAccessDenied;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(ServiceRepositoriesRestApiConfig.BASE_PATH)
@Tag(name = ServiceRepositoriesRestApiConfig.TAG)
public class ServiceRepositoriesRestController implements ServiceRepositoriesRestApi {

    @Autowired
    private ServiceRepositoryHandler serviceRepositoryHandler;

    @Override
    public ResponseEntity<List<ServiceRepositoryDTOApiRead>> getRepositories(
            UUID serviceVendorId
    ) throws ServiceVendorAccessDenied, ServiceRepositoryNotFound {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (this.checkUserPermissions(jwtAuthenticationToken, serviceVendorId)) {
            var serviceRepositories = this.serviceRepositoryHandler.getRepositoriesOfServiceVendor(serviceVendorId);

            List<ServiceRepositoryDTOApiRead> serviceRepositoriesDTO =
                    ObjectMapperUtils.modelMapper.map(serviceRepositories, new TypeToken<List<ServiceRepositoryDTOApiRead>>() {}.getType());

            return ResponseEntity.ok(serviceRepositoriesDTO);
        }
        else {
            throw this.getNoPermissionException(jwtAuthenticationToken, serviceVendorId);
        }
    }

    @Override
    public ResponseEntity<ServiceRepositoryCreateResponse> createRepository(
            UUID serviceVendorId,
            ServiceRepository serviceRepository
    ) throws ServiceVendorAccessDenied {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (this.checkUserPermissions(jwtAuthenticationToken, serviceVendorId)) {
            serviceRepository.setId(UUID.randomUUID());
            serviceRepository.setServiceVendorId(serviceVendorId);
            serviceRepository = this.serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepository);

            var response = new ServiceRepositoryCreateResponse(serviceRepository.getId());

            return ResponseEntity.ok(response);
        }
        else {
            throw this.getNoPermissionException(jwtAuthenticationToken, serviceVendorId);
        }
    }

    @Override
    public ResponseEntity<ServiceRepositoryCreateResponse> createOrUpdateRepository(
            UUID serviceVendorId,
            UUID serviceRepositoryId,
            ServiceRepository serviceRepository
    ) throws ServiceVendorAccessDenied {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (this.checkUserPermissions(jwtAuthenticationToken, serviceVendorId)) {
            serviceRepository.setId(serviceRepositoryId);
            serviceRepository.setServiceVendorId(serviceVendorId);
            serviceRepository = this.serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepository);

            var response = new ServiceRepositoryCreateResponse(serviceRepository.getId());

            return ResponseEntity.ok(response);
        }
        else {
            throw this.getNoPermissionException(jwtAuthenticationToken, serviceVendorId);
        }
    }

    @Override
    public ResponseEntity<Void> deleteRepository(
            UUID serviceVendorId,
            UUID repositoryId
    ) throws ServiceVendorAccessDenied {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (this.checkUserPermissions(jwtAuthenticationToken, serviceVendorId)) {
            this.serviceRepositoryHandler.deleteServiceRepository(serviceVendorId, repositoryId);

            return ResponseEntity.ok().build();
        }
        else {
            throw this.getNoPermissionException(jwtAuthenticationToken, serviceVendorId);
        }
    }

    private boolean checkUserPermissions(JwtAuthenticationToken jwtAuthenticationToken, UUID serviceVendorId) {
        var token = jwtAuthenticationToken.getToken();
        var claims = token.getClaims();

        var userRealmAccessRoles = (List<String>)((Map<String,Object>)claims
                .getOrDefault("realm_access", Map.of()))
                .getOrDefault("roles", List.of());
        if (userRealmAccessRoles.contains("slm-admin"))
        {
            return true;
        }

        if (claims.containsKey("groups")) {
            var userGroups = claims.get("groups");
            List<String> userGroupsCasted;
            if (userGroups instanceof String[]) {
                userGroupsCasted = List.of((String[])userGroups);
            } else {
                userGroupsCasted = (ArrayList<String>)userGroups;
            }
            if (userGroupsCasted.contains("vendor_" + serviceVendorId)) {
                return true;
            }
        }

        return false;
    }

    private ServiceVendorAccessDenied getNoPermissionException(JwtAuthenticationToken jwtAuthenticationToken, UUID serviceVendorId) {
        var token = jwtAuthenticationToken.getToken();
        return new ServiceVendorAccessDenied("User '" + token.getId() + "' has no permissions for service vendor '" + serviceVendorId + "'");
    }
}
