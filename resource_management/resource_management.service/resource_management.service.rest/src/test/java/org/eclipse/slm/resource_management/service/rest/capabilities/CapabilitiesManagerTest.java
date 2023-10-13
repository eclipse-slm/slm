package org.eclipse.slm.resource_management.service.rest.capabilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.client.AwxProjectUpdateFailedException;
import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.awx.model.JobTemplate;
import org.eclipse.slm.common.awx.model.SurveyItem;
import org.eclipse.slm.common.awx.model.SurveyItemType;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.notification_service.model.JobFinalState;
import org.eclipse.slm.notification_service.model.JobGoal;
import org.eclipse.slm.notification_service.model.JobTarget;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.eclipse.slm.resource_management.model.capabilities.*;
import org.eclipse.slm.resource_management.model.actions.AwxAction;
import org.eclipse.slm.resource_management.model.actions.ActionType;
import org.eclipse.slm.resource_management.model.consul.capability.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.persistence.api.CapabilityJpaRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.net.ssl.SSLException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        CapabilitiesManager.class,
        AwxJobObserverInitializer.class
})
@ActiveProfiles("test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class CapabilitiesManagerTest {
    public static final String keycloakDummyToken = "";
    @Autowired
    CapabilitiesManager capabilitiesManager;
    @MockBean
    CapabilitiesConsulClient capabilitiesConsulClient;
    @MockBean
    SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient;
    @MockBean
    MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient;
    @MockBean
    SingleHostCapabilitiesVaultClient singleHostCapabilitiesVaultClient;
    @MockBean
    AwxJobExecutor awxJobExecutor;
    @MockBean
    NotificationServiceClient notificationServiceClient;
    @MockBean
    AwxJobObserverInitializer awxJobObserverInitializer;
    @MockBean
    AwxClient awxClient;
    @MockBean
    CapabilityJpaRepository capabilityJpaRepository;
    @MockBean
    KeycloakUtil keycloakUtil;
    @MockBean
    CapabilityUtil capabilityUtil;
    @MockBean
    ObjectMapper objectMapper;

    @Nested
    @Order(10)
    @DisplayName("Pretests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class preTests {
        @Test
        @Order(10)
        public void testCapabilitiesManagerNotNull() {
            assertNotNull(capabilitiesManager);
        }
    }


    @Nested
    @Order(20)
    @DisplayName("Test Deployment Capability")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class testDeploymentCapabilities {
        private static final DeploymentCapability dockerDeploymentCapability = new DeploymentCapability();
        private static final UUID resourceUuid = UUID.randomUUID();

        @BeforeAll
        public static void beforeAll() {
            //region Create Java Object of Docker DeploymentCapability:
            dockerDeploymentCapability.setId(UUID.randomUUID());
            dockerDeploymentCapability.setName("Docker");
            dockerDeploymentCapability.setLogo("mdi-docker");
            dockerDeploymentCapability.setType(Arrays.asList(
                    CapabilityType.SETUP,
                    CapabilityType.DEPLOY
            ));
            dockerDeploymentCapability.setCapabilityClass("DeploymentCapability");

            dockerDeploymentCapability.setSupportedDeploymentTypes(Arrays.asList(
                    DeploymentType.DOCKER_CONTAINER,
                    DeploymentType.DOCKER_COMPOSE
            ));

            // Set AWX Capability Actions
            var dockerDeploymentCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-docker.git";
            var dockerDeploymentCapabilityBranch = "1.0.0";
            AwxAction installAwxCapabilityAction = new AwxAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "install.yml");
            installAwxCapabilityAction.setUsername("username");
            installAwxCapabilityAction.setPassword("password");

            dockerDeploymentCapability.getActions()
                    .put(ActionType.INSTALL, installAwxCapabilityAction);
            dockerDeploymentCapability.getActions()
                    .put(ActionType.UNINSTALL, new AwxAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "uninstall.yml"));
            dockerDeploymentCapability.getActions()
                    .put(ActionType.DEPLOY, new AwxAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "deploy.yml"));
            dockerDeploymentCapability.getActions()
                    .put(ActionType.UNDEPLOY, new AwxAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "undeploy.yml"));
            //endregion
        }

        @Test
        @Order(10)
        public void testGetCapabilitiesAndExpectEmptyList() {
            List<Capability> capabilities = capabilitiesManager.getCapabilities();

            assertEquals(0, capabilities.size());
        }

        @Test
        @Order(20)
        public void testAddDeploymentCapability() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
            capabilitiesManager.addCapability(dockerDeploymentCapability);
        }

        @Test
        @Order(30)
        public void testGetCapabilitiesAndExpectListWithOneDeploymentCapability() {
            Mockito.when(capabilityJpaRepository.findAll())
                    .thenReturn(Collections.singletonList(dockerDeploymentCapability));

            List<Capability> capabilities = capabilitiesManager.getCapabilities();

            assertEquals(1, capabilities.size());
        }

        @Order(40)
        public void testGetCapabilitiesAndFilterBySingleHostCapabilities() {
            var filter = new CapabilityFilter.Builder()
                    .capabilityHostType(CapabilityFilter.CapabilityHostType.SINGLE_HOST)
                    .build();

            Mockito.when(capabilityJpaRepository.findAll())
                    .thenReturn(Collections.singletonList(dockerDeploymentCapability));

            List<Capability> capabilities = capabilitiesManager.getCapabilities(Optional.of(filter));
            assertEquals(1, capabilities.size());

            if(filter.equals(CapabilityFilter.CapabilityHostType.MULTI_HOST))
                assertEquals(0, capabilities.size());
        }

        @Order(41)
        public void testGetCapabilitiesAndFilterByMultiHostCapabilities() {
            var filter = new CapabilityFilter.Builder()
                    .capabilityHostType(CapabilityFilter.CapabilityHostType.MULTI_HOST)
                    .build();

            Mockito.when(capabilityJpaRepository.findAll())
                    .thenReturn(Collections.singletonList(dockerDeploymentCapability));

            List<Capability> capabilities = capabilitiesManager.getCapabilities(Optional.of(filter));
            assertEquals(0, capabilities.size());
        }

        @Test
        @Order(50)
        public void testGetCapabilityByName() {
            Mockito.when(capabilityJpaRepository.findAll())
                    .thenReturn(Collections.singletonList(dockerDeploymentCapability));

            Optional<Capability> optionalCapability = capabilitiesManager.getCapabilityByName(
                    dockerDeploymentCapability.getName()
            );

            assertTrue(optionalCapability.isPresent());
        }

        @Test
        @Order(60)
        public void testGetCapabilityById() {
            Mockito.when(capabilityJpaRepository.findAll())
                    .thenReturn(Collections.singletonList(dockerDeploymentCapability));

            Optional<Capability> optionalCapability = capabilitiesManager.getCapabilityById(
                    dockerDeploymentCapability.getId()
            );

            assertTrue(optionalCapability.isPresent());
        }

        @Test
        @Order(70)
        public void testDeleteCapability() throws ConsulLoginFailedException {
            Mockito.when(capabilityJpaRepository.findById(dockerDeploymentCapability.getId()))
                    .thenReturn(Optional.of(dockerDeploymentCapability));

            assertTrue(
                    capabilitiesManager.deleteCapability(dockerDeploymentCapability.getId())
            );
        }

        @Test
        @Order(80)
        public void testInstallOfCapabilityWithoutHealthcheck() throws CapabilityNotFoundException, SSLException, ConsulLoginFailedException, ResourceNotFoundException, JsonProcessingException, IllegalAccessException {
            ConsulCredential consulCredential = new ConsulCredential();
            AwxCredential awxCredential = new AwxCredential("username", "password");
            SingleHostCapabilityService newSingleHostCapabilityService = new SingleHostCapabilityService(
                    dockerDeploymentCapability,
                    resourceUuid
            );

            //region MOCK:
            Mockito
                    .when(capabilityJpaRepository.findAll())
                    .thenReturn(Collections.singletonList(dockerDeploymentCapability));
            AwxJobObserver awxJobObserver = new AwxJobObserver(
                    0,
                    JobTarget.RESOURCE,
                    JobGoal.ADD,
                    capabilitiesManager
            );

            Mockito.when(awxJobObserverInitializer.init(
                    Mockito.anyInt(),
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any()
                    )).thenReturn(awxJobObserver);

            Mockito.when(singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any()
                    )).thenReturn(newSingleHostCapabilityService);

            Mockito.when(singleHostCapabilitiesConsulClient.getCapabilityServiceForCapabilityOfResource(
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any()
            )).thenReturn(newSingleHostCapabilityService);
            //endregion

            capabilitiesManager.installCapabilityOnResource(
                    resourceUuid,
                    newSingleHostCapabilityService,
                    awxCredential,
                    consulCredential,
                    keycloakDummyToken,
                    new HashMap<>()
            );

            capabilitiesManager.onJobStateFinished(awxJobObserver, JobFinalState.SUCCESSFUL);
        }

        @Test
        @Order(90)
        public void testUninstallOfCapabilityWithoutHealthcheck() throws SSLException, ConsulLoginFailedException, ResourceNotFoundException {
            SingleHostCapabilityService newSingleHostCapabilityService = new SingleHostCapabilityService(
                    dockerDeploymentCapability,
                    resourceUuid
            );
            capabilitiesManager.uninstallCapabilityFromResource(
                    resourceUuid,
                    newSingleHostCapabilityService,
                    new AwxCredential("username", "password"),
                    new ConsulCredential(),
                    keycloakDummyToken
            );
        }
    }

    @Nested
    @Order(30)
    @DisplayName("Test Virtualization Capability")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class testVirtualizationCapabilities {
        private static final VirtualizationCapability virtualizationCapability = new VirtualizationCapability();
        private static final UUID resourceUuid = UUID.randomUUID();

        @MockBean
        JobTemplate jobTemplate;

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
        List<SurveyItem> surveyItems = Arrays.asList(
            surveyItemText,
            surveyItemPassword,
            surveyItemInteger,
            surveyItemMultiplechoice,
            surveyItemMultiselect
        );
        //endregion

        @BeforeAll
        public static void beforeAll() {
            //region Create Java Object of Docker DeploymentCapability:
            virtualizationCapability.setName("Dummy");
            virtualizationCapability.setLogo("mdi-dummy");
            virtualizationCapability.setType(Arrays.asList(
                    CapabilityType.SETUP,
                    CapabilityType.VM
            ));

            virtualizationCapability.setCapabilityClass("VirtualizationCapability");

            // Set AWX Capability Actions
            var virtualizationCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-dummy";
            var virtualizationCapabilityBranch = "main";
            virtualizationCapability.getActions()
                    .put(ActionType.INSTALL, new AwxAction(virtualizationCapabilityRepo, virtualizationCapabilityBranch, "install.yml"));
            virtualizationCapability.getActions()
                    .put(ActionType.UNINSTALL, new AwxAction(virtualizationCapabilityRepo, virtualizationCapabilityBranch, "uninstall.yml"));
            virtualizationCapability.getActions()
                    .put(ActionType.CREATE_VM, new AwxAction(virtualizationCapabilityRepo, virtualizationCapabilityBranch, "create_vm.yml"));
            virtualizationCapability.getActions()
                    .put(ActionType.DELETE_VM, new AwxAction(virtualizationCapabilityRepo, virtualizationCapabilityBranch, "delete_vm.yml"));
            //endregion
        }

        @Test
        @Order(10)
        public void testGetCapabilitiesAndExpectEmptyList() {
            List<Capability> capabilities = capabilitiesManager.getCapabilities();

            assertEquals(0, capabilities.size());
        }

        @Test
        @Order(20)
        public void testAddVirtualizationCapability() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
            capabilitiesManager.addCapability(virtualizationCapability);
        }

        @Test
        @Order(30)
        public void testGetCapabilitiesAndExpectListWithOneDeploymentCapability() {
            Mockito.when(capabilityJpaRepository.findAll())
                    .thenReturn(Collections.singletonList(virtualizationCapability));

            List<Capability> capabilities = capabilitiesManager.getCapabilities();

            assertEquals(1, capabilities.size());
        }

        @Order(40)
        public void testGetCapabilitiesAndFilterBySingleHostCapabilities() {
            var filter = new CapabilityFilter.Builder()
                    .capabilityHostType(CapabilityFilter.CapabilityHostType.SINGLE_HOST).build();

            Mockito.when(capabilityJpaRepository.findAll())
                    .thenReturn(Collections.singletonList(virtualizationCapability));

            List<Capability> capabilities = capabilitiesManager.getCapabilities(Optional.of(filter));
            assertEquals(1, capabilities.size());

            if(filter.equals(CapabilityFilter.CapabilityHostType.MULTI_HOST))
                assertEquals(0, capabilities.size());
        }

        @Order(41)
        public void testGetCapabilitiesAndFilterByMultiHostCapabilities() {
            var filter = new CapabilityFilter.Builder()
                    .capabilityHostType(CapabilityFilter.CapabilityHostType.MULTI_HOST).build();

            Mockito.when(capabilityJpaRepository.findAll())
                    .thenReturn(Collections.singletonList(virtualizationCapability));

            List<Capability> capabilities = capabilitiesManager.getCapabilities(Optional.of(filter));
            assertEquals(0, capabilities.size());
        }

        @Test
        @Order(50)
        public void testGetCapabilityByName() {
            Mockito.when(capabilityJpaRepository.findAll())
                    .thenReturn(Collections.singletonList(virtualizationCapability));

            Optional<Capability> optionalCapability = capabilitiesManager.getCapabilityByName(
                    virtualizationCapability.getName()
            );

            assertTrue(optionalCapability.isPresent());
        }

        @Test
        @Order(60)
        public void testGetCapabilityById() {
            Mockito.when(capabilityJpaRepository.findAll())
                    .thenReturn(Collections.singletonList(virtualizationCapability));

            Optional<Capability> optionalCapability = capabilitiesManager.getCapabilityById(
                    virtualizationCapability.getId()
            );

            assertTrue(optionalCapability.isPresent());
        }

        @Test
        @Order(70)
        public void testDeleteCapability() throws ConsulLoginFailedException {
            Mockito.when(capabilityJpaRepository.findById(virtualizationCapability.getId()))
                    .thenReturn(Optional.of(virtualizationCapability));

            assertTrue(
                    capabilitiesManager.deleteCapability(virtualizationCapability.getId())
            );
        }

        @Test
        @Order(75)
        public void testAddCapabilityWithParams() throws ConsulLoginFailedException, ResourceNotFoundException, AwxProjectUpdateFailedException, JsonProcessingException, SSLException, IllegalAccessException {
            AwxAction action = (AwxAction) virtualizationCapability.getActions().get(ActionType.CREATE_VM);
            action.setParameter(surveyItems);

            Mockito.when(jobTemplate.getId())
                            .thenReturn(1);

            Mockito.when(awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(
                            Mockito.any(),
                            Mockito.any(),
                            Mockito.any(),
                            Mockito.any(),
                            Mockito.any()))
                    .thenReturn(jobTemplate);

            capabilitiesManager.addCapability(virtualizationCapability);
        }

        @Test
        @Order(80)
        public void testInstallOfCapabilityWithoutHealthcheck() throws CapabilityNotFoundException, SSLException, ConsulLoginFailedException, ResourceNotFoundException, JsonProcessingException, IllegalAccessException {
            SingleHostCapabilityService newSingleHostCapabilityService = new SingleHostCapabilityService(
                    virtualizationCapability,
                    resourceUuid
            );

            Mockito
                    .when(capabilityJpaRepository.findAll())
                    .thenReturn(Collections.singletonList(virtualizationCapability));
            AwxJobObserver awxJobObserver = new AwxJobObserver(
                    0,
                    JobTarget.RESOURCE,
                    JobGoal.ADD,
                    capabilitiesManager
            );

            Mockito.when(awxJobObserverInitializer.init(
                    Mockito.anyInt(),
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any()
            )).thenReturn(awxJobObserver);

            Mockito.when(singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any()
            )).thenReturn(newSingleHostCapabilityService);

            Mockito.when(singleHostCapabilitiesConsulClient.getCapabilityServiceForCapabilityOfResource(
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any()
            )).thenReturn(newSingleHostCapabilityService);

            capabilitiesManager.installCapabilityOnResource(
                    resourceUuid,
                    newSingleHostCapabilityService,
                    new AwxCredential("username", "password"),
                    new ConsulCredential(),
                    keycloakDummyToken,
                    new HashMap<>()
            );

            capabilitiesManager.onJobStateFinished(awxJobObserver, JobFinalState.SUCCESSFUL);
        }

        @Test
        @Order(90)
        public void testUninstallOfCapabilityWithoutHealthcheck() throws SSLException, ConsulLoginFailedException, ResourceNotFoundException {
            SingleHostCapabilityService newSingleHostCapabilityService = new SingleHostCapabilityService(
                    virtualizationCapability,
                    resourceUuid
            );

            capabilitiesManager.uninstallCapabilityFromResource(
                    resourceUuid,
                    newSingleHostCapabilityService,
                    new AwxCredential("username", "password"),
                    new ConsulCredential(),
                    keycloakDummyToken
            );
        }
    }
}
