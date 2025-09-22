package org.eclipse.slm.resource_management.features.device_integration.firmware_update;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.IOUtils;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.slm.common.minio.model.exceptions.*;
import org.eclipse.slm.common.utils.general.Base64Util;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.resource_management.common.exceptions.ResourceTypeNotFoundException;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.UpdateInformationResource;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.UpdateInformationResourceType;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/resources")
@Tag(name = "Updates")
public class FirmwareUpdatesRestController {

    private final static Logger LOG = LoggerFactory.getLogger(FirmwareUpdatesRestController.class);

    private final FirmwareUpdateManager firmwareUpdateManager;

    private final FirmwareUpdateJobService firmwareUpdateJobService;

    public FirmwareUpdatesRestController(FirmwareUpdateManager firmwareUpdateManager, FirmwareUpdateJobService firmwareUpdateJobService) {
        this.firmwareUpdateManager = firmwareUpdateManager;
        this.firmwareUpdateJobService = firmwareUpdateJobService;
    }

    @RequestMapping(value = "/{resourceId}/updates", method = RequestMethod.GET)
    @Operation(summary = "Get available updates for resource")
    public ResponseEntity<UpdateInformationResource> getUpdateInformationOfResource(
            @PathVariable(name = "resourceId") UUID resourceId
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var updateInformation = this.firmwareUpdateManager.getUpdateInformationOfResource(resourceId, jwtAuthenticationToken);

        return ResponseEntity.ok(updateInformation);
    }

    @RequestMapping(value = "/types/{resourceTypeName}/updates", method = RequestMethod.GET)
    @Operation(summary = "Get available updates for resource")
    public ResponseEntity<UpdateInformationResourceType> getUpdateInformationOfResourceType(
            @PathVariable(name = "resourceTypeName") String resourceTypeName
    ) throws ResourceTypeNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var updateInformation = this.firmwareUpdateManager.getUpdateInformationOfResourceType(resourceTypeName, jwtAuthenticationToken);

        return ResponseEntity.ok(updateInformation);
    }

    @RequestMapping(value = "/updates/{softwareNameplateId}/file/{fileName}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Get update file of a software nameplate of a resource")
    public ResponseEntity<byte[]> getUpdateFileOfSoftwareNameplate(
            @PathVariable(name = "softwareNameplateId") String softwareNameplateIdBase64Encoded,
            @PathVariable(name = "fileName") String fileName
    ) throws IOException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var softwareNameplateId = Base64Util.decodeFromBase64(softwareNameplateIdBase64Encoded);
        var fileInputStream = this.firmwareUpdateManager.getUpdateFileOfSoftwareNameplateByFileName(
                softwareNameplateId,
                fileName,
                jwtAuthenticationToken
        );

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(IOUtils.toByteArray(fileInputStream));
    }

    @RequestMapping(value = "/updates/{softwareNameplateId}/file",
            method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary="Add or update firmware update file")
    public void addOrUpdateFirmwareUpdateFile(
            @PathVariable(name = "softwareNameplateId")  String softwareNameplateIdBase64Encoded,
            @RequestPart("file") MultipartFile firmwareUpdateFile
    ) throws MinioUploadException, MinioObjectPathNameException, MinioBucketNameException, MinioBucketCreateException, MinioRemoveObjectException {
        firmwareUpdateManager.addOrUpdateFirmwareUpdateFile(softwareNameplateIdBase64Encoded, firmwareUpdateFile);
    }

    @RequestMapping(value = "/updates/{softwareNameplateId}/file", method = RequestMethod.DELETE)
    @Operation(summary="Delete firmware update file")
    public void deleteFirmwareUpdateFile(
            @PathVariable(name = "softwareNameplateId")  String softwareNameplateIdBase64Encoded
    ) throws MinioObjectPathNameException, MinioBucketNameException, MinioRemoveObjectException {
        firmwareUpdateManager.deleteFirmwareUpdateFile(softwareNameplateIdBase64Encoded);
    }

    @RequestMapping(value = "/updates/{softwareNameplateId}/file/download",
            method = RequestMethod.POST)
    @Operation(summary="Download firmware update file from vendor")
    public void downloadFirmwareUpdateFileFromVendor(
            @PathVariable(name = "softwareNameplateId")  String softwareNameplateIdBase64Encoded
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var softwareNameplateId = Base64UrlEncodedIdentifier.fromEncodedValue(softwareNameplateIdBase64Encoded).getIdentifier();

        firmwareUpdateManager.downloadFirmwareUpdateFileFromVendor(softwareNameplateId, jwtAuthenticationToken);
    }

    @RequestMapping(value = "/{resourceId}/updates/jobs",
            method = RequestMethod.GET)
    @Operation(summary="Get firmware updates jobs of resource")
    public ResponseEntity<List<FirmwareUpdateJob>> getFirmwareUpdateJobsOfResource(
            @PathVariable(name = "resourceId")  UUID resourceId
    ) {
        var firmwareUpdateJobs = this.firmwareUpdateJobService.getFirmwareUpdateJobsOfResource(resourceId);

        return ResponseEntity.ok(firmwareUpdateJobs);
    }

    @RequestMapping(value = "/{resourceId}/updates/jobs",
            method = RequestMethod.POST)
    @Operation(summary="Start firmware update job for a resource")
    public void startFirmwareUpdateOnResource(
            @PathVariable(name = "resourceId")  UUID resourceId,
            @RequestParam(name = "softwareNameplateId")  String softwareNameplateIdBase64Encoded
    ) throws Exception {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken);
        var softwareNameplateId = Base64UrlEncodedIdentifier.fromEncodedValue(softwareNameplateIdBase64Encoded).getIdentifier();

        this.firmwareUpdateJobService.initFirmwareUpdate(resourceId, softwareNameplateId, userId);
    }

    @RequestMapping(value = "/{resourceId}/updates/jobs/{firmwareUpdateJobId}/activate",
            method = RequestMethod.POST)
    @Operation(summary="Activate firmware update on resource")
    public void activateFirmwareUpdateOnResource(
            @PathVariable(name = "resourceId")  UUID resourceId,
            @PathVariable(name = "firmwareUpdateJobId")  UUID firmwareUpdateJobId
            ) throws Exception {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken);

        this.firmwareUpdateJobService.activateFirmwareUpdate(firmwareUpdateJobId);
    }

    @RequestMapping(value = "/{resourceId}/updates/jobs/{firmwareUpdateJobId}/cancel",
            method = RequestMethod.POST)
    @Operation(summary="Activate firmware update on resource")
    public void cancelFirmwareUpdateOnResource(
            @PathVariable(name = "resourceId")  UUID resourceId,
            @PathVariable(name = "firmwareUpdateJobId")  UUID firmwareUpdateJobId
    ) throws Exception {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken);

        this.firmwareUpdateJobService.activateFirmwareUpdate(firmwareUpdateJobId);
    }

}
