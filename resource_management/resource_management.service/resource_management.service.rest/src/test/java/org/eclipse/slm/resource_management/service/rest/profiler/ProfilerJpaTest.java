package org.eclipse.slm.resource_management.service.rest.profiler;

import org.eclipse.slm.resource_management.model.actions.AwxAction;
import org.eclipse.slm.resource_management.model.profiler.Profiler;
import org.eclipse.slm.resource_management.persistence.api.ProfilerJpaRepository;
import org.eclipse.slm.resource_management.service.rest.capabilities.CapabilityJpaTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class ProfilerJpaTest {
    public final static Logger LOG = LoggerFactory.getLogger(CapabilityJpaTest.class);
    @Autowired
    private ProfilerJpaRepository profilerJpaRepository;

    @Nested
    @Order(10)
    @DisplayName("Pretest")
    public class doPreTest{
        @Test
        @Order(10)
        void injectedComponentsAreNotNull(){
            assertNotNull(profilerJpaRepository);
        }
    }

    @Nested
    @Order(20)
    @Commit
    @DisplayName("CRUD Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class CrudJPAProfilerTest {
        UUID testProfilerId = null;
        Profiler testProfiler = null;
        AwxAction testAction = new AwxAction(
                "https://github.com/FabOS-AI/fabos-slm-dc-dummy",
                "main",
                "install.yml"
        );

        public CrudJPAProfilerTest() {
            this.testProfiler = new Profiler();
            testProfiler.setName("Test-Profiler");
            testProfiler.setAction(testAction);
        }

        @Test
        @Order(10)
        public void getAllProfilerExpectEmptyList() {
            assertEquals(
                    0,
                    profilerJpaRepository.findAll().size()
            );
        }

        @Test
        @Order(20)
        public void createProfiler() {
            profilerJpaRepository.save(testProfiler);

            List<Profiler> profilerList = profilerJpaRepository.findAll();

            assertEquals(
                    1,
                    profilerList.size()
            );

            testProfilerId = profilerList.get(0).getId();
        }

        @Test
        @Order(30)
        public void getProfiler() {
            Optional<Profiler> optionalProfiler = getProfilerAndTestPresent(true);

            assertTrue(
                optionalProfiler.get().equals(testProfiler)
            );
        }

        @Test
        @Order(40)
        public void deleteProfiler() {
            profilerJpaRepository.deleteById(testProfilerId);

            getProfilerAndTestPresent(false);
        }

        private Optional<Profiler> getProfilerAndTestPresent(Boolean isPresent) {
            Optional<Profiler> optionalProfiler = profilerJpaRepository.findById(testProfilerId);

            assertEquals(
                    isPresent,
                    optionalProfiler.isPresent()
            );

            return optionalProfiler;
        }
    }
}
