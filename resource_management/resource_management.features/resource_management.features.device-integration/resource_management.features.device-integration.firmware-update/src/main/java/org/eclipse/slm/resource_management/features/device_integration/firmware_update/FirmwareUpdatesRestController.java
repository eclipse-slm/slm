package org.eclipse.slm.resource_management.features.device_integration.firmware_update;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
public class FirmwareUpdatesRestController implements FirmwareUpdatesRestApi {

    private final static Logger LOG = LoggerFactory.getLogger(FirmwareUpdatesRestController.class);

    private final FirmwareUpdateManager firmwareUpdateManager;

    private final FirmwareUpdateJobService firmwareUpdateJobService;

    public FirmwareUpdatesRestController(FirmwareUpdateManager firmwareUpdateManager,
                                         FirmwareUpdateJobService firmwareUpdateJobService) {
        this.firmwareUpdateManager = firmwareUpdateManager;
        this.firmwareUpdateJobService = firmwareUpdateJobService;
    }

    @Override
    public ResponseEntity<UpdateInformationResource> getUpdateInformationOfResource(
            @PathVariable(name = "resourceId") UUID resourceId
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var updateInformation = this.firmwareUpdateManager.getUpdateInformationOfResource(resourceId, jwtAuthenticationToken);

        return ResponseEntity.ok(updateInformation);
    }

    @Override
    public ResponseEntity<UpdateInformationResourceType> getUpdateInformationOfResourceType(
            @PathVariable(name = "resourceTypeName") String resourceTypeName
    ) throws ResourceTypeNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var updateInformation = this.firmwareUpdateManager.getUpdateInformationOfResourceType(resourceTypeName, jwtAuthenticationToken);

        return ResponseEntity.ok(updateInformation);
    }

    @Override
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

    @Override
    public void addOrUpdateFirmwareUpdateFile(
            @PathVariable(name = "softwareNameplateId")  String softwareNameplateIdBase64Encoded,
            @RequestPart("file") MultipartFile firmwareUpdateFile
    ) throws MinioUploadException, MinioObjectPathNameException, MinioBucketNameException, MinioBucketCreateException, MinioRemoveObjectException {
        firmwareUpdateManager.addOrUpdateFirmwareUpdateFile(softwareNameplateIdBase64Encoded, firmwareUpdateFile);
    }

    @Override
    public void deleteFirmwareUpdateFile(
            @PathVariable(name = "softwareNameplateId")  String softwareNameplateIdBase64Encoded
    ) throws MinioObjectPathNameException, MinioBucketNameException, MinioRemoveObjectException {
        firmwareUpdateManager.deleteFirmwareUpdateFile(softwareNameplateIdBase64Encoded);
    }

    @Override
    public void downloadFirmwareUpdateFileFromVendor(
            @PathVariable(name = "softwareNameplateId")  String softwareNameplateIdBase64Encoded
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var softwareNameplateId = Base64UrlEncodedIdentifier.fromEncodedValue(softwareNameplateIdBase64Encoded).getIdentifier();

        firmwareUpdateManager.downloadFirmwareUpdateFileFromVendor(softwareNameplateId, jwtAuthenticationToken);
    }

    @Override
    public ResponseEntity<List<FirmwareUpdateJob>> getFirmwareUpdateJobsOfResource(
            @PathVariable(name = "resourceId")  UUID resourceId
    ) {
        var firmwareUpdateJobs = this.firmwareUpdateJobService.getFirmwareUpdateJobsOfResource(resourceId);

        return ResponseEntity.ok(firmwareUpdateJobs);
    }

    @Override
    public void startFirmwareUpdateOnResource(
            @PathVariable(name = "resourceId")  UUID resourceId,
            @RequestParam(name = "softwareNameplateId")  String softwareNameplateIdBase64Encoded
    ) throws Exception {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        var userId = KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken);
        var softwareNameplateId = Base64UrlEncodedIdentifier.fromEncodedValue(softwareNameplateIdBase64Encoded).getIdentifier();

        this.firmwareUpdateJobService.initFirmwareUpdate(resourceId, softwareNameplateId, userId, accessToken);
    }

    @Override
    public void activateFirmwareUpdateOnResource(
            @PathVariable(name = "resourceId")  UUID resourceId,
            @PathVariable(name = "firmwareUpdateJobId")  UUID firmwareUpdateJobId
            ) throws Exception {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken);

        this.firmwareUpdateJobService.activateFirmwareUpdate(firmwareUpdateJobId);
    }

    @Override
    public void cancelFirmwareUpdateOnResource(
            @PathVariable(name = "resourceId")  UUID resourceId,
            @PathVariable(name = "firmwareUpdateJobId")  UUID firmwareUpdateJobId
    ) throws Exception {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken);

        this.firmwareUpdateJobService.activateFirmwareUpdate(firmwareUpdateJobId);
    }

    @Override
    public void assignFirmwareUpdateCredentialToResource(
            @PathVariable(name = "resourceId") UUID resourceId,
            @PathVariable(name = "credentialId") UUID credentialId
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        this.firmwareUpdateManager.assignFirmwareUpdateCredentialToResource(resourceId, credentialId, userAccessToken);
    }

    @Override
    public void unassignFirmwareUpdateCredentialFromResource(
            @PathVariable(name = "resourceId") UUID resourceId,
            @PathVariable(name = "credentialId") UUID credentialId,
            @RequestParam(name = "deleteIfOrphaned", required = false, defaultValue = "false") boolean deleteIfOrphaned
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        this.firmwareUpdateManager.unassignFirmwareUpdateCredentialFromResource(resourceId, credentialId, userAccessToken, deleteIfOrphaned);
    }

}
