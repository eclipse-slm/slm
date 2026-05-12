package org.eclipse.slm.resource_management.features.device_integration.firmware_update;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping(FirmwareUpdatesRestApiConfig.BASE_PATH)
@Tag(name = FirmwareUpdatesRestApiConfig.TAG)
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
    public ResponseEntity<UpdateInformationResource> getUpdateInformationOfResource(UUID resourceId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var updateInformation = this.firmwareUpdateManager.getUpdateInformationOfResource(resourceId, jwtAuthenticationToken);

        return ResponseEntity.ok(updateInformation);
    }

    @Override
    public ResponseEntity<UpdateInformationResourceType> getUpdateInformationOfResourceType(String resourceTypeName
    ) throws ResourceTypeNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var updateInformation = this.firmwareUpdateManager.getUpdateInformationOfResourceType(resourceTypeName, jwtAuthenticationToken);

        return ResponseEntity.ok(updateInformation);
    }

    @Override
    public ResponseEntity<byte[]> getUpdateFileOfSoftwareNameplate(String softwareNameplateIdBase64Encoded, String fileName) throws IOException {
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
    public void addOrUpdateFirmwareUpdateFile(String softwareNameplateIdBase64Encoded, MultipartFile firmwareUpdateFile)
            throws MinioUploadException, MinioObjectPathNameException, MinioBucketNameException, MinioBucketCreateException, MinioRemoveObjectException {
        firmwareUpdateManager.addOrUpdateFirmwareUpdateFile(softwareNameplateIdBase64Encoded, firmwareUpdateFile);
    }

    @Override
    public void deleteFirmwareUpdateFile(String softwareNameplateIdBase64Encoded)
            throws MinioObjectPathNameException, MinioBucketNameException, MinioRemoveObjectException {
        firmwareUpdateManager.deleteFirmwareUpdateFile(softwareNameplateIdBase64Encoded);
    }

    @Override
    public void downloadFirmwareUpdateFileFromVendor(String softwareNameplateIdBase64Encoded) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var softwareNameplateId = Base64UrlEncodedIdentifier.fromEncodedValue(softwareNameplateIdBase64Encoded).getIdentifier();

        firmwareUpdateManager.downloadFirmwareUpdateFileFromVendor(softwareNameplateId, jwtAuthenticationToken);
    }

    @Override
    public ResponseEntity<List<FirmwareUpdateJob>> getFirmwareUpdateJobsOfResource(UUID resourceId) {
        var firmwareUpdateJobs = this.firmwareUpdateJobService.getFirmwareUpdateJobsOfResource(resourceId);

        return ResponseEntity.ok(firmwareUpdateJobs);
    }

    @Override
    public void startFirmwareUpdateOnResource(UUID resourceId, String softwareNameplateIdBase64Encoded
    ) throws Exception {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        var userId = KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken);
        var softwareNameplateId = Base64UrlEncodedIdentifier.fromEncodedValue(softwareNameplateIdBase64Encoded).getIdentifier();

        this.firmwareUpdateJobService.initFirmwareUpdate(resourceId, softwareNameplateId, userId, accessToken);
    }

    @Override
    public void activateFirmwareUpdateOnResource(UUID resourceId, UUID firmwareUpdateJobId
    ) throws Exception {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken);

        this.firmwareUpdateJobService.activateFirmwareUpdate(firmwareUpdateJobId);
    }

    @Override
    public void cancelFirmwareUpdateOnResource(UUID resourceId, UUID firmwareUpdateJobId
    ) throws Exception {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken);

        this.firmwareUpdateJobService.activateFirmwareUpdate(firmwareUpdateJobId);
    }

    @Override
    public void assignFirmwareUpdateCredentialToResource(UUID resourceId, UUID credentialId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        this.firmwareUpdateManager.assignFirmwareUpdateCredentialToResource(resourceId, credentialId, userAccessToken);
    }

    @Override
    public void unassignFirmwareUpdateCredentialFromResource(UUID resourceId, UUID credentialId, boolean deleteIfOrphaned) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        this.firmwareUpdateManager.unassignFirmwareUpdateCredentialFromResource(resourceId, credentialId, userAccessToken, deleteIfOrphaned);
    }

}
