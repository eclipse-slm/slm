package org.eclipse.slm.resource_management.service.rest.importer;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityNotFoundException;
import org.eclipse.slm.resource_management.model.resource.Location;
import org.eclipse.slm.resource_management.model.resource.ResourceAas;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.service.importer.ExcelImporter;
import org.eclipse.slm.resource_management.service.importer.ImportDefinition;
import org.eclipse.slm.resource_management.service.importer.ZipImporter;
import org.eclipse.slm.resource_management.service.rest.aas.SubmodelManager;
import org.eclipse.slm.resource_management.service.rest.aas.resources.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.service.rest.location.LocationHandler;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/importer")
public class ImporterRestController {

    private final ResourcesManager resourcesManager;

    private final LocationHandler locationHandler;

    private final SubmodelManager submodelManager;


    public ImporterRestController(ResourcesManager resourcesManager, LocationHandler locationHandler, SubmodelManager submodelManager) {
        this.resourcesManager = resourcesManager;
        this.locationHandler = locationHandler;
        this.submodelManager = submodelManager;
    }

    @RequestMapping(value = "/file", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, method = RequestMethod.POST)
    @Operation(summary = "Import from file")
    public ResponseEntity<Void> importFromFile(
            @RequestParam(name = "file") MultipartFile importFile
    ) throws IOException, CapabilityNotFoundException, ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, InvalidFormatException, DeserializationException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var importFileInputStream = new BufferedInputStream(importFile.getInputStream());

        ImportDefinition importDefinition;
        if (importFile.getOriginalFilename().contains(".xlsx")) {
            var excelImporter = new ExcelImporter();
            importDefinition = excelImporter.importExcel(importFileInputStream);
        } else if (importFile.getOriginalFilename().contains(".zip")) {
            var zipImporter = new ZipImporter();
            importDefinition = zipImporter.importZip(importFileInputStream);
        }
        else {
            return ResponseEntity.badRequest().build();
        }

        for (var location : importDefinition.getLocations().entrySet()) {
            locationHandler.addLocation(new Location(location.getKey(), location.getValue()));
        }

        for (var device : importDefinition.getDevices()) {
            var addedResource = resourcesManager.addExistingResource(jwtAuthenticationToken,
                    device.resourceId, device.assetId, device.hostname, device.ipAddress, device.firmwareVersion, new DigitalNameplateV3());

            if (device.connectionPort != null
                && device.connectionType != null
                && device.username != null
                && device.password != null) {
                resourcesManager.setRemoteAccessOfResource(jwtAuthenticationToken,
                        device.resourceId,
                        device.username,
                        device.password,
                        device.connectionType,
                        device.connectionPort);
            }

            if (device.locationId != null) {
                resourcesManager.setLocationOfResource(addedResource.getId(), device.locationId);
            }
        }


        for (var aasxFilesEntry : importDefinition.getAasxFiles().entrySet()) {
            var resourceId = aasxFilesEntry.getKey();
            for (var aasxFile : aasxFilesEntry.getValue()) {
                this.submodelManager.addSubmodelsFromAASX(ResourceAas.createAasIdFromResourceId(resourceId), aasxFile);
            }
        }

        return ResponseEntity.ok().build();
    }
}

