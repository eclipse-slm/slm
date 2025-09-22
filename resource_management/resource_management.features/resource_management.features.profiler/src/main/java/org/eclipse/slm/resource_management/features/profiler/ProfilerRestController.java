package org.eclipse.slm.resource_management.features.profiler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(ProfilerRestControllerConfig.BASE_PATH)
@Tag(name = ProfilerRestControllerConfig.TAG)
public class ProfilerRestController {
    private static final Logger LOG = LoggerFactory.getLogger(ProfilerRestController.class);

    private final ProfilerService profilerService;

    public ProfilerRestController(ProfilerService profilerService) {
        this.profilerService = profilerService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @Operation(summary = "Create Profiler")
    public Profiler createProfiler(@RequestBody ProfilerDTOApi profilerDTOApi) {
        return profilerService.createProfiler(
                ProfilerToProfilerDTOApiMapper.INSTANCE.toEntity(profilerDTOApi)
        );
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all Profiler")
    @ResponseBody
    public List<Profiler> getProfiler() {
        return profilerService.getProfiler();
    }

    @RequestMapping(value = "/execute", method = RequestMethod.POST)
    @Operation(summary = "Run all Profiler")
    public void runProfiler() {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        profilerService.runAllProfilerAction(
                jwtAuthenticationToken
        );
    }

    @RequestMapping(value = "/{profilerId}", method = RequestMethod.GET)
    @Operation(summary = "Get Profiler")
    public Optional<Profiler> getProfiler(@PathVariable(name = "profilerId") UUID profilerId) {
        return profilerService.getProfiler(profilerId);
    }

    @RequestMapping(value = "/{profilerId}/execute", method = RequestMethod.POST)
    @Operation(summary = "Run one Profiler")
    public void runProfiler(@PathVariable(name = "profilerId") UUID profilerId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        profilerService.runProfilerAction(
                profilerId,
                jwtAuthenticationToken
        );
    }

    @RequestMapping(value = "/{profilerId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete Profiler")
    public void deleteProfiler(@PathVariable(name = "profilerId") UUID profilerId) {
        profilerService.deleteProfiler(profilerId);
    }
}

