package org.eclipse.slm.service_management.service.rest.service_offerings;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.minio.model.exceptions.*;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.resource_management.model.resource.MatchingResourceDTO;
import org.eclipse.slm.resource_management.service.client.handler.ApiException;
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.service.rest.service_categories.ServiceCategoryNotFoundException;
import org.eclipse.slm.service_management.service.rest.service_deployment.CapabilityServiceNotFoundException;
import org.eclipse.slm.service_management.model.offerings.ServiceOrder;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersionDTOApi;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionCreateException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirement;
import org.eclipse.slm.service_management.model.offerings.responses.ServiceOfferingVersionCreateResponse;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services/offerings/{serviceOfferingId}/versions")
public class ServiceOfferingVersionsRestController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingVersionsRestController.class);

    private final ServiceOfferingVersionHandler serviceOfferingVersionHandler;

    private final ServiceOfferingOrderHandler serviceOfferingOrderHandler;


    public ServiceOfferingVersionsRestController(
            ServiceOfferingVersionHandler serviceOfferingVersionHandler,
            ServiceOfferingOrderHandler serviceOfferingOrderHandler) {
        this.serviceOfferingVersionHandler = serviceOfferingVersionHandler;
        this.serviceOfferingOrderHandler = serviceOfferingOrderHandler;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service offering versions of service offering")
    public ResponseEntity<List<ServiceOfferingVersionDTOApi>> getServiceOfferingVersionsOfServiceOffering(
            @PathVariable("serviceOfferingId") UUID serviceOfferingId
    ) throws ServiceOfferingNotFoundException {
        var serviceOfferingVersions = this.serviceOfferingVersionHandler
                .getServiceOfferingVersionsOfServiceOffering(serviceOfferingId);

        var serviceOfferingVersionsDTOApi = ObjectMapperUtils
                .mapAll(serviceOfferingVersions, ServiceOfferingVersionDTOApi.class);

        return ResponseEntity.ok(serviceOfferingVersionsDTOApi);
    }

    @RequestMapping(value = "/{serviceOfferingVersionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service offering version by id")
    public ResponseEntity<ServiceOfferingVersionDTOApi> getServiceOfferingVersionById(
            @PathVariable("serviceOfferingId") UUID serviceOfferingId,
            @PathVariable("serviceOfferingVersionId") UUID serviceOfferingVersionId) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .getServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersionId);

        var serviceOfferingVersionDTOApi = ObjectMapperUtils
                .map(serviceOfferingVersion, ServiceOfferingVersionDTOApi.class);

        return ResponseEntity.ok(serviceOfferingVersionDTOApi);
    }

    @RequestMapping(value = "/{serviceOfferingVersionId}/requirements", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service offering version requirements by id")
    public ResponseEntity<List<ServiceRequirement>> getServiceOfferingVersionRequirementsById(
            @PathVariable("serviceOfferingId") UUID serviceOfferingId,
            @PathVariable("serviceOfferingVersionId") UUID serviceOfferingVersionId) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .getServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersionId);

        return ResponseEntity.ok(serviceOfferingVersion.getServiceRequirements());
    }

    @RequestMapping(value = "/{serviceOfferingVersionId}/requirements", method = RequestMethod.PUT)
    @Operation(summary = "Create or update requirements for service offering version")
    public @ResponseBody ResponseEntity createOrUpdateServiceOfferingVersionRequirementsWithId(
            @PathVariable("serviceOfferingId") UUID serviceOfferingId,
            @PathVariable("serviceOfferingVersionId") UUID serviceOfferingVersionId,
            @RequestBody List<ServiceRequirement> requirements) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .getServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersionId);
        serviceOfferingVersion.setServiceRequirements(requirements);

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @Operation(summary = "Create new service offering version")
    public ResponseEntity<ServiceOfferingVersionCreateResponse> createServiceOfferingVersionWithAutoGeneratedId(
            @PathVariable("serviceOfferingId") UUID serviceOfferingId,
            @RequestBody ServiceOfferingVersionDTOApi serviceOfferingVersionDTOApi)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionCreateException {

        serviceOfferingVersionDTOApi.setServiceOfferingId(serviceOfferingId);
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .createServiceOfferingVersionWithAutoGeneratedId(serviceOfferingVersionDTOApi);

        return ResponseEntity.ok(new ServiceOfferingVersionCreateResponse(serviceOfferingVersion));
    }

    @RequestMapping(value = "/{serviceOfferingVersionId}", method = RequestMethod.PUT)
    @Operation(summary = "Create new service offering version with specified id or update existing one")
    public ResponseEntity<ServiceOfferingVersionCreateResponse> createOrUpdateServiceOfferingVersionWithId(
            @PathVariable("serviceOfferingId") UUID serviceOfferingId,
            @PathVariable("serviceOfferingVersionId") UUID serviceOfferingVersionId,
            @RequestBody ServiceOfferingVersionDTOApi serviceOfferingVersionDTOApi)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionCreateException {

        serviceOfferingVersionDTOApi.setServiceOfferingId(serviceOfferingId);
        serviceOfferingVersionDTOApi.setId(serviceOfferingVersionId);
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .createOrUpdateServiceOfferingVersionWithId(serviceOfferingVersionDTOApi);

        return ResponseEntity.ok(new ServiceOfferingVersionCreateResponse(serviceOfferingVersion));
    }

    @RequestMapping(value = "/{serviceOfferingVersionId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete service offering version")
    public @ResponseBody ResponseEntity<Void> deleteServiceOfferingVersion(
            @PathVariable("serviceOfferingId") UUID serviceOfferingId,
            @PathVariable("serviceOfferingVersionId") UUID serviceOfferingVersionId
    ) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, ServiceVendorNotFoundException, ServiceCategoryNotFoundException {
        this.serviceOfferingVersionHandler.deleteServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersionId);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{serviceOfferingVersionId}/order", method = RequestMethod.POST)
    @Operation(summary = "Order service offering version")
    public ResponseEntity<Void> orderServiceOfferingVersionById(
            KeycloakAuthenticationToken keycloakAuthenticationToken,
            @PathVariable("serviceOfferingId") UUID serviceOfferingId,
            @PathVariable("serviceOfferingVersionId") UUID serviceOfferingVersionId,
            @RequestBody ServiceOrder serviceOrder,
            @RequestParam UUID deploymentCapabilityServiceId)
            throws SSLException, JsonProcessingException, ServiceOptionNotFoundException, ApiException,
            ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException, ConsulLoginFailedException {

        var keycloakPrincipal = (KeycloakPrincipal)keycloakAuthenticationToken.getPrincipal();
        this.serviceOfferingOrderHandler
                .orderServiceOfferingById(serviceOfferingId, serviceOfferingVersionId,
                        serviceOrder, deploymentCapabilityServiceId, keycloakPrincipal);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{serviceOfferingVersionId}/matching-resources", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get possible resources matching service requirements")
    public @ResponseBody ResponseEntity<List<MatchingResourceDTO>> getResourcesMatchingServiceRequirements(
            @PathVariable("serviceOfferingId") UUID serviceOfferingId,
            @PathVariable("serviceOfferingVersionId") UUID serviceOfferingVersionId)
            throws ApiException, SSLException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var matchingResources = this.serviceOfferingOrderHandler
                .getCapabilityServicesMatchingServiceRequirements(serviceOfferingId, serviceOfferingVersionId, keycloakPrincipal);
        return ResponseEntity.ok().body(matchingResources);
    }

    @RequestMapping(value = "/{serviceOfferingVersionId}/file", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, method = RequestMethod.POST)
    @Operation(summary = "Upload new service offering file for deployment ")
    public ResponseEntity<Void> createOrUpdateServiceOfferingFileWithId(
            @PathVariable("serviceOfferingId") UUID serviceOfferingId,
            @PathVariable("serviceOfferingVersionId") UUID serviceOfferingVersionId,
            @RequestPart("file") MultipartFile file
    )
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, MinioUploadException, MinioBucketCreateException, MinioRemoveObjectException, MinioObjectPathNameException, MinioBucketNameException {

        this.serviceOfferingVersionHandler
                .createOrUpdateServiceOfferingFile(serviceOfferingId, serviceOfferingVersionId, file);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{serviceOfferingVersionId}/file/{fileName}", method = RequestMethod.GET)
    @Operation(summary = "Download service offering file ")
    public ResponseEntity<InputStreamResource> getServiceOfferingFileWithId(
            @PathVariable("serviceOfferingId") UUID serviceOfferingId,
            @PathVariable("serviceOfferingVersionId") UUID serviceOfferingVersionId,
            @PathVariable("fileName") String fileName
    ) throws Exception {

        var result = this.serviceOfferingVersionHandler
                .getServiceOfferingFile(serviceOfferingId, serviceOfferingVersionId, fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.getFileName())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE).body(new InputStreamResource(result.getFileStream()));
    }

}
