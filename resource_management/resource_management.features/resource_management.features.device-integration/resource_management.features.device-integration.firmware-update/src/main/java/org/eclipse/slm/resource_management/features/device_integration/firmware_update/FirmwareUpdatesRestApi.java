package org.eclipse.slm.resource_management.features.device_integration.firmware_update;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.common.minio.model.exceptions.MinioBucketCreateException;
import org.eclipse.slm.common.minio.model.exceptions.MinioBucketNameException;
import org.eclipse.slm.common.minio.model.exceptions.MinioObjectPathNameException;
import org.eclipse.slm.common.minio.model.exceptions.MinioRemoveObjectException;
import org.eclipse.slm.common.minio.model.exceptions.MinioUploadException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceTypeNotFoundException;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJob;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.UpdateInformationResource;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.UpdateInformationResourceType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface FirmwareUpdatesRestApi {

    @RequestMapping(value = "/{resourceId}/updates", method = RequestMethod.GET)
    @Operation(summary = "Get available updates for resource")
    ResponseEntity<UpdateInformationResource> getUpdateInformationOfResource(
            @PathVariable(name = "resourceId") UUID resourceId
    );

    @RequestMapping(value = "/types/{resourceTypeName}/updates", method = RequestMethod.GET)
    @Operation(summary = "Get available updates for resource")
    ResponseEntity<UpdateInformationResourceType> getUpdateInformationOfResourceType(
            @PathVariable(name = "resourceTypeName") String resourceTypeName
    ) throws ResourceTypeNotFoundException;

    @RequestMapping(value = "/updates/{softwareNameplateId}/file/{fileName}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Get update file of a software nameplate of a resource")
    ResponseEntity<byte[]> getUpdateFileOfSoftwareNameplate(
            @PathVariable(name = "softwareNameplateId") String softwareNameplateIdBase64Encoded,
            @PathVariable(name = "fileName") String fileName
    ) throws IOException;

    @RequestMapping(value = "/updates/{softwareNameplateId}/file",
            method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add or update firmware update file")
    void addOrUpdateFirmwareUpdateFile(
            @PathVariable(name = "softwareNameplateId") String softwareNameplateIdBase64Encoded,
            @RequestPart("file") MultipartFile firmwareUpdateFile
    ) throws MinioUploadException, MinioObjectPathNameException, MinioBucketNameException,
            MinioBucketCreateException, MinioRemoveObjectException;

    @RequestMapping(value = "/updates/{softwareNameplateId}/file", method = RequestMethod.DELETE)
    @Operation(summary = "Delete firmware update file")
    void deleteFirmwareUpdateFile(
            @PathVariable(name = "softwareNameplateId") String softwareNameplateIdBase64Encoded
    ) throws MinioObjectPathNameException, MinioBucketNameException, MinioRemoveObjectException;

    @RequestMapping(value = "/updates/{softwareNameplateId}/file/download",
            method = RequestMethod.POST)
    @Operation(summary = "Download firmware update file from vendor")
    void downloadFirmwareUpdateFileFromVendor(
            @PathVariable(name = "softwareNameplateId") String softwareNameplateIdBase64Encoded
    );

    @RequestMapping(value = "/{resourceId}/updates/jobs", method = RequestMethod.GET)
    @Operation(summary = "Get firmware updates jobs of resource")
    ResponseEntity<List<FirmwareUpdateJob>> getFirmwareUpdateJobsOfResource(
            @PathVariable(name = "resourceId") UUID resourceId
    );

    @RequestMapping(value = "/{resourceId}/updates/jobs", method = RequestMethod.POST)
    @Operation(summary = "Start firmware update job for a resource")
    void startFirmwareUpdateOnResource(
            @PathVariable(name = "resourceId") UUID resourceId,
            @RequestParam(name = "softwareNameplateId") String softwareNameplateIdBase64Encoded
    ) throws Exception;

    @RequestMapping(value = "/{resourceId}/updates/jobs/{firmwareUpdateJobId}/activate",
            method = RequestMethod.POST)
    @Operation(summary = "Activate firmware update on resource")
    void activateFirmwareUpdateOnResource(
            @PathVariable(name = "resourceId") UUID resourceId,
            @PathVariable(name = "firmwareUpdateJobId") UUID firmwareUpdateJobId
    ) throws Exception;

    @RequestMapping(value = "/{resourceId}/updates/jobs/{firmwareUpdateJobId}/cancel",
            method = RequestMethod.POST)
    @Operation(summary = "Activate firmware update on resource")
    void cancelFirmwareUpdateOnResource(
            @PathVariable(name = "resourceId") UUID resourceId,
            @PathVariable(name = "firmwareUpdateJobId") UUID firmwareUpdateJobId
    ) throws Exception;

    @RequestMapping(value = "/{resourceId}/updates/credentials/{credentialId}", method = RequestMethod.POST)
    @Operation(summary = "Assign existing credential to resource for firmware updates")
    void assignFirmwareUpdateCredentialToResource(
            @PathVariable(name = "resourceId") UUID resourceId,
            @PathVariable(name = "credentialId") UUID credentialId
    );

    @RequestMapping(value = "/{resourceId}/updates/credentials/{credentialId}", method = RequestMethod.DELETE)
    @Operation(summary = "Unassign existing credential from resource firmware updates")
    void unassignFirmwareUpdateCredentialFromResource(
            @PathVariable(name = "resourceId") UUID resourceId,
            @PathVariable(name = "credentialId") UUID credentialId,
            @RequestParam(name = "deleteIfOrphaned", required = false, defaultValue = "false") boolean deleteIfOrphaned
    );
}
