package org.eclipse.slm.service_management.service.rest.service_repositories;

import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepository;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepositoryDTOApiRead;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepositoryCreateResponse;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorAccessDenied;
import io.swagger.v3.oas.annotations.Operation;
import org.keycloak.KeycloakPrincipal;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (this.checkUserPermissions(keycloakPrincipal, serviceVendorId)) {
            var serviceRepositories = this.serviceRepositoryHandler.getRepositoriesOfServiceVendor(serviceVendorId);

            List<ServiceRepositoryDTOApiRead> serviceRepositoriesDTO =
                    ObjectMapperUtils.modelMapper.map(serviceRepositories, new TypeToken<List<ServiceRepositoryDTOApiRead>>() {}.getType());

            return ResponseEntity.ok(serviceRepositoriesDTO);
        }
        else {
            throw this.getNoPermissionException(keycloakPrincipal, serviceVendorId);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @Operation(summary = "Create repository containing files for service offerings")
    @ResponseBody
    public ResponseEntity<ServiceRepositoryCreateResponse> createRepository(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @RequestBody ServiceRepository serviceRepository
    ) throws ServiceVendorAccessDenied {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (this.checkUserPermissions(keycloakPrincipal, serviceVendorId)) {
            serviceRepository.setId(UUID.randomUUID());
            serviceRepository.setServiceVendorId(serviceVendorId);
            serviceRepository = this.serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepository);

            var response = new ServiceRepositoryCreateResponse(serviceRepository.getId());

            return ResponseEntity.ok(response);
        }
        else {
            throw this.getNoPermissionException(keycloakPrincipal, serviceVendorId);
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
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (this.checkUserPermissions(keycloakPrincipal, serviceVendorId)) {
            serviceRepository.setId(serviceRepositoryId);
            serviceRepository.setServiceVendorId(serviceVendorId);
            serviceRepository = this.serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepository);

            var response = new ServiceRepositoryCreateResponse(serviceRepository.getId());

            return ResponseEntity.ok(response);
        }
        else {
            throw this.getNoPermissionException(keycloakPrincipal, serviceVendorId);
        }
    }

    @RequestMapping(value = "/{repositoryId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete repository containing files for service offerings")
    @ResponseBody
    public ResponseEntity deleteRepository(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @PathVariable(name = "repositoryId") UUID repositoryId
    ) throws ServiceVendorAccessDenied {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (this.checkUserPermissions(keycloakPrincipal, serviceVendorId)) {
            this.serviceRepositoryHandler.deleteServiceRepository(serviceVendorId, repositoryId);

            return ResponseEntity.ok().build();
        }
        else {
            throw this.getNoPermissionException(keycloakPrincipal, serviceVendorId);
        }
    }

    private boolean checkUserPermissions(KeycloakPrincipal keycloakPrincipal, UUID serviceVendorId)
            throws ServiceVendorAccessDenied {
        var token = keycloakPrincipal.getKeycloakSecurityContext().getToken();
        var otherClaims = token.getOtherClaims();

        var userRealmAccessRoles = token.getRealmAccess().getRoles();
        if (userRealmAccessRoles.contains("slm-admin"))
        {
            return true;
        }

        if (otherClaims.containsKey("groups")) {
            var userGroups = (ArrayList) otherClaims.get("groups");
            if (userGroups.contains("vendor_" + serviceVendorId)) {
                return true;
            }
        }

        return false;
    }

    private ServiceVendorAccessDenied getNoPermissionException(KeycloakPrincipal keycloakPrincipal, UUID serviceVendorId) {
        var token = keycloakPrincipal.getKeycloakSecurityContext().getToken();
        return new ServiceVendorAccessDenied("User '" + token.getId() + "' has no permissions for service vendor '" + serviceVendorId + "'");
    }
}
