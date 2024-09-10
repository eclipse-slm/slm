package org.eclipse.slm.service_management.service.rest.service_repositories;

import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepository;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepositoryDTOApiRead;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepositoryCreateResponse;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorAccessDenied;
import io.swagger.v3.oas.annotations.Operation;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/services/vendors/{serviceVendorId}/repositories")
public class ServiceRepositoriesRestController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceRepositoriesRestController.class);

    @Autowired
    private ServiceRepositoryHandler serviceRepositoryHandler;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get repositories containing files for service offerings")
    @ResponseBody
    public ResponseEntity<List<ServiceRepositoryDTOApiRead>> getRepositories(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId
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

    @RequestMapping(value = "", method = RequestMethod.POST)
    @Operation(summary = "Create repository containing files for service offerings")
    @ResponseBody
    public ResponseEntity<ServiceRepositoryCreateResponse> createRepository(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @RequestBody ServiceRepository serviceRepository
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

    @RequestMapping(value = "/{serviceRepositoryId}", method = RequestMethod.PUT)
    @Operation(summary = "Create or update a repository containing files for service offerings")
    @ResponseBody
    public ResponseEntity<ServiceRepositoryCreateResponse> createOrUpdateRepository(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @PathVariable(name = "serviceRepositoryId") UUID serviceRepositoryId,
            @RequestBody ServiceRepository serviceRepository
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

    @RequestMapping(value = "/{repositoryId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete repository containing files for service offerings")
    @ResponseBody
    public ResponseEntity deleteRepository(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @PathVariable(name = "repositoryId") UUID repositoryId
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
            var userGroups = (ArrayList<String>) claims.get("groups");
            if (userGroups.contains("vendor_" + serviceVendorId)) {
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
