package org.eclipse.slm.resource_management.service.rest.profiler;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.resource_management.model.profiler.Profiler;
import org.eclipse.slm.resource_management.model.profiler.ProfilerDTOApi;
import org.modelmapper.ModelMapper;
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
@RequestMapping("/resources/profiler")
public class ProfilerRestController {
    private static final Logger LOG = LoggerFactory.getLogger(ProfilerRestController.class);
    private final ProfilerManager profilerManager;
    private ModelMapper modelMapper = new ModelMapper();

    public ProfilerRestController(ProfilerManager profilerManager) {
        this.profilerManager = profilerManager;

        //modelMapper.typeMap(ProfilerDTOApi.class, Profiler.class)
        //        .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), Profiler.class));
    }

    @RequestMapping(method = RequestMethod.POST)
    @Operation(summary = "Create Profiler")
    public Profiler createProfiler(@RequestBody ProfilerDTOApi profilerDTOApi) {
        return profilerManager.createProfiler(
                modelMapper.map(profilerDTOApi, Profiler.class)
        );
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all Profiler")
    @ResponseBody
    public List<Profiler> getProfiler() {
        return profilerManager.getProfiler();
    }

    @RequestMapping(value = "/execute", method = RequestMethod.POST)
    @Operation(summary = "Run all Profiler")
    public void runProfiler() {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        profilerManager.runAllProfilerAction(
                jwtAuthenticationToken
        );
    }

    @RequestMapping(value = "/{profilerId}", method = RequestMethod.GET)
    @Operation(summary = "Get Profiler")
    public Optional<Profiler> getProfiler(@PathVariable UUID profilerId) {
        return profilerManager.getProfiler(profilerId);
    }

    @RequestMapping(value = "/{profilerId}/execute", method = RequestMethod.POST)
    @Operation(summary = "Run one Profiler")
    public void runProfiler(@PathVariable UUID profilerId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        profilerManager.runProfilerAction(
                profilerId,
                jwtAuthenticationToken
        );
    }

    @RequestMapping(value = "/{profilerId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete Profiler")
    public void deleteProfiler(@PathVariable UUID profilerId) {
        profilerManager.deleteProfiler(profilerId);
    }
}

