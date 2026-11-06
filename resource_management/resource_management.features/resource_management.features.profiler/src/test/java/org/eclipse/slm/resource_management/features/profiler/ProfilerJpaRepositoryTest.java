package org.eclipse.slm.resource_management.features.profiler;

import jakarta.persistence.EntityManager;
import org.eclipse.slm.awx.model.SurveyItem;
import org.eclipse.slm.awx.model.SurveyItemType;
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
    private final static Logger LOG = LoggerFactory.getLogger(ProfilerJpaRepositoryTest.class);

    @Autowired
    private ProfilerJpaRepository profilerJpaRepository;

    @Autowired
    private EntityManager entityManager;

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
        @Order(32)
        public void updateParameterOnly() {
            var surveyItem = new SurveyItem(SurveyItemType.text, "name", "description", "variable");

            assertEquals(List.of(surveyItem), saveAndReload(actionWith(List.of(surveyItem), false)).getParameter());
        }

        @Test
        @Order(34)
        public void updateSurveyItemFieldOnly() {
            var surveyItem = new SurveyItem(SurveyItemType.text, "name", "description", "variable");
            surveyItem.setRequired(true);

            var storedItem = saveAndReload(actionWith(List.of(surveyItem), false)).getParameter().get(0);

            assertTrue(storedItem.getRequired());
        }

        @Test
        @Order(36)
        public void updateSkipableOnly() {
            // First clear the previous test's parameters so skipable is the only changed field.
            saveAndReload(actionWith(null, false));

            assertTrue(saveAndReload(actionWith(null, true)).getSkipable());
        }

        private AwxAction actionWith(List<SurveyItem> parameter, boolean skipable) {
            var action = new AwxAction(testAction.getAwxRepo(), testAction.getAwxBranch(), testAction.getPlaybook());
            action.setParameter(parameter);
            action.setSkipable(skipable);
            return action;
        }

        private AwxAction saveAndReload(AwxAction action) {
            var update = new Profiler(testProfilerId);
            update.setName(testProfiler.getName());
            update.setAction(action);

            profilerJpaRepository.saveAndFlush(update);
            entityManager.clear();

            return (AwxAction) profilerJpaRepository.findById(testProfilerId).orElseThrow().getAction();
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
