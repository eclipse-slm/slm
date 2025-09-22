package org.eclipse.slm.resource_management.features.capabilities.jobs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityJobDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
@Tag(name = "Capabilities")
public class CapabilityJobsRestController {
    private final static Logger LOG = LoggerFactory.getLogger(CapabilityJobsRestController.class);

    private final CapabilityJobService capabilityJobService;

    @Autowired
    public CapabilityJobsRestController(CapabilityJobService capabilityJobService) {
        this.capabilityJobService = capabilityJobService;
    }

    @RequestMapping(value = "/{resourceId}/capabilities/jobs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get capability jobs of resource")
    public ResponseEntity<List<CapabilityJobDTO>> getCapabilityJobsOfResource(@PathVariable(name = "resourceId") UUID resourceId) {
        var capabilityJobsOfResource = this.capabilityJobService.getCapabilityJobsOfResource(resourceId);
        var dtos = CapabilityJobMapper.INSTANCE.toDtoList(capabilityJobsOfResource);

        return ResponseEntity.ok(dtos);
    }

    @RequestMapping(value = "/{resourceId}/capabilities", method = RequestMethod.PUT)
    @Operation(summary = "Install capability on resource")
    public ResponseEntity installCapabilityOnSingleHost(
            @PathVariable(name = "resourceId")                                                   UUID resourceId,
            @RequestParam(name = "capabilityId")                                                 UUID capabilityId,
            @RequestParam(name = "skipInstall", required = false, defaultValue = "false")        boolean skipInstall,
            @RequestParam(name = "forceInstall", required = false, defaultValue = "false")        boolean forceInstall,
            @RequestBody Map<String, String> configParameters
    ) throws Exception {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        this.capabilityJobService.initCapabilityJob(jwtAuthenticationToken, resourceId, capabilityId, skipInstall, configParameters, forceInstall);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value="/{resourceId}/capabilities", method = RequestMethod.DELETE)
    @Operation(summary = "Uninstall capability from resource")
    public ResponseEntity removeCapabilityFromSingleHost(
            @PathVariable(name = "resourceId")           UUID resourceId,
            @RequestParam(name = "capabilityId")      UUID capabilityId
    ) throws Exception {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        this.capabilityJobService.uninstallCapability(jwtAuthenticationToken, resourceId, capabilityId);

        return ResponseEntity.ok().build();
    }

}
