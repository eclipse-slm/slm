package org.eclipse.slm.resource_management.service.rest.capabilities;

import org.eclipse.slm.common.awx.model.SurveyItem;
import org.eclipse.slm.common.awx.model.SurveyItemType;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.model.capabilities.Capability;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityType;
import org.eclipse.slm.resource_management.model.capabilities.DeploymentCapability;
import org.eclipse.slm.resource_management.model.capabilities.VirtualizationCapability;
import org.eclipse.slm.resource_management.model.actions.AwxAction;
import org.eclipse.slm.resource_management.model.actions.Action;
import org.eclipse.slm.resource_management.model.actions.ActionType;
import org.eclipse.slm.resource_management.persistence.api.CapabilityJpaRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class CapabilityJpaTest {
    //region Variables
    public final static Logger LOG = LoggerFactory.getLogger(CapabilityJpaTest.class);
    @Autowired
    private static CapabilityJpaRepository staticCapabilityJpaRepository;
    //endregion

    @Autowired
    public CapabilityJpaTest(CapabilityJpaRepository capabilityJpaRepository) {
        staticCapabilityJpaRepository = capabilityJpaRepository;
    }

    @Nested
    @Order(10)
    @DisplayName("Pretest")
    public class doPreTest{
        @Test
        @Order(10)
        void injectedComponentsAreNotNull(){
            assertNotNull(staticCapabilityJpaRepository);
        }
    }

    @Nested
    @Order(20)
    @Commit
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Deployment Capability")
    public class testDeploymentCapability {

        //region Variables - Deployment Capability
        private static DeploymentCapability dockerDeploymentCapability;

        private static List<DeploymentCapability> deploymentCapabilityList = new ArrayList<>();
        //endregion

        @BeforeAll
        static void beforeAll() {
            int dcCount = 5;

            ArrayList<DeploymentCapability> list = new ArrayList<>(dcCount);
            DeploymentCapability dc = null;

            for(int i = 0; i < dcCount; i++) {
                //region Create Java Object of Docker DeploymentCapability:
                dc = new DeploymentCapability();
                dc.setName("Docker");
                dc.setLogo("mdi-docker");
                dc.setType(Arrays.asList(
                        CapabilityType.SETUP,
                        CapabilityType.DEPLOY
                ));
                dc.setCapabilityClass("DeploymentCapability");

                dc.setSupportedDeploymentTypes(Arrays.asList(
                        DeploymentType.DOCKER_CONTAINER,
                        DeploymentType.DOCKER_COMPOSE
                ));

                // Set AWX Capability Actions

                HashMap<ActionType, Action> capabilityActions = new HashMap();
                capabilityActions.put(
                        ActionType.INSTALL,
                        new AwxAction("awxRepo", "awxBranch", "playbook")
                );

                dc.setActions(capabilityActions);
                //endregion

                list.add(dc);
            }

            deploymentCapabilityList = list;
            dockerDeploymentCapability = list.get(0);
        }

        @AfterAll
        private static void afterAll() {
            staticCapabilityJpaRepository.deleteAll();
        }

        @Test
        @Order(10)
        void findAllIfNoCapabilityPersisted() {
            List<Capability> capabilities = staticCapabilityJpaRepository.findAll();

            assertEquals(0, capabilities.size());
        }

        @Test
        @Order(20)
        public void persistOneDeploymentCapability() {
            List<Capability> capabilitiesBefore = staticCapabilityJpaRepository.findAll();

            DeploymentCapability persistedSingleHostDeploymentCapability = staticCapabilityJpaRepository.save(dockerDeploymentCapability);

            assertEquals(capabilitiesBefore.size()+1, staticCapabilityJpaRepository.findAll().size());
        }

        @Test
        @Order(30)
        public void getDeploymentCapabilityById() {
            Optional<Capability> optionalFoundDeploymentCapability = staticCapabilityJpaRepository.findById(
                    dockerDeploymentCapability.getId()
            );

            assertEquals(
                    true,
                    optionalFoundDeploymentCapability.isPresent()
            );

            Capability foundDeploymentCapability = optionalFoundDeploymentCapability.get();

            assertEquals(
                    dockerDeploymentCapability.getId(),
                    foundDeploymentCapability.getId()
            );
        }

        @Test
        @Order(40)
        public void removeOneDeploymentCapability() {
            List<Capability> capabilitiesBefore = staticCapabilityJpaRepository.findAll();

            staticCapabilityJpaRepository.delete(dockerDeploymentCapability);

            assertEquals(capabilitiesBefore.size()-1, staticCapabilityJpaRepository.findAll().size());
        }

        @Test
        @Order(50)
        public void persistMultipleDeploymentCapabilities() {
            List<Capability> capabilitiesBefore = staticCapabilityJpaRepository.findAll();

            staticCapabilityJpaRepository.saveAll(deploymentCapabilityList);

            assertEquals(
                    capabilitiesBefore.size()+deploymentCapabilityList.size(),
                    staticCapabilityJpaRepository.findAll().size()
            );
        }

        @Test
        @Order(60)
        public void removeAllDeploymentCapabilities() {
            assertNotEquals(
                    0,
                    staticCapabilityJpaRepository.findAll().size()
            );

            staticCapabilityJpaRepository.deleteAll();

            assertEquals(
                    0,
                    staticCapabilityJpaRepository.findAll().size()
            );
        }

        @Test
        @Order(70)
        void testUsernamePasswordGetNotPersisted() {
            //Add Username/Password to all actions of DC:
            dockerDeploymentCapability.getActions().forEach((k,v) -> {
                AwxAction awxCapabilityAction = (AwxAction) v;
                awxCapabilityAction.setUsername("username");
                awxCapabilityAction.setPassword("password");
            });

            staticCapabilityJpaRepository.save(dockerDeploymentCapability);

            //Get all capabilities from DB
            List<Capability> persistedCapabilities = staticCapabilityJpaRepository.findAll();

            //Check persisted DC has correct properties:
            Capability persistedCapability = persistedCapabilities.stream().filter(c -> c.getName().equals(dockerDeploymentCapability.getName())).findFirst().get();

            //Assert all actions have empty username/password:
            persistedCapability.getActions().forEach((k,v) -> {
                AwxAction awxCapabilityAction = (AwxAction) v;
                assertEquals("", awxCapabilityAction.getUsername());
                assertEquals("", awxCapabilityAction.getPassword());
            });
        }
    }

    @Nested
    @Order(30)
    @Commit
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Virtualization Capability")
    public class testVirtualizationCapability {
        //region Variables - Deployment Capability
        private static VirtualizationCapability virtualizationCapability;

        private static List<VirtualizationCapability> virtualizationCapabilityList = new ArrayList<>();

        private static List<SurveyItem> awxCapabilityParams;
        //endregion

        @BeforeAll
        static void beforeAll() {
            int vcCount = 5;

            ArrayList<VirtualizationCapability> list = new ArrayList<>(vcCount);
            VirtualizationCapability vc = null;

            for(int i = 0; i < vcCount; i++) {
                //region Create Java Object of Docker DeploymentCapability:
                vc = new VirtualizationCapability();
                vc.setName("KVM/QEMU");
                vc.setLogo("mdi-kvm");
                vc.setType(Arrays.asList(
                        CapabilityType.SETUP,
                        CapabilityType.DEPLOY,
                        CapabilityType.VM
                ));
                vc.setCapabilityClass("VirtualizationCapability");

                // Set AWX Capability Actions
                HashMap<ActionType, Action> capabilityActions = new HashMap();
                capabilityActions.put(
                        ActionType.INSTALL,
                        new AwxAction("awxRepo", "awxBranch", "install.yml")
                );
                capabilityActions.put(
                        ActionType.UNINSTALL,
                        new AwxAction("awxRepo", "awxBranch", "uninstall.yml")
                );
                capabilityActions.put(
                        ActionType.CREATE_VM,
                        new AwxAction("awxRepo", "awxBranch", "create_vm.yml")
                );
                capabilityActions.put(
                        ActionType.DELETE_VM,
                        new AwxAction("awxRepo", "awxBranch", "delete_vm.yml")
                );

                vc.setActions(capabilityActions);
                //endregion

                list.add(vc);
            }

            virtualizationCapabilityList = list;
            virtualizationCapability = list.get(0);

            //region Survey Items
            SurveyItem surveyItemText = new SurveyItem(
                    SurveyItemType.text,
                    "Question 1",
                    "Question 1 Description",
                    "question_1",
                    new ArrayList<String>(),
                    null,
                    null,
                    false,
                    "default-text"
            );

            SurveyItem surveyItemMultiplechoice = new SurveyItem(
                    SurveyItemType.multiplechoice,
                    "Question 2",
                    "Question 2 Description",
                    "question_2",
                    Arrays.asList("Choice1", "Choice2", "Choice3"),
                    null,
                    null,
                    true,
                    ""
            );

            SurveyItem surveyItemInteger = new SurveyItem(
                    SurveyItemType.integer,
                    "Question 3",
                    "Question 3 Description",
                    "question_3",
                    new ArrayList<String>(),
                    3,
                    45,
                    true,
                    ""
            );

            SurveyItem surveyItemPassword = new SurveyItem(
                    SurveyItemType.password,
                    "Question 4",
                    "Question 4 Description",
                    "question_4",
                    new ArrayList<String>(),
                    null,
                    null,
                    true,
                    ""
            );

            SurveyItem surveyItemMultiselect = new SurveyItem(
                    SurveyItemType.multiselect,
                    "Question 5",
                    "Question 5 Description",
                    "question_5",
                    Arrays.asList("Choice4", "Choice5", "Choice6"),
                    null,
                    null,
                    false,
                    ""
            );
            awxCapabilityParams = Arrays.asList(
                    surveyItemText,
                    surveyItemPassword,
                    surveyItemInteger,
                    surveyItemMultiplechoice,
                    surveyItemMultiselect
            );
            //endregion
        }

        @AfterAll
        private static void afterAll() {
            staticCapabilityJpaRepository.deleteAll();
        }

        @Test
        @Order(10)
        void findAllIfNoCapabilityPersisted() {
            List<Capability> capabilities = staticCapabilityJpaRepository.findAll();

            assertEquals(0, capabilities.size());
        }

        @Test
        @Order(20)
        void persistOneVirtualizationCapability() {
            List<Capability> capabilitiesBefore = staticCapabilityJpaRepository.findAll();

            VirtualizationCapability persistedVirtualizationCapability = staticCapabilityJpaRepository.save(virtualizationCapability);

            assertEquals(capabilitiesBefore.size()+1, staticCapabilityJpaRepository.findAll().size());
        }

        @Test
        @Order(30)
        void getVirtualizationCapabilityById() {
            Optional<Capability> optionalFoundVirtualizationCapability = staticCapabilityJpaRepository.findById(
                    virtualizationCapability.getId()
            );

            assertEquals(
                    true,
                    optionalFoundVirtualizationCapability.isPresent()
            );

            Capability foundVirtualizationCapability = optionalFoundVirtualizationCapability.get();

            assertEquals(
                    virtualizationCapability.getId(),
                    foundVirtualizationCapability.getId()
            );

            assertEquals(
                    VirtualizationCapability.class,
                    foundVirtualizationCapability.getClass()
            );
        }

        @Test
        @Order(40)
        void removeOneVirtualizationCapability() {
            List<Capability> capabilitiesBefore = staticCapabilityJpaRepository.findAll();

            staticCapabilityJpaRepository.delete(virtualizationCapability);

            assertEquals(capabilitiesBefore.size()-1, staticCapabilityJpaRepository.findAll().size());
        }

        @Test
        @Order(50)
        void persistMultipleVirtualizationCapabilities() {
            List<Capability> capabilitiesBefore = staticCapabilityJpaRepository.findAll();

            staticCapabilityJpaRepository.saveAll(virtualizationCapabilityList);

            assertEquals(
                    capabilitiesBefore.size()+virtualizationCapabilityList.size(),
                    staticCapabilityJpaRepository.findAll().size()
            );
        }

        @Test
        @Order(60)
        void removeAllVirtualizationCapabilities() {
            assertNotEquals(
                    0,
                    staticCapabilityJpaRepository.findAll().size()
            );

            staticCapabilityJpaRepository.deleteAll();

            assertEquals(
                    0,
                    staticCapabilityJpaRepository.findAll().size()
            );
        }

        @Test
        @Order(70)
        void testUsernamePasswordGetNotPersisted() {
            //Add Username/Password to all actions of DC:
            virtualizationCapability.getActions().forEach((k,v) -> {
                AwxAction awxCapabilityAction = (AwxAction) v;
                awxCapabilityAction.setUsername("username");
                awxCapabilityAction.setPassword("password");
            });

            staticCapabilityJpaRepository.save(virtualizationCapability);

            //Get all capabilities from DB
            List<Capability> persistedCapabilities = staticCapabilityJpaRepository.findAll();

            //Check persisted DC has correct properties:
            Capability persistedCapability = persistedCapabilities.stream().filter(c -> c.getName().equals(virtualizationCapability.getName())).findFirst().get();

            //Assert all actions have empty username/password:
            persistedCapability.getActions().forEach((k,v) -> {
                AwxAction awxCapabilityAction = (AwxAction) v;
                assertEquals("", awxCapabilityAction.getUsername());
                assertEquals("", awxCapabilityAction.getPassword());
            });

            staticCapabilityJpaRepository.deleteAll();
        }

        @Test
        @Order(80)
        void persistCapabilityWithAwxActionContainingParameter() {
            //Add params to one Action:
            AwxAction action = (AwxAction) virtualizationCapability.getActions().get(ActionType.CREATE_VM);
            action.setParameter(awxCapabilityParams);
            List<SurveyItem> paramsToPersist = action.getParameter();

            List<Capability> capabilitiesBefore = staticCapabilityJpaRepository.findAll();

            VirtualizationCapability persistedVirtualizationCapability = staticCapabilityJpaRepository.save(virtualizationCapability);

            assertEquals(capabilitiesBefore.size()+1, staticCapabilityJpaRepository.findAll().size());

            AwxAction persistedAction = (AwxAction) persistedVirtualizationCapability.getActions().get(ActionType.CREATE_VM);
            List<SurveyItem> paramsPersisted = persistedAction.getParameter();

            assertIterableEquals(paramsToPersist, paramsPersisted);
        }

    }
}
