package org.eclipse.slm.service_management.service.rest.service_vendors;

import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakGroupNotFoundException;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakUserNotFoundException;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.model.vendors.ServiceVendorDTOApi;
import org.eclipse.slm.service_management.model.vendors.ServiceVendorDeveloper;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.eclipse.slm.service_management.model.vendors.responses.ServiceVendorCreateResponse;
import org.eclipse.slm.service_management.persistence.keycloak.ServiceVendorRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services/vendors")
public class ServiceVendorsRestController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceVendorsRestController.class);

    private final ServiceVendorRepository serviceVendorRepository;

    @Autowired
    public ServiceVendorsRestController(
            ServiceVendorRepository serviceVendorRepository
    ) {
        this.serviceVendorRepository = serviceVendorRepository;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service vendors")
    public ResponseEntity<List<ServiceVendor>> getServiceVendors(
            @RequestParam(name = "withImage", required = false, defaultValue = "false") boolean withImage) {
        var serviceVendors = this.serviceVendorRepository.getServiceVendors();
        if (!withImage) {
            for (var serviceVendor : serviceVendors) {
                serviceVendor.setLogo(null);
            }
        }

        return ResponseEntity.ok(serviceVendors);
    }

    @RequestMapping(value = "/{serviceVendorId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service vendor by id")
    public ResponseEntity<ServiceVendor> getServiceVendorById(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @RequestParam(name = "withImage", required = false, defaultValue = "false") boolean withImage
    ) throws ServiceVendorNotFoundException {
        var serviceVendorOptional = this.serviceVendorRepository.getServiceVendorById(serviceVendorId);
        if (serviceVendorOptional.isPresent()) {
            var serviceVendor = serviceVendorOptional.get();
            if (withImage) {
                serviceVendor.setLogo(null);
            }
            return ResponseEntity.ok(serviceVendor);
        }
        else {
            throw new ServiceVendorNotFoundException(serviceVendorId);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceVendorCreateResponse> createServiceVendor(
            @RequestBody ServiceVendorDTOApi serviceVendorDTOApi) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var realm =  KeycloakTokenUtil.getRealm(jwtAuthenticationToken);
        serviceVendorDTOApi.setId(UUID.randomUUID());
        var serviceVendorToCreate = ObjectMapperUtils.map(serviceVendorDTOApi, ServiceVendor.class);

        var createdServiceVendor = this.serviceVendorRepository.createOrUpdateServiceVendorWithId(serviceVendorToCreate, realm);
        var response = new ServiceVendorCreateResponse(createdServiceVendor.getId());

        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{serviceVendorId}", method = RequestMethod.PUT)
    public ResponseEntity<Void> createOrUpdateServiceVendorWithId(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @RequestBody ServiceVendorDTOApi serviceVendorDTOApi) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var realm =  KeycloakTokenUtil.getRealm(jwtAuthenticationToken);

        serviceVendorDTOApi.setId(serviceVendorId);
        var serviceVendorToCreateOrUpdate = ObjectMapperUtils.map(serviceVendorDTOApi, ServiceVendor.class);

        this.serviceVendorRepository.createOrUpdateServiceVendorWithId(serviceVendorToCreateOrUpdate, realm);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{serviceVendorId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteServiceVendor(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId
    ) throws ServiceVendorNotFoundException, KeycloakGroupNotFoundException {
        this.serviceVendorRepository.deleteServiceVendorById(serviceVendorId, "fabos");

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{serviceVendorId}/logo", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getLogoOfServiceVendor(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId
    ) throws ServiceVendorNotFoundException {
        var serviceVendorLogo = this.serviceVendorRepository.getServiceVendorLogo(serviceVendorId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;")
                .body(serviceVendorLogo);
    }

    @RequestMapping(value = "/{serviceVendorId}/developers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ServiceVendorDeveloper>> getDevelopersOfServiceVendor(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId) throws KeycloakGroupNotFoundException, ServiceVendorNotFoundException
    {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var realm =  KeycloakTokenUtil.getRealm(jwtAuthenticationToken);
        var serviceVendorDevelopers = this.serviceVendorRepository.getDevelopersOfServiceVendor(serviceVendorId, realm);

        return ResponseEntity.ok(serviceVendorDevelopers);
    }

    @RequestMapping(value = "/{serviceVendorId}/developers/{userId}", method = RequestMethod.PUT)
    public ResponseEntity<Void> addDeveloperToServiceVendor(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @PathVariable(name = "userId") UUID userId)
        throws ServiceVendorNotFoundException, KeycloakUserNotFoundException, KeycloakGroupNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var realm =  KeycloakTokenUtil.getRealm(jwtAuthenticationToken);
        this.serviceVendorRepository.addDeveloperToServiceVendor(serviceVendorId, userId, realm);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{serviceVendorId}/developers/{userId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeDeveloperFromServiceVendor(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @PathVariable(name = "userId") UUID userId)
        throws ServiceVendorNotFoundException, KeycloakUserNotFoundException, KeycloakGroupNotFoundException
    {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var realm =  KeycloakTokenUtil.getRealm(jwtAuthenticationToken);
        this.serviceVendorRepository.removeDeveloperFromServiceVendor(serviceVendorId, userId, realm);

        return ResponseEntity.ok().build();
    }

}
