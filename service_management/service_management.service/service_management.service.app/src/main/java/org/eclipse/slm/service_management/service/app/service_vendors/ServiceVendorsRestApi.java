package org.eclipse.slm.service_management.service.app.service_vendors;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakGroupNotFoundException;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakUserNotFoundException;
import org.eclipse.slm.service_management.model.exceptions.ServiceVendorRuntimeException;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.model.vendors.ServiceVendorDTOApi;
import org.eclipse.slm.service_management.model.vendors.ServiceVendorDeveloper;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.eclipse.slm.service_management.model.vendors.responses.ServiceVendorCreateResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

public interface ServiceVendorsRestApi {

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service vendors")
    ResponseEntity<List<ServiceVendor>> getServiceVendors(
            @RequestParam(name = "withImage", required = false, defaultValue = "false") boolean withImage
    );

    @RequestMapping(value = "/{serviceVendorId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service vendor by id")
    ResponseEntity<ServiceVendor> getServiceVendorById(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @RequestParam(name = "withImage", required = false, defaultValue = "false") boolean withImage
    ) throws ServiceVendorNotFoundException;

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ServiceVendorCreateResponse> createServiceVendor(
            @RequestBody ServiceVendorDTOApi serviceVendorDTOApi
    );

    @RequestMapping(value = "/{serviceVendorId}", method = RequestMethod.PUT)
    ResponseEntity<Void> createOrUpdateServiceVendorWithId(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @RequestBody ServiceVendorDTOApi serviceVendorDTOApi
    );

    @RequestMapping(value = "/{serviceVendorId}", method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteServiceVendor(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId
    ) throws ServiceVendorNotFoundException, ServiceVendorRuntimeException;

    @RequestMapping(value = "/{serviceVendorId}/logo", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    ResponseEntity<byte[]> getLogoOfServiceVendor(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId
    ) throws ServiceVendorNotFoundException;

    @RequestMapping(value = "/{serviceVendorId}/developers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ServiceVendorDeveloper>> getDevelopersOfServiceVendor(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId
    ) throws KeycloakGroupNotFoundException, ServiceVendorNotFoundException;

    @RequestMapping(value = "/{serviceVendorId}/developers/{userId}", method = RequestMethod.PUT)
    ResponseEntity<Void> addDeveloperToServiceVendor(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @PathVariable(name = "userId") UUID userId
    ) throws ServiceVendorNotFoundException, KeycloakUserNotFoundException, KeycloakGroupNotFoundException;

    @RequestMapping(value = "/{serviceVendorId}/developers/{userId}", method = RequestMethod.DELETE)
    ResponseEntity<Void> removeDeveloperFromServiceVendor(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @PathVariable(name = "userId") UUID userId
    ) throws ServiceVendorNotFoundException, KeycloakUserNotFoundException, KeycloakGroupNotFoundException;
}
