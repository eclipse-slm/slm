package org.eclipse.slm.service_management.features.service_offerings.impl.servicerepositories;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmUserOrApiKey;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.ServiceRepositoriesRestApi;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.ServiceRepositoriesRestApiConfig;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.ServiceRepositoryNotFound;
import org.eclipse.slm.service_management.features.service_offerings.api.servicerepositories.ServiceRepository;
import org.eclipse.slm.service_management.features.service_offerings.api.servicerepositories.ServiceRepositoryCreateResponse;
import org.eclipse.slm.service_management.features.service_offerings.api.servicerepositories.ServiceRepositoryDTOApiRead;
import org.eclipse.slm.service_management.features.service_offerings.api.servicevendors.exceptions.ServiceVendorAccessDenied;
import org.modelmapper.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.vault.authentication.UsernamePasswordAuthentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(ServiceRepositoriesRestApiConfig.BASE_PATH)
@Tag(name = ServiceRepositoriesRestApiConfig.TAG)
@AuthorizedAsSlmUserOrApiKey
public class ServiceRepositoriesRestController implements ServiceRepositoriesRestApi {

    private final ServiceRepositoryHandler serviceRepositoryHandler;

    public ServiceRepositoriesRestController(ServiceRepositoryHandler serviceRepositoryHandler) {
        this.serviceRepositoryHandler = serviceRepositoryHandler;
    }

    @Override
    public ResponseEntity<List<ServiceRepositoryDTOApiRead>> getRepositories(
            UUID serviceVendorId
    ) throws ServiceVendorAccessDenied, ServiceRepositoryNotFound {
        var authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (this.checkUserPermissions(authentication, serviceVendorId)) {
            var serviceRepositories = this.serviceRepositoryHandler.getRepositoriesOfServiceVendor(serviceVendorId);

            List<ServiceRepositoryDTOApiRead> serviceRepositoriesDTO =
                    ObjectMapperUtils.modelMapper.map(serviceRepositories, new TypeToken<List<ServiceRepositoryDTOApiRead>>() {}.getType());

            return ResponseEntity.ok(serviceRepositoriesDTO);
        }
        else {
            throw this.getNoPermissionException(authentication, serviceVendorId);
        }
    }

    @Override
    public ResponseEntity<ServiceRepositoryCreateResponse> createRepository(
            UUID serviceVendorId,
            ServiceRepository serviceRepository
    ) throws ServiceVendorAccessDenied {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (this.checkUserPermissions(authentication, serviceVendorId)) {
            serviceRepository.setId(UUID.randomUUID());
            serviceRepository.setServiceVendorId(serviceVendorId);
            serviceRepository = this.serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepository);

            var response = new ServiceRepositoryCreateResponse(serviceRepository.getId());

            return ResponseEntity.ok(response);
        }
        else {
            throw this.getNoPermissionException(authentication, serviceVendorId);
        }
    }

    @Override
    public ResponseEntity<ServiceRepositoryCreateResponse> createOrUpdateRepository(
            UUID serviceVendorId,
            UUID serviceRepositoryId,
            ServiceRepository serviceRepository
    ) throws ServiceVendorAccessDenied {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (this.checkUserPermissions(authentication, serviceVendorId)) {
            serviceRepository.setId(serviceRepositoryId);
            serviceRepository.setServiceVendorId(serviceVendorId);
            serviceRepository = this.serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepository);

            var response = new ServiceRepositoryCreateResponse(serviceRepository.getId());

            return ResponseEntity.ok(response);
        }
        else {
            throw this.getNoPermissionException(authentication, serviceVendorId);
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

    private boolean checkUserPermissions(Authentication authentication, UUID serviceVendorId) {
        if (authentication instanceof JwtAuthenticationToken) {
            var jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
            var token = jwtAuthenticationToken.getToken();
            var claims = token.getClaims();

            var userRealmAccessRoles = (List<String>) ((Map<String, Object>) claims
                    .getOrDefault("realm_access", Map.of()))
                    .getOrDefault("roles", List.of());
            if (userRealmAccessRoles.contains("slm-admin")) {
                return true;
            }

            if (claims.containsKey("groups")) {
                var userGroups = claims.get("groups");
                List<String> userGroupsCasted;
                if (userGroups instanceof String[]) {
                    userGroupsCasted = List.of((String[]) userGroups);
                } else {
                    userGroupsCasted = (ArrayList<String>) userGroups;
                }
                if (userGroupsCasted.contains("vendor_" + serviceVendorId)) {
                    return true;
                }
            }

            return false;
        }
        else if  (authentication instanceof UsernamePasswordAuthentication) {
            return true;
        }

        return false;
    }

    private ServiceVendorAccessDenied getNoPermissionException(Authentication authentication, UUID serviceVendorId) {
        if (authentication instanceof JwtAuthenticationToken) {
            var jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
            var token = jwtAuthenticationToken.getToken();
            return new ServiceVendorAccessDenied("User '" + token.getId() + "' has no permissions for service vendor '" + serviceVendorId + "'");
        } else {
            return new ServiceVendorAccessDenied("Authentication type '" + authentication.getClass().getName() + "' is not supported to check permissions " +
                    "for service vendor '" + serviceVendorId + "'");
        }
    }
}

