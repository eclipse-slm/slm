package org.eclipse.slm.service_management.service.app.service_vendors;

import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakGroupNotFoundException;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakUserNotFoundException;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmAdminOrApiKey;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmUserOrApiKey;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.exceptions.ServiceVendorRuntimeException;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.model.vendors.ServiceVendorDTOApi;
import org.eclipse.slm.service_management.model.vendors.ServiceVendorDeveloper;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.eclipse.slm.service_management.model.vendors.responses.ServiceVendorCreateResponse;
import org.eclipse.slm.service_management.persistence.keycloak.ServiceVendorRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ServiceVendorsRestApiConfig.BASE_PATH)
@Tag(name = ServiceVendorsRestApiConfig.TAG)
@AuthorizedAsSlmUserOrApiKey
public class ServiceVendorsRestController implements ServiceVendorsRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceVendorsRestController.class);

    private final ServiceVendorRepository serviceVendorRepository;

    @Autowired
    public ServiceVendorsRestController(ServiceVendorRepository serviceVendorRepository) {
        this.serviceVendorRepository = serviceVendorRepository;
    }

    @Override
    public ResponseEntity<List<ServiceVendor>> getServiceVendors(boolean withImage) {
        var serviceVendors = this.serviceVendorRepository.getServiceVendors();
        if (!withImage) {
            for (var serviceVendor : serviceVendors) {
                serviceVendor.setLogo(null);
            }
        }

        return ResponseEntity.ok(serviceVendors);
    }

    @Override
    public ResponseEntity<ServiceVendor> getServiceVendorById(UUID serviceVendorId, boolean withImage) throws ServiceVendorNotFoundException {
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

    @Override
    @AuthorizedAsSlmAdminOrApiKey
    public ResponseEntity<ServiceVendorCreateResponse> createServiceVendor(ServiceVendorDTOApi serviceVendorDTOApi) {
        serviceVendorDTOApi.setId(UUID.randomUUID());
        var serviceVendorToCreate = ObjectMapperUtils.map(serviceVendorDTOApi, ServiceVendor.class);

        var createdServiceVendor = this.serviceVendorRepository.createOrUpdateServiceVendorWithId(serviceVendorToCreate, "fabos");
        var response = new ServiceVendorCreateResponse(createdServiceVendor.getId());

        return ResponseEntity.ok(response);
    }

    @Override
    @AuthorizedAsSlmAdminOrApiKey
    public ResponseEntity<Void> createOrUpdateServiceVendorWithId(UUID serviceVendorId, ServiceVendorDTOApi serviceVendorDTOApi) {
        serviceVendorDTOApi.setId(serviceVendorId);
        var serviceVendorToCreateOrUpdate = ObjectMapperUtils.map(serviceVendorDTOApi, ServiceVendor.class);

        this.serviceVendorRepository.createOrUpdateServiceVendorWithId(serviceVendorToCreateOrUpdate, "fabos");

        return ResponseEntity.ok().build();
    }

    @Override
    @AuthorizedAsSlmAdminOrApiKey
    public ResponseEntity<Void> deleteServiceVendor(UUID serviceVendorId) throws ServiceVendorNotFoundException, ServiceVendorRuntimeException {
        this.serviceVendorRepository.deleteServiceVendorById(serviceVendorId, "fabos");

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<byte[]> getLogoOfServiceVendor(UUID serviceVendorId) throws ServiceVendorNotFoundException {
        var serviceVendorLogo = this.serviceVendorRepository.getServiceVendorLogo(serviceVendorId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;")
                .body(serviceVendorLogo);
    }

    @Override
    public ResponseEntity<List<ServiceVendorDeveloper>> getDevelopersOfServiceVendor(UUID serviceVendorId)
            throws KeycloakGroupNotFoundException, ServiceVendorNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var realm =  KeycloakTokenUtil.getRealm(jwtAuthenticationToken);
        var serviceVendorDevelopers = this.serviceVendorRepository.getDevelopersOfServiceVendor(serviceVendorId, realm);

        return ResponseEntity.ok(serviceVendorDevelopers);
    }

    @Override
    public ResponseEntity<Void> addDeveloperToServiceVendor(UUID serviceVendorId, UUID userId)
            throws ServiceVendorNotFoundException, KeycloakUserNotFoundException, KeycloakGroupNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var realm =  KeycloakTokenUtil.getRealm(jwtAuthenticationToken);
        this.serviceVendorRepository.addDeveloperToServiceVendor(serviceVendorId, userId, realm);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> removeDeveloperFromServiceVendor(UUID serviceVendorId, UUID userId)
            throws ServiceVendorNotFoundException, KeycloakUserNotFoundException, KeycloakGroupNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var realm =  KeycloakTokenUtil.getRealm(jwtAuthenticationToken);
        this.serviceVendorRepository.removeDeveloperFromServiceVendor(serviceVendorId, userId, realm);

        return ResponseEntity.ok().build();
    }

}
