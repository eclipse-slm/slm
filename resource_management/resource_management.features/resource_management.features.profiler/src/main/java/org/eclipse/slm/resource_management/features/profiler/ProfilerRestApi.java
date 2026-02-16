package org.eclipse.slm.resource_management.features.profiler;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfilerRestApi {

    @RequestMapping(method = RequestMethod.POST)
    @Operation(summary = "Create Profiler")
    ResponseEntity<Profiler> createProfiler(@RequestBody ProfilerDTOApi profilerDTOApi);

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all Profiler")
    @ResponseBody
    ResponseEntity<List<Profiler>> getProfiler();

    @RequestMapping(value = "/execute", method = RequestMethod.POST)
    @Operation(summary = "Run all Profiler")
    ResponseEntity<Void> runProfiler();

    @RequestMapping(value = "/{profilerId}", method = RequestMethod.GET)
    @Operation(summary = "Get Profiler")
    ResponseEntity<Optional<Profiler>> getProfiler(@PathVariable(name = "profilerId") UUID profilerId);

    @RequestMapping(value = "/{profilerId}/execute", method = RequestMethod.POST)
    @Operation(summary = "Run one Profiler")
    ResponseEntity<Void> runProfiler(@PathVariable(name = "profilerId") UUID profilerId);

    @RequestMapping(value = "/{profilerId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete Profiler")
    ResponseEntity<Void> deleteProfiler(@PathVariable(name = "profilerId") UUID profilerId);
}

