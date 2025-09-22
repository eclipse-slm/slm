package org.eclipse.slm.resource_management.features.device_integration.discovery;

import org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver.DriverInfo;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver.DriverRegistryClient;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.exceptions.DriverNotFoundException;
import org.eclipse.slm.resource_management.features.device_integration.discovery.api.DiscoveryJobStartedResponse;
import org.eclipse.slm.resource_management.features.device_integration.discovery.api.OnboardingRequest;
import org.eclipse.slm.resource_management.features.device_integration.discovery.dto.DiscoveredResourceDTO;
import org.eclipse.slm.resource_management.features.device_integration.discovery.exceptions.DiscoveryJobNotFoundException;
import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveryJob;
import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveryRequest;
import org.eclipse.slm.resource_management.features.device_integration.discovery.persistence.DiscoveryJobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resources/discovery")
@Tag(name = "Discovery")
public class DiscoveryRestController {

    private final DriverRegistryClient driverRegistryClient;

    private final DiscoveryJobRepository discoveryJobRepository;

    private final DiscoveryService discoveryService;

    public DiscoveryRestController(DriverRegistryClient driverRegistryClient,
                                   DiscoveryJobRepository discoveryJobRepository,
                                   DiscoveryService discoveryService) {
        this.driverRegistryClient = driverRegistryClient;
        this.discoveryJobRepository = discoveryJobRepository;
        this.discoveryService = discoveryService;
    }

    @RequestMapping(value = "/jobs", method = RequestMethod.GET)
    public ResponseEntity<Collection<DiscoveryJob>> getDiscoveryJobs() {
        var discoveryJobs = this.discoveryJobRepository.findAll();

        return ResponseEntity.ok(discoveryJobs);
    }

    @RequestMapping(value = "/jobs/{discoveryJobId}", method = RequestMethod.GET)
    public ResponseEntity<DiscoveryJob> getDiscoveryJob(@PathVariable(name = "discoveryJobId") UUID discoveryJobId)
            throws DiscoveryJobNotFoundException {
        var discoveryJobOptional = this.discoveryJobRepository.findById(discoveryJobId);

        if (discoveryJobOptional.isPresent()) {
            return ResponseEntity.ok(discoveryJobOptional.get());
        }
        else {
            throw new DiscoveryJobNotFoundException(discoveryJobId);
        }
    }

    @RequestMapping(value = "/jobs/{discoveryJobId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteDiscoveryJob(@PathVariable(name = "discoveryJobId") UUID discoveryJobId)
            throws DiscoveryJobNotFoundException {
        var discoveryJobOptional = this.discoveryJobRepository.findById(discoveryJobId);
        if (discoveryJobOptional.isPresent()) {
            this.discoveryJobRepository.delete(discoveryJobOptional.get());
            return ResponseEntity.ok().build();
        }
        else {
            throw new DiscoveryJobNotFoundException(discoveryJobId);
        }
    }

    @RequestMapping(value = "/drivers", method = RequestMethod.GET)
    public ResponseEntity<List<DriverInfo>> getRegisteredDrivers() {
        var driverInfos = this.driverRegistryClient.getRegisteredDrivers();

        return ResponseEntity.ok(driverInfos);
    }

    @RequestMapping(value = "/drivers/{driverId}/discover", method = RequestMethod.POST)
    public ResponseEntity<DiscoveryJobStartedResponse> discover(@PathVariable(name = "driverId") String driverId,
                                                                @RequestBody DiscoveryRequest discoveryRequest)
            throws DriverNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var discoveryJobId = this.discoveryService.discover(jwtAuthenticationToken, driverId, discoveryRequest);

        var response = new DiscoveryJobStartedResponse(discoveryJobId);

        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/inbox", method = RequestMethod.GET)
    public ResponseEntity<Collection<DiscoveredResourceDTO>> getDiscoveredResources(
            @RequestParam(name = "removeDuplicate", required = false, defaultValue = "false") boolean removeDuplicate,
            @RequestParam(name = "onlyLatestJobs", required = false, defaultValue = "false") boolean onlyLatestJobs,
            @RequestParam(name = "includeIgnored", required = false, defaultValue = "false") boolean includeIgnored,
            @RequestParam(name = "includedOnboarded", required = false, defaultValue = "false") boolean includedOnboarded
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var discoveredResources = this.discoveryService.getDiscoveredResources(jwtAuthenticationToken, removeDuplicate,
                onlyLatestJobs, includeIgnored, includedOnboarded);

        return ResponseEntity.ok(discoveredResources);
    }

    @RequestMapping(value = "/inbox/onboard", method = RequestMethod.POST)
    public ResponseEntity<Void> onboardDiscoveredResources(@RequestBody OnboardingRequest onboardingRequest
    ) throws Exception {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        for(var resultId : onboardingRequest.getResultIds()) {
            this.discoveryService.onboard(jwtAuthenticationToken, resultId);
        }

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/inbox/ignore", method = RequestMethod.POST)
    public ResponseEntity<Void> ignoreDiscoveredResource(
            @RequestParam(name = "resultId") String resultId,
            @RequestParam(name = "ignored", required = false, defaultValue = "true") boolean ignored) {

        this.discoveryService.ignore(resultId, ignored);

        return ResponseEntity.ok().build();
    }
}

