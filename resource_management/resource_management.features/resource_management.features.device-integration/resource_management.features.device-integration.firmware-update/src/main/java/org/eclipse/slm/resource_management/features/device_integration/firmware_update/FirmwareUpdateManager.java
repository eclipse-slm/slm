package org.eclipse.slm.resource_management.features.device_integration.firmware_update;

import org.apache.logging.log4j.util.Base64Util;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.common.aas.clients.exceptions.ShellNotFoundException;
import org.eclipse.slm.common.aas.repositories.exceptions.SubmodelNotFoundException;
import org.eclipse.slm.common.keycloak.client.KeycloakServiceClient;
import org.eclipse.slm.common.minio.client.MinioClient;
import org.eclipse.slm.common.minio.model.exceptions.*;
import org.eclipse.slm.common.utils.files.FileDownloader;
import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.eclipse.slm.resource_management.common.exceptions.ResourceTypeNotFoundException;
import org.eclipse.slm.resource_management.common.resource_types.ResourceTypesManager;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.exceptions.FirmwareUpdateFileNotFoundException;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.*;
import org.eclipse.slm.resource_management.common.resources.ResourcesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class FirmwareUpdateManager {

    private final static Logger LOG = LoggerFactory.getLogger(FirmwareUpdateManager.class);

    private final static String FIRMWARE_UPDATE_BUCKET_NAME = "firmware-updates";

    private final String resourceManagementDeploymentUrl;

    private final ResourcesManager resourcesManager;

    private final ResourceTypesManager resourceTypesManager;

    private final FirmwareUpdateJobJpaRepository firmwareUpdateJobRepository;

    private final AasRepositoryClient aasRepositoryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    private final MinioClient minioClient;

    private final KeycloakServiceClient keycloakServiceClient;

    public FirmwareUpdateManager(@Value("${deployment.url}")String resourceManagementDeploymentUrl,
                                 ResourcesManager resourcesManager, ResourceTypesManager resourceTypesManager, FirmwareUpdateJobJpaRepository firmwareUpdateJobRepository,
                                 AasRepositoryClientFactory aasRepositoryClientFactory,
                                 SubmodelRegistryClientFactory submodelRegistryClientFactroy,
                                 MinioClient minioClient,
                                 KeycloakServiceClient keycloakServiceClient) {
        this.resourceManagementDeploymentUrl = resourceManagementDeploymentUrl;
        this.resourcesManager = resourcesManager;
        this.resourceTypesManager = resourceTypesManager;
        this.firmwareUpdateJobRepository = firmwareUpdateJobRepository;
        this.aasRepositoryClient = aasRepositoryClientFactory.getClient();
        this.submodelRegistryClient = submodelRegistryClientFactroy.getClient();
        this.minioClient = minioClient;
        this.keycloakServiceClient = keycloakServiceClient;
    }

    @PostConstruct
    public void init() {
        try {
            if (!minioClient.bucketExist(FIRMWARE_UPDATE_BUCKET_NAME)) {
                this.minioClient.createBucket(FIRMWARE_UPDATE_BUCKET_NAME);
            }
        } catch (MinioBucketCreateException | MinioBucketNameException e) {
            LOG.error("Error creating bucket '" + FIRMWARE_UPDATE_BUCKET_NAME + "' at startup: ", e);
        }
    }

    public UpdateInformationResource getUpdateInformationOfResource(UUID resourceId) {
        JwtAuthenticationToken jwtAuthenticationToken = null;
        try {
            jwtAuthenticationToken = this.keycloakServiceClient.getJwtAuthentication();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }

        return this.getUpdateInformationOfResource(resourceId, jwtAuthenticationToken);
    }

    public UpdateInformationResource getUpdateInformationOfResource(UUID resourceId, JwtAuthenticationToken jwtAuthenticationToken) {
        var softwareNameplateSubmodels = this.getSoftwareNameplateSubmodelsOfResource(resourceId, jwtAuthenticationToken);
        var availableFirmwareVersions = this.getAvailableFirmwareVersionsFromSoftwareNameplates(softwareNameplateSubmodels);

        var updateInformation = new UpdateInformationResource();
        updateInformation.setFirmwareUpdateStatus(FirmwareUpdateStatus.UNKNOWN);
        updateInformation.setAvailableFirmwareVersions(availableFirmwareVersions);

        FirmwareVersionDetails currentFirmwareVersion = null;
        var resource = this.resourcesManager.getResourceByIdOrThrow(jwtAuthenticationToken, resourceId);

        for (int i = 0; i < availableFirmwareVersions.size(); i++) {
            var firmwareVersion = availableFirmwareVersions.get(i);
            if (firmwareVersion.getVersion().equals(resource.getFirmwareVersion())) {
                currentFirmwareVersion = firmwareVersion;
                // If current firmware version is on top of sorted list of availableFirmwareVersions, it is up to date
                if (i == 0) {
                    updateInformation.setFirmwareUpdateStatus(FirmwareUpdateStatus.UP_TO_DATE);
                }
                else {
                    updateInformation.setFirmwareUpdateStatus(FirmwareUpdateStatus.UPDATE_AVAILABLE);
                }
            }
        }

        if (currentFirmwareVersion == null) {
            currentFirmwareVersion = new FirmwareVersionDetails(resource.getFirmwareVersion(), "", "", "", "");
        }
        updateInformation.setCurrentFirmwareVersion(currentFirmwareVersion);

        if (!availableFirmwareVersions.isEmpty()) {
            updateInformation.setLatestFirmwareVersion(availableFirmwareVersions.get(0));
        }

        var firmwareUpdateJobs = firmwareUpdateJobRepository.findByResourceIdOrderByCreatedAtDesc(resourceId);
        updateInformation.setFirmwareUpdateJobs(firmwareUpdateJobs);

        return updateInformation;
    }

    public UpdateInformationResourceType getUpdateInformationOfResourceType(String resourceTypeName, JwtAuthenticationToken jwtAuthenticationToken)
            throws ResourceTypeNotFoundException {
        var resourceTypes = this.resourceTypesManager.getResourceTypes();
        var resourceType = resourceTypes.stream()
                .filter(rt -> rt.getTypeName().equals(resourceTypeName))
                .findFirst()
                .orElseThrow(() -> new ResourceTypeNotFoundException(resourceTypeName));

        var softwareNameplateSubmodels = new ArrayList<Submodel>();
        for (var softwareNameplateId : resourceType.getSoftwareNameplateIds()) {
            var softwareNameplateSubmodelDescriptor = this.submodelRegistryClient.findSubmodelDescriptor(softwareNameplateId);
            if (softwareNameplateSubmodelDescriptor.isPresent()) {
                var scopedSubmodelRepositoryClient = SubmodelRepositoryClientFactory.FromSubmodelDescriptor(softwareNameplateSubmodelDescriptor.get());
                var softwareNameplateSubmodel = scopedSubmodelRepositoryClient.getSubmodel(softwareNameplateSubmodelDescriptor.get().getId());

                softwareNameplateSubmodels.add(softwareNameplateSubmodel);
            }

        }

        var availableFirmwareVersions = this.getAvailableFirmwareVersionsFromSoftwareNameplates(softwareNameplateSubmodels);
        var updateInformation = new UpdateInformationResourceType();
        updateInformation.setAvailableFirmwareVersions(availableFirmwareVersions);

        return updateInformation;
    }

    public void addOrUpdateFirmwareUpdateFile(String softwareNameplateIdBase64Encoded, MultipartFile firmwareUpdateFile)
            throws MinioUploadException, MinioObjectPathNameException, MinioBucketNameException, MinioRemoveObjectException, MinioBucketCreateException {

        if(firmwareUpdateFile.isEmpty()){
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        var path = "/" + softwareNameplateIdBase64Encoded + "/";
        var fileName = firmwareUpdateFile.getOriginalFilename();
        fileName = fileName.replaceAll("[\\x00-\\x1F\\\\?*<>|\"\\r\\n\\t\\[\\]\\(\\)\\s]", "_");
        var objectName = path + fileName;

        if (minioClient.objectExist(FirmwareUpdateManager.FIRMWARE_UPDATE_BUCKET_NAME, objectName)){
            minioClient.removeObject(FirmwareUpdateManager.FIRMWARE_UPDATE_BUCKET_NAME, objectName);
            LOG.debug("Removed existing firmware update file '" + firmwareUpdateFile.getName() + "'for software nameplate id (Base64): " + softwareNameplateIdBase64Encoded);
        }

        try {
            minioClient.putObject(FirmwareUpdateManager.FIRMWARE_UPDATE_BUCKET_NAME, objectName, firmwareUpdateFile.getInputStream(), firmwareUpdateFile.getSize(), firmwareUpdateFile.getContentType());
            LOG.debug("Added firmware update file '" + firmwareUpdateFile.getName() + "' for software nameplate id (Base64): " + softwareNameplateIdBase64Encoded);
        } catch (IOException e) {
            throw new MinioUploadException(FirmwareUpdateManager.FIRMWARE_UPDATE_BUCKET_NAME, objectName);
        }
    }

    public void downloadFirmwareUpdateFileFromVendor(String softwareNameplateId, JwtAuthenticationToken jwtAuthenticationToken) {
        var softwareNameplateOptional = this.submodelRegistryClient.findSubmodelDescriptor(softwareNameplateId);
        if (softwareNameplateOptional.isEmpty()) {
            LOG.error("Software nameplate with ID {} not found", softwareNameplateId);
            throw new SubmodelNotFoundException(softwareNameplateId);
        }

        var submodelRepositoryClient = SubmodelRepositoryClientFactory.FromSubmodelDescriptor(softwareNameplateOptional.get(), jwtAuthenticationToken);

        var softwareNameplateSubmodel = submodelRepositoryClient.getSubmodel(softwareNameplateId);
        softwareNameplateSubmodel.getSubmodelElements().stream()
                .filter(se -> se.getIdShort().equals("SoftwareNameplateType"))
                .findAny()
                .ifPresent(softwareNameplateTypeSmc -> {

                    ((SubmodelElementCollection) softwareNameplateTypeSmc).getValue()
                            .stream().filter(se -> se.getIdShort().equals("InstallationURI"))
                            .findAny()
                            .ifPresent(prop -> {
                                var installationUri = ((Property) prop).getValue();
                                if (installationUri != null && !installationUri.isEmpty()) {
                                    try {
                                        var firmwareUpdateFile = FileDownloader.downloadFile(installationUri);
                                        var softwareNameplateIdBase64Encoded = Base64.getEncoder().encodeToString(softwareNameplateId.getBytes());
                                        this.addOrUpdateFirmwareUpdateFile(softwareNameplateIdBase64Encoded, firmwareUpdateFile);
                                    } catch (Exception e) {
                                        LOG.error("Error downloading firmware update file: {}", e.getMessage());
                                    }
                                }
                            });
                });
    }

    private List<Submodel> getSoftwareNameplateSubmodelsOfResource(UUID resourceId, JwtAuthenticationToken jwtAuthenticationToken) {
        var resourceAasId = ResourceAas.createAasIdFromResourceId(resourceId);
        var resourceAasOptional = this.aasRepositoryClient.getAas(resourceAasId);
        if (resourceAasOptional.isEmpty()) {
            LOG.error("Resource AAS with ID {} not found", resourceAasId);
            throw new ShellNotFoundException(resourceAasId);
        }
        var resourceAas = resourceAasOptional.get();

        var softwareNameplateSubmodels = new ArrayList<Submodel>();
        for (var submodelRef : resourceAas.getSubmodels()) {
            var submodelId = submodelRef.getKeys().get(0).getValue();

            this.submodelRegistryClient.findSubmodelDescriptor(submodelId).ifPresent(submodelDescriptor -> {
                if (submodelDescriptor.getSemanticId() != null) {
                    if (!submodelDescriptor.getSemanticId().getKeys().isEmpty()) {
                        var semanticId = submodelDescriptor.getSemanticId().getKeys().get(0).getValue();

                        if (semanticId.equals("https://admin-shell.io/idta/SoftwareNameplate/1/0")) {
                            try {
                                var submodelRepositoryClient = SubmodelRepositoryClientFactory.FromSubmodelDescriptor(submodelDescriptor, jwtAuthenticationToken);
                                var submodel = submodelRepositoryClient.getSubmodel(submodelDescriptor.getId());
                                if (submodel != null) {
                                    softwareNameplateSubmodels.add(submodel);
                                }
                            } catch (Exception e) {
                                LOG.error("Error retrieving submodel {}: {}", submodelId, e.getMessage());
                            }
                        }
                    }
                }
            });
        }

        return softwareNameplateSubmodels;
    }

    private List<FirmwareVersionDetails> getAvailableFirmwareVersionsFromSoftwareNameplates(List<Submodel> softwareNameplateSubmodels) {
        var availableFirmwareVersions = new ArrayList<FirmwareVersionDetails>();
        for (var softwareNameplateSubmodel : softwareNameplateSubmodels) {
            softwareNameplateSubmodel.getSubmodelElements()
                    .stream().filter(se -> se.getIdShort().equals("SoftwareNameplateType"))
                    .findAny().ifPresent(
                            softwareNameplateTypeSmc -> {

                                var firmwareVersionDetails = new FirmwareVersionDetails.Builder();
                                firmwareVersionDetails.softwareNameplateSubmodelId(softwareNameplateSubmodel.getId());

                                ((SubmodelElementCollection) softwareNameplateTypeSmc).getValue()
                                        .stream().filter(se -> se.getIdShort().equals("Version"))
                                        .findAny()
                                        .ifPresent(
                                                prop -> {
                                                    var version = ((Property) prop).getValue();
                                                    firmwareVersionDetails.version(version);
                                                }
                                        );

                                ((SubmodelElementCollection) softwareNameplateTypeSmc).getValue()
                                        .stream().filter(se -> se.getIdShort().equals("ReleaseDate"))
                                        .findAny()
                                        .ifPresent(
                                                prop -> {
                                                    var dateString = ((Property) prop).getValue();
                                                    firmwareVersionDetails.date(dateString);
                                                }
                                        );

                                ((SubmodelElementCollection) softwareNameplateTypeSmc).getValue()
                                        .stream().filter(se -> se.getIdShort().equals("InstallationURI"))
                                        .findAny()
                                        .ifPresent(
                                                prop -> {
                                                    var installationUri = ((Property) prop).getValue();
                                                    firmwareVersionDetails.installationUri(installationUri);
                                                }
                                        );

                                ((SubmodelElementCollection) softwareNameplateTypeSmc).getValue()
                                        .stream().filter(se -> se.getIdShort().equals("InstallationChecksum"))
                                        .findAny()
                                        .ifPresent(
                                                prop -> {
                                                    var installationChecksum = ((Property) prop).getValue();
                                                    firmwareVersionDetails.installationChecksum(installationChecksum);
                                                }
                                        );

                                var firmwareUpdateFile = this.getFirmwareUpdateFile(softwareNameplateSubmodel.getId());
                                if (firmwareUpdateFile.isPresent()) {
                                    firmwareVersionDetails.firmwareUpdateFile(firmwareUpdateFile.get());
                                }

                                availableFirmwareVersions.add(firmwareVersionDetails.build());
                            }
                    );
        }

        // Sort available firmware versions from new to old
        availableFirmwareVersions.sort((o1, o2) -> {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            if (o1.getDate() == null || o1.getDate().isEmpty() || o2.getDate() == null || o2.getDate().isEmpty()) {
                return 0;
            }
            try {
                var date1 = formatter.parse(o1.getDate());
                var date2 = formatter.parse(o2.getDate());

                return date2.compareTo(date1);
            } catch (Exception e) {
                LOG.error("Error parsing date: {}", e.getMessage());
                return 0;
            }
        });

        return availableFirmwareVersions;
    }

    private Optional<FirmwareUpdateFile> getFirmwareUpdateFile(String softwareNameplateId) {
        var softwareNameplateIdBase64Encoded = Base64.getEncoder().encodeToString(softwareNameplateId.getBytes());

        var path = softwareNameplateIdBase64Encoded + "/";
        var objectItems = this.minioClient.getObjectsOfPath(FirmwareUpdateManager.FIRMWARE_UPDATE_BUCKET_NAME, path);

        if (objectItems.isEmpty()) {
            return Optional.empty();
        }

        var firmwareUpdateObjectItem = objectItems.get(0);
        if (objectItems.size() > 1) {
            LOG.warn("Multiple firmware update files found for software nameplate '" + softwareNameplateId + "', using first one");
        }

        var objectName = firmwareUpdateObjectItem.objectName();
        if (objectName.startsWith(path)) {
            var fileName = objectName.substring(path.length());
            var firmwareUpdateFile = new FirmwareUpdateFile(
                    fileName,
                    firmwareUpdateObjectItem.size(),
                    firmwareUpdateObjectItem.lastModified());
            var downloadUrl = this.resourceManagementDeploymentUrl + "/resources/updates/" + softwareNameplateIdBase64Encoded + "/file/" + fileName;
            firmwareUpdateFile.setDownloadUrl(downloadUrl);
            
            LOG.debug("Found firmware update file: " + firmwareUpdateFile.getFileName() + " for software nameplate id (Base64): " + softwareNameplateIdBase64Encoded);
            return Optional.of(firmwareUpdateFile);
        }

        return Optional.empty();
    }

    public InputStream getUpdateFileOfSoftwareNameplateByFileName(String softwareNameplateId, String fileName, JwtAuthenticationToken jwtAuthenticationToken) {
        var softwareNameplateIdBase64Encoded = Base64Util.encode(softwareNameplateId);

        var objectName = softwareNameplateIdBase64Encoded + "/" + fileName;
        try {
            var fileInputStream = this.minioClient.getObject(FirmwareUpdateManager.FIRMWARE_UPDATE_BUCKET_NAME, objectName);
            return fileInputStream;
        } catch (MinioObjectPathNameException | MinioBucketNameException e) {
            LOG.error("Error retrieving firmware update file: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getFirstUpdateFileOfSoftwareNameplate(String softwareNameplateId, JwtAuthenticationToken jwtAuthenticationToken) {

        var fileInputStream = this.getFirmwareUpdateFile(softwareNameplateId)
                .map(firmwareUpdateFile -> {
                    var fileName = firmwareUpdateFile.getFileName();
                    return this.getUpdateFileOfSoftwareNameplateByFileName(softwareNameplateId, fileName, jwtAuthenticationToken);
                })
                .orElseThrow(() -> new FirmwareUpdateFileNotFoundException(softwareNameplateId));

        return fileInputStream;
    }

    public void deleteFirmwareUpdateFile(String softwareNameplateIdBase64Encoded)
            throws MinioObjectPathNameException, MinioBucketNameException, MinioRemoveObjectException {
        var softwareNameplateId = new String(Base64.getDecoder().decode(softwareNameplateIdBase64Encoded));

        var path = softwareNameplateIdBase64Encoded + "/";
        var objectItems = this.minioClient.getObjectsOfPath(FirmwareUpdateManager.FIRMWARE_UPDATE_BUCKET_NAME, path);

        if (objectItems.isEmpty()) {
            LOG.debug("No firmware updates files found for software nameplate '" + softwareNameplateId + "', nothing to delete");
            return ;
        }

        if (objectItems.size() > 1) {
            LOG.warn("Multiple firmware update files found for software nameplate '" + softwareNameplateId + "', deleting all of them");
        }

        for (var objectItem : objectItems) {
            var objectName = objectItem.objectName();
            if (objectName.startsWith(path)) {
                this.minioClient.removeObject(FirmwareUpdateManager.FIRMWARE_UPDATE_BUCKET_NAME, objectName);
                LOG.debug("Removed firmware update file '" + objectItem.objectName() + "' for software nameplate id (Base64): " + softwareNameplateIdBase64Encoded);
            }
        }
    }
}
