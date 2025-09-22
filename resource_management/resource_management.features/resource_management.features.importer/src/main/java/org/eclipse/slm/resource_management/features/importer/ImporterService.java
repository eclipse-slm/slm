package org.eclipse.slm.resource_management.features.importer;

import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.eclipse.slm.resource_management.common.aas.ResourcesSubmodelManager;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.common.location.Location;
import org.eclipse.slm.resource_management.common.location.LocationHandler;
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
import java.util.Map;

@Component
public class ImporterService {

    public final static Logger LOG = LoggerFactory.getLogger(ImporterService.class);

    private final ResourcesManager resourcesManager;

    private final CapabilityJobService capabilityJobService;

    private final LocationHandler locationHandler;

    private final ResourcesSubmodelManager resourcesSubmodelManager;

    private final RemoteAccessManager remoteAccessManager;

    public ImporterService(ResourcesManager resourcesManager,
                           CapabilityJobService capabilityJobService,
                           LocationHandler locationHandler,
                           ResourcesSubmodelManager resourcesSubmodelManager,
                           RemoteAccessManager remoteAccessManager) {
        this.resourcesManager = resourcesManager;
        this.capabilityJobService = capabilityJobService;
        this.locationHandler = locationHandler;
        this.resourcesSubmodelManager = resourcesSubmodelManager;
        this.remoteAccessManager = remoteAccessManager;
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

    public void importDevices(JwtAuthenticationToken jwtAuthenticationToken, ImportDefinition importDefinition) {

        for (var location : importDefinition.getLocations().entrySet()) {
            this.locationHandler.addLocation(new Location(location.getKey(), location.getValue()));
        }

        for (var device : importDefinition.getDevices()) {
            try {
                var addedResource = this.resourcesManager.addResource(jwtAuthenticationToken,
                        device.resourceId, device.assetId, device.hostname, device.ipAddress, device.firmwareVersion, null, new DigitalNameplateV3());

                if (device.connectionPort != null
                        && device.connectionType != null
                        && device.username != null
                        && device.password != null) {
                    var userId = jwtAuthenticationToken.getToken().getSubject();
                    this.remoteAccessManager.addUsernamePasswordRemoteAccessService(userId,
                            device.resourceId,
                            device.connectionType,
                            device.connectionPort,
                            device.username,
                            device.password);
                }

                if (device.locationId != null) {
                    this.resourcesManager.setLocationOfResource(addedResource.getId(), device.locationId);
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

    public void importCapabilities(JwtAuthenticationToken jwtAuthenticationToken, ImportDefinition importDefinition, boolean forceInstall) {
        for (var device : importDefinition.getDevices()) {
            for (var capability : device.capabilities) {
                try {
                    this.capabilityJobService.initCapabilityJob(jwtAuthenticationToken, device.resourceId, capability.getCapabilityId(), capability.isSkipInstall(), Map.of(), forceInstall);
                } catch (CapabilityAlreadyInstalledException e) {
                    LOG.info("Capability {} already installed for resource '{}'", capability.getCapabilityId(), device.resourceId);
                } catch (Exception e) {
                    LOG.error("Failed to install capability {} for resource '{}': {}", capability.getCapabilityId(), device.resourceId, e.getMessage());
                }
            }
        }
    }
}
