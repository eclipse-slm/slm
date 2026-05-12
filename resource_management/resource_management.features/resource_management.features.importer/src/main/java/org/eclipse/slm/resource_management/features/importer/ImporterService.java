package org.eclipse.slm.resource_management.features.importer;

import org.eclipse.slm.common.credentials.model.Credential;
import org.eclipse.slm.common.credentials.model.CredentialDataUsernamePassword;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.platform_management.features.credentials_management.api.CredentialCreateRequest;
import org.eclipse.slm.platform_management.service.client.PlatformManagementClientFactory;
import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.eclipse.slm.resource_management.common.aas.ResourcesSubmodelManager;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.common.location.Location;
import org.eclipse.slm.resource_management.common.location.LocationHandler;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessCreateDTO;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessManager;
import org.eclipse.slm.resource_management.common.resources.ResourcesManager;
import org.eclipse.slm.resource_management.features.capabilities.jobs.CapabilityAlreadyInstalledException;
import org.eclipse.slm.resource_management.features.capabilities.jobs.CapabilityJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ImporterService {

    private final static Logger LOG = LoggerFactory.getLogger(ImporterService.class);

    private final ResourcesManager resourcesManager;

    private final CapabilityJobService capabilityJobService;

    private final LocationHandler locationHandler;

    private final ResourcesSubmodelManager resourcesSubmodelManager;

    private final RemoteAccessManager remoteAccessManager;

    private final PlatformManagementClientFactory platformManagementClientFactory;

    public ImporterService(ResourcesManager resourcesManager,
                           CapabilityJobService capabilityJobService,
                           LocationHandler locationHandler,
                           ResourcesSubmodelManager resourcesSubmodelManager,
                           RemoteAccessManager remoteAccessManager,
                           PlatformManagementClientFactory platformManagementClientFactory) {
        this.resourcesManager = resourcesManager;
        this.capabilityJobService = capabilityJobService;
        this.locationHandler = locationHandler;
        this.resourcesSubmodelManager = resourcesSubmodelManager;
        this.remoteAccessManager = remoteAccessManager;
        this.platformManagementClientFactory = platformManagementClientFactory;
    }

    public ImportDefinition getImportDefinition(MultipartFile importFile) {
        try {
            var importFileInputStream = new BufferedInputStream(importFile.getInputStream());

            ImportDefinition importDefinition;
            if (importFile.getOriginalFilename().contains(".xlsx")) {
                var excelImporter = new ExcelImporter();
                importDefinition = excelImporter.importExcel(importFileInputStream);
            } else if (importFile.getOriginalFilename().contains(".zip")) {
                var zipImporter = new ZipImporter();
                importDefinition = zipImporter.importZip(importFileInputStream);
            } else {
                throw new ResourceManagementImportBadRequestException("Unsupported file type. Only .xlsx and .zip are supported.");
            }

            return importDefinition;
        } catch (Exception e) {
            throw new ResourceManagementImportRuntimeException("Error importing file: " + e.getMessage());
        }
    }

    public void importDevices(JwtAuthenticationToken jwtAuthenticationToken, ImportDefinition importDefinition, String fullPathOwnerGroupId) {
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        var platformManagementClientUser = this.platformManagementClientFactory.create(userAccessToken);

        for (var location : importDefinition.getLocations().entrySet()) {
            this.locationHandler.addLocation(new Location(location.getKey(), location.getValue()));
        }

        for (var device : importDefinition.getDevices()) {
            try {
                var addedResource = this.resourcesManager.createResource(
                        device.resourceId,
                        device.assetId,
                        device.hostname,
                        device.ipAddress,
                        device.firmwareVersion,
                        null,
                        new DigitalNameplateV3(),
                        fullPathOwnerGroupId);

                if (device.connectionPort != null
                        && device.connectionType != null
                        && device.username != null
                        && device.password != null) {

                    var credential = new Credential(UUID.randomUUID(), "",List.of(), new CredentialDataUsernamePassword(device.username, device.password));
                    var credentialCreateRequest = new CredentialCreateRequest(List.of(), credential, fullPathOwnerGroupId);
                    platformManagementClientUser.credentials().createOrUpdateCredential(credential.getId(), credentialCreateRequest);

                    this.remoteAccessManager.addRemoteAccessForResource(
                            device.resourceId,
                            new RemoteAccessCreateDTO(fullPathOwnerGroupId, credential.getId(), null, device.connectionPort, device.connectionType),
                            userAccessToken
                    );

                }

                if (device.locationId != null) {
                    this.resourcesManager.setLocationOfResource(addedResource.getId(), device.locationId, userAccessToken);
                }
            } catch (Exception e) {
                throw new ResourceManagementImportRuntimeException("Error importing devices: " + e.getMessage());
            }
        }
    }

    public void importAasxFiles(JwtAuthenticationToken jwtAuthenticationToken, ImportDefinition importDefinition) {
        for (var aasxFilesEntry : importDefinition.getAasxFiles().entrySet()) {
            var resourceId = aasxFilesEntry.getKey();
            for (var aasxFile : aasxFilesEntry.getValue()) {
                try {
                    this.resourcesSubmodelManager.addSubmodelsFromAASX(ResourceAas.createAasIdFromResourceId(resourceId), aasxFile);
                } catch (Exception e) {
                    throw new ResourceManagementImportRuntimeException("Error importing AASX files: " + e.getMessage());
                }
            }
        }
    }

    public void importCapabilities(JwtAuthenticationToken jwtAuthenticationToken, ImportDefinition importDefinition, boolean forceInstall, String fullPathOwnerGroupId) {
        for (var device : importDefinition.getDevices()) {
            for (var capability : device.capabilities) {
                try {
                    this.capabilityJobService.initCapabilityJob(jwtAuthenticationToken, device.resourceId, capability.getCapabilityId(), capability.isSkipInstall(), Map.of(), forceInstall, fullPathOwnerGroupId);
                } catch (CapabilityAlreadyInstalledException e) {
                    LOG.info("Capability {} already installed for resource '{}'", capability.getCapabilityId(), device.resourceId);
                } catch (Exception e) {
                    LOG.error("Failed to install capability {} for resource '{}': {}", capability.getCapabilityId(), device.resourceId, e.getMessage());
                }
            }
        }
    }
}
