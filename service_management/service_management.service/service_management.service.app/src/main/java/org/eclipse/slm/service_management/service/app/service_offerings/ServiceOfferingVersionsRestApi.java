package org.eclipse.slm.service_management.service.app.service_offerings;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.minio.model.exceptions.MinioBucketCreateException;
import org.eclipse.slm.common.minio.model.exceptions.MinioBucketNameException;
import org.eclipse.slm.common.minio.model.exceptions.MinioObjectPathNameException;
import org.eclipse.slm.common.minio.model.exceptions.MinioRemoveObjectException;
import org.eclipse.slm.common.minio.model.exceptions.MinioUploadException;
import org.eclipse.slm.resource_management.common.model.MatchingResourceDTO;
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersionDTOApi;
import org.eclipse.slm.service_management.model.offerings.ServiceOrder;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirement;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionCreateException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.responses.ServiceOfferingVersionCreateResponse;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.eclipse.slm.service_management.service.app.service_categories.ServiceCategoryNotFoundException;
import org.eclipse.slm.service_management.service.app.service_deployment.CapabilityServiceNotFoundException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.UUID;

public interface ServiceOfferingVersionsRestApi {

    @RequestMapping(value = "/{serviceOfferingId}/versions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service offering versions of service offering")
    ResponseEntity<List<ServiceOfferingVersionDTOApi>> getServiceOfferingVersionsOfServiceOffering(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId
    ) throws ServiceOfferingNotFoundException;

    @RequestMapping(value = "/{serviceOfferingId}/versions/{serviceOfferingVersionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service offering version by id")
    ResponseEntity<ServiceOfferingVersionDTOApi> getServiceOfferingVersionById(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId,
            @PathVariable(name = "serviceOfferingVersionId") UUID serviceOfferingVersionId
    ) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException;

    @RequestMapping(value = "/{serviceOfferingId}/versions/{serviceOfferingVersionId}/requirements", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service offering version requirements by id")
    ResponseEntity<List<ServiceRequirement>> getServiceOfferingVersionRequirementsById(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId,
            @PathVariable(name = "serviceOfferingVersionId") UUID serviceOfferingVersionId
    ) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException;

    @RequestMapping(value = "/{serviceOfferingId}/versions/{serviceOfferingVersionId}/requirements", method = RequestMethod.PUT)
    @Operation(summary = "Create or update requirements for service offering version")
    ResponseEntity<Void> createOrUpdateServiceOfferingVersionRequirementsWithId(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId,
            @PathVariable(name = "serviceOfferingVersionId") UUID serviceOfferingVersionId,
            @RequestBody List<ServiceRequirement> requirements
    ) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException;

    @RequestMapping(value = "/{serviceOfferingId}/versions", method = RequestMethod.POST)
    @Operation(summary = "Create new service offering version")
    ResponseEntity<ServiceOfferingVersionCreateResponse> createServiceOfferingVersionWithAutoGeneratedId(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId,
            @RequestBody ServiceOfferingVersionDTOApi serviceOfferingVersionDTOApi
    ) throws ServiceOfferingNotFoundException, ServiceOfferingVersionCreateException;

    @RequestMapping(value = "/{serviceOfferingId}/versions/{serviceOfferingVersionId}", method = RequestMethod.PUT)
    @Operation(summary = "Create new service offering version with specified id or update existing one")
    ResponseEntity<ServiceOfferingVersionCreateResponse> createOrUpdateServiceOfferingVersionWithId(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId,
            @PathVariable(name = "serviceOfferingVersionId") UUID serviceOfferingVersionId,
            @RequestBody ServiceOfferingVersionDTOApi serviceOfferingVersionDTOApi
    ) throws ServiceOfferingNotFoundException, ServiceOfferingVersionCreateException;

    @RequestMapping(value = "/{serviceOfferingId}/versions/{serviceOfferingVersionId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete service offering version")
    ResponseEntity<Void> deleteServiceOfferingVersion(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId,
            @PathVariable(name = "serviceOfferingVersionId") UUID serviceOfferingVersionId
    ) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, ServiceVendorNotFoundException, ServiceCategoryNotFoundException;

    @RequestMapping(value = "/{serviceOfferingId}/versions/{serviceOfferingVersionId}/order", method = RequestMethod.POST)
    @Operation(summary = "Order service offering version")
    ResponseEntity<Void> orderServiceOfferingVersionById(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId,
            @PathVariable(name = "serviceOfferingVersionId") UUID serviceOfferingVersionId,
            @RequestBody ServiceOrder serviceOrder
    ) throws SSLException, JsonProcessingException, ServiceOptionNotFoundException, ServiceOfferingNotFoundException,
            ServiceOfferingVersionNotFoundException, InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException,
            ConsulLoginFailedException;

    @RequestMapping(value = "/{serviceOfferingId}/versions/{serviceOfferingVersionId}/matching-resources", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get possible resources matching service requirements")
    ResponseEntity<List<MatchingResourceDTO>> getResourcesMatchingServiceRequirements(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId,
            @PathVariable(name = "serviceOfferingVersionId") UUID serviceOfferingVersionId
    ) throws SSLException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException;

    @RequestMapping(value = "/{serviceOfferingId}/versions/{serviceOfferingVersionId}/file", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, method = RequestMethod.POST)
    @Operation(summary = "Upload new service offering file for deployment ")
    ResponseEntity<Void> createOrUpdateServiceOfferingFileWithId(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId,
            @PathVariable(name = "serviceOfferingVersionId") UUID serviceOfferingVersionId,
            @RequestPart("file") MultipartFile file
    ) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, MinioUploadException,
            MinioBucketCreateException, MinioRemoveObjectException, MinioObjectPathNameException, MinioBucketNameException;

    @RequestMapping(value = "/{serviceOfferingId}/versions/{serviceOfferingVersionId}/file/{fileName}", method = RequestMethod.GET)
    @Operation(summary = "Download service offering file ")
    ResponseEntity<InputStreamResource> getServiceOfferingFileWithId(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId,
            @PathVariable(name = "serviceOfferingVersionId") UUID serviceOfferingVersionId,
            @PathVariable(name = "fileName") String fileName
    ) throws Exception;
}
