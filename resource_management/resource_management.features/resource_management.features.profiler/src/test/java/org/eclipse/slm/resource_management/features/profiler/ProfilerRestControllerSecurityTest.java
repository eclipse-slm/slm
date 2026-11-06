package org.eclipse.slm.resource_management.features.profiler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringJUnitConfig(ProfilerRestControllerSecurityTest.MethodSecurityConfig.class)
class ProfilerRestControllerSecurityTest {

    @Configuration
    @EnableMethodSecurity(proxyTargetClass = true)
    @Import(ProfilerRestController.class)
    static class MethodSecurityConfig {
    }

    @Autowired
    ProfilerRestController profilerRestController;

    @MockBean
    ProfilerService profilerService;

    private final UUID profilerId = UUID.randomUUID();

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticateWith(String authority) {
        var authentication = new TestingAuthenticationToken("user", null, authority);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void nonAdminCannotCreateProfiler() {
        authenticateWith("ROLE_slm-user");

        assertThrows(AccessDeniedException.class, () -> profilerRestController.createProfiler(new ProfilerDTOApi()));

        verifyNoInteractions(profilerService);
    }

    @Test
    void nonAdminCannotUpdateProfiler() {
        authenticateWith("ROLE_slm-user");

        assertThrows(AccessDeniedException.class,
                () -> profilerRestController.updateProfiler(profilerId, new ProfilerDTOApi()));

        verifyNoInteractions(profilerService);
    }

    @Test
    void nonAdminCannotDeleteProfiler() {
        authenticateWith("ROLE_slm-user");

        assertThrows(AccessDeniedException.class, () -> profilerRestController.deleteProfiler(profilerId));

        verifyNoInteractions(profilerService);
    }

    @Test
    void nonAdminCanStillReadProfilers() {
        authenticateWith("ROLE_slm-user");

        assertEquals(200, profilerRestController.getProfiler().getStatusCode().value());
    }

    @Test
    void adminCanUpdateProfiler() {
        authenticateWith("ROLE_slm-admin");

        profilerRestController.updateProfiler(profilerId, new ProfilerDTOApi());

        verify(profilerService).updateProfiler(any(), any());
    }

    @Test
    void apiKeyCanCreateProfiler() {
        authenticateWith("API_KEY");

        profilerRestController.createProfiler(new ProfilerDTOApi());

        verify(profilerService).createProfiler(any());
    }
}
