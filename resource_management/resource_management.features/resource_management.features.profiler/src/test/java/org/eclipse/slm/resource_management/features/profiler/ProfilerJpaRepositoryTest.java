package org.eclipse.slm.resource_management.features.profiler;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@EnableAutoConfiguration
@ContextConfiguration(classes = {
    ProfilerJpaRepository.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class ProfilerJpaRepositoryTest {
    public final static Logger LOG = LoggerFactory.getLogger(ProfilerJpaRepositoryTest.class);

    @Autowired
    private ProfilerJpaRepository profilerJpaRepository;

    @Nested
    @Order(10)
    @DisplayName("Pre tests")
    public class PreTests {
        @Test
        @Order(10)
        void injectedComponentsAreNotNull(){
            assertNotNull(profilerJpaRepository);
        }
    }

    @Nested
    @Order(20)
    @Commit
    @DisplayName("CRUD tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class CrudTests {
        UUID testProfilerId = null;
        Profiler testProfiler = null;
        AwxAction testAction = new AwxAction(
                "https://github.com/FabOS-AI/fabos-slm-dc-dummy",
                "main",
                "install.yml"
        );

        public CrudTests() {
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
