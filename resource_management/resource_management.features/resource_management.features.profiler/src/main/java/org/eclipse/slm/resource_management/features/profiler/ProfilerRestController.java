package org.eclipse.slm.resource_management.features.profiler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(ProfilerRestApiConfig.BASE_PATH)
@Tag(name = ProfilerRestApiConfig.TAG)
public class ProfilerRestController implements ProfilerRestApi {
    private static final Logger LOG = LoggerFactory.getLogger(ProfilerRestController.class);

    private final ProfilerService profilerService;

    public ProfilerRestController(ProfilerService profilerService) {
        this.profilerService = profilerService;
    }

    public ResponseEntity<Profiler> createProfiler(ProfilerDTOApi profilerDTOApi) {
        var createdProfiler = profilerService.createProfiler(
                ProfilerToProfilerDTOApiMapper.INSTANCE.toEntity(profilerDTOApi)
        );

        return ResponseEntity.ok(createdProfiler);
    }

    public ResponseEntity<List<Profiler>> getProfiler() {
        return ResponseEntity.ok(profilerService.getProfiler());
    }

    public ResponseEntity<Void> runProfiler() {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        profilerService.runAllProfilerAction(
                jwtAuthenticationToken
        );

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Optional<Profiler>> getProfiler(UUID profilerId) {
        return ResponseEntity.ok(profilerService.getProfiler(profilerId));
    }

    public ResponseEntity<Void> runProfiler(UUID profilerId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        profilerService.runProfilerAction(
                profilerId,
                jwtAuthenticationToken
        );

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> deleteProfiler(UUID profilerId) {
        profilerService.deleteProfiler(profilerId);

        return ResponseEntity.ok().build();
    }
}
