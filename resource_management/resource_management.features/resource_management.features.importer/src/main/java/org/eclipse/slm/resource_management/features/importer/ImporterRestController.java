package org.eclipse.slm.resource_management.features.importer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/importer")
@Tag(name = "Importer")
public class ImporterRestController {

    public final static Logger LOG = LoggerFactory.getLogger(ImporterRestController.class);

    private final ImporterService importerService;

    public ImporterRestController(ImporterService importerService) {
        this.importerService = importerService;
    }

    @RequestMapping(value = "/resources", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, method = RequestMethod.POST)
    @Operation(summary = "Import resources from file")
    public ResponseEntity<Void> importFromFile(
            @RequestParam(name = "file") MultipartFile importFile
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var importDefinition = this.importerService.getImportDefinition(importFile);
        this.importerService.importDevices(jwtAuthenticationToken, importDefinition);
        this.importerService.importAasxFiles(jwtAuthenticationToken, importDefinition);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/capabilities", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, method = RequestMethod.POST)
    @Operation(summary = "Import capabilities from file")
    public ResponseEntity<Void> importCapabilitiesFromFile(
            @RequestParam(name = "file") MultipartFile importFile,
            @RequestParam(name = "forceInstall", required = false, defaultValue = "false") boolean forceInstall
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var importDefinition = this.importerService.getImportDefinition(importFile);
        this.importerService.importCapabilities(jwtAuthenticationToken, importDefinition, forceInstall);

        return ResponseEntity.ok().build();
    }
}

