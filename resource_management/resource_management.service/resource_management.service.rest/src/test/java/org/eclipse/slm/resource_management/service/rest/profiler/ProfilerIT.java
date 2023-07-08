package org.eclipse.slm.resource_management.service.rest.profiler;

import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.resource_management.persistence.api.ProfilerJpaRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.Assert.assertNotNull;

@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {
        ProfilerManager.class,
        ProfilerRestController.class,
        ProfilerJpaRepository.class
})
@ActiveProfiles("test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class ProfilerIT {
    @Autowired
    ProfilerManager profilerManager;
    @Autowired
    ProfilerRestController profilerRestController;
    @Autowired
    ProfilerJpaRepository profilerJpaRepository;
    @MockBean
    AwxClient awxClient;
    @MockBean
    AwxJobExecutor awxJobExecutor;

    @Nested
    @Order(10)
    @DisplayName("Pretests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class preTests {
        @Test
        @Order(10)
        public void testCapabilitiesManagerNotNull() {
            assertNotNull(profilerManager);
            assertNotNull(profilerRestController);
        }
    }
}
