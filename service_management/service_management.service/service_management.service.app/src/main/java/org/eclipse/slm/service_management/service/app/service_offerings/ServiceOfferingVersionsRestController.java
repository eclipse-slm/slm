package org.eclipse.slm.service_management.service.app.service_offerings;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.minio.model.exceptions.*;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmUser;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmUserOrApiKey;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.resource_management.common.model.MatchingResourceDTO;
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.service.app.service_categories.ServiceCategoryNotFoundException;
import org.eclipse.slm.service_management.service.app.service_deployment.CapabilityServiceNotFoundException;
import org.eclipse.slm.service_management.model.offerings.ServiceOrder;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersionDTOApi;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionCreateException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.ServiceRequirement;
import org.eclipse.slm.service_management.model.offerings.responses.ServiceOfferingVersionCreateResponse;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ServiceOfferingVersionsRestApiConfig.BASE_PATH)
@Tag(name = ServiceOfferingVersionsRestApiConfig.TAG)
@AuthorizedAsSlmUserOrApiKey
public class ServiceOfferingVersionsRestController implements ServiceOfferingVersionsRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingVersionsRestController.class);

    private final ServiceOfferingVersionHandler serviceOfferingVersionHandler;

    private final ServiceOfferingOrderHandler serviceOfferingOrderHandler;


    public ServiceOfferingVersionsRestController(
            ServiceOfferingVersionHandler serviceOfferingVersionHandler,
            ServiceOfferingOrderHandler serviceOfferingOrderHandler) {
        this.serviceOfferingVersionHandler = serviceOfferingVersionHandler;
        this.serviceOfferingOrderHandler = serviceOfferingOrderHandler;
    }

    @Override
    public ResponseEntity<List<ServiceOfferingVersionDTOApi>> getServiceOfferingVersionsOfServiceOffering(
            UUID serviceOfferingId
    ) throws ServiceOfferingNotFoundException {
        var serviceOfferingVersions = this.serviceOfferingVersionHandler
                .getServiceOfferingVersionsOfServiceOffering(serviceOfferingId);

        var serviceOfferingVersionsDTOApi = ObjectMapperUtils
                .mapAll(serviceOfferingVersions, ServiceOfferingVersionDTOApi.class);

        return ResponseEntity.ok(serviceOfferingVersionsDTOApi);
    }

    @Override
    public ResponseEntity<ServiceOfferingVersionDTOApi> getServiceOfferingVersionById(
            UUID serviceOfferingId,
            UUID serviceOfferingVersionId) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .getServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersionId);

        var serviceOfferingVersionDTOApi = ObjectMapperUtils
                .map(serviceOfferingVersion, ServiceOfferingVersionDTOApi.class);

        return ResponseEntity.ok(serviceOfferingVersionDTOApi);
    }

    @Override
    public ResponseEntity<List<ServiceRequirement>> getServiceOfferingVersionRequirementsById(
            UUID serviceOfferingId,
            UUID serviceOfferingVersionId) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .getServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersionId);

        return ResponseEntity.ok(serviceOfferingVersion.getServiceRequirements());
    }

    @Override
    public @ResponseBody ResponseEntity<Void> createOrUpdateServiceOfferingVersionRequirementsWithId(
            UUID serviceOfferingId,
            UUID serviceOfferingVersionId,
            List<ServiceRequirement> requirements) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .getServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersionId);
        serviceOfferingVersion.setServiceRequirements(requirements);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ServiceOfferingVersionCreateResponse> createServiceOfferingVersionWithAutoGeneratedId(
            UUID serviceOfferingId,
            ServiceOfferingVersionDTOApi serviceOfferingVersionDTOApi)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionCreateException {

        serviceOfferingVersionDTOApi.setServiceOfferingId(serviceOfferingId);
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .createServiceOfferingVersionWithAutoGeneratedId(serviceOfferingVersionDTOApi);

        return ResponseEntity.ok(new ServiceOfferingVersionCreateResponse(serviceOfferingVersion));
    }

    @Override
    public ResponseEntity<ServiceOfferingVersionCreateResponse> createOrUpdateServiceOfferingVersionWithId(
            UUID serviceOfferingId,
            UUID serviceOfferingVersionId,
            ServiceOfferingVersionDTOApi serviceOfferingVersionDTOApi)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionCreateException {

        serviceOfferingVersionDTOApi.setServiceOfferingId(serviceOfferingId);
        serviceOfferingVersionDTOApi.setId(serviceOfferingVersionId);
        var serviceOfferingVersion = this.serviceOfferingVersionHandler
                .createOrUpdateServiceOfferingVersionWithId(serviceOfferingVersionDTOApi);

        return ResponseEntity.ok(new ServiceOfferingVersionCreateResponse(serviceOfferingVersion));
    }

    @Override
    public @ResponseBody ResponseEntity<Void> deleteServiceOfferingVersion(UUID serviceOfferingId, UUID serviceOfferingVersionId)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        this.serviceOfferingVersionHandler.deleteServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersionId);
        return ResponseEntity.ok().build();
    }

    @Override
    @AuthorizedAsSlmUser
    public ResponseEntity<Void> orderServiceOfferingVersionById(UUID serviceOfferingId, UUID serviceOfferingVersionId, ServiceOrder serviceOrder)
            throws SSLException, JsonProcessingException, ServiceOptionNotFoundException, ServiceOfferingNotFoundException,
            ServiceOfferingVersionNotFoundException, InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException, ConsulLoginFailedException {

        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        this.serviceOfferingOrderHandler
                .orderServiceOfferingById(serviceOfferingId, serviceOfferingVersionId,
                        serviceOrder, jwtAuthenticationToken);

        return ResponseEntity.ok().build();
    }

    @Override
    @AuthorizedAsSlmUser
    public @ResponseBody ResponseEntity<List<MatchingResourceDTO>> getResourcesMatchingServiceRequirements(UUID serviceOfferingId, UUID serviceOfferingVersionId)
            throws SSLException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var matchingResources = this.serviceOfferingOrderHandler
                .getCapabilityServicesMatchingServiceRequirements(serviceOfferingId, serviceOfferingVersionId, jwtAuthenticationToken);
        return ResponseEntity.ok().body(matchingResources);
    }

    @Override
    public ResponseEntity<Void> createOrUpdateServiceOfferingFileWithId(UUID serviceOfferingId, UUID serviceOfferingVersionId, MultipartFile file)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, MinioUploadException, MinioBucketCreateException,
            MinioRemoveObjectException, MinioObjectPathNameException, MinioBucketNameException {

        this.serviceOfferingVersionHandler
                .createOrUpdateServiceOfferingFile(serviceOfferingId, serviceOfferingVersionId, file);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<InputStreamResource> getServiceOfferingFileWithId(UUID serviceOfferingId, UUID serviceOfferingVersionId, String fileName)
            throws Exception {

        var result = this.serviceOfferingVersionHandler
                .getServiceOfferingFile(serviceOfferingId, serviceOfferingVersionId, fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.getFileName())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE).body(new InputStreamResource(result.getFileStream()));
    }

}
