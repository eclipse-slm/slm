package org.eclipse.slm.common.awx.client.test.testcontainer;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.websocket.DeploymentException;
import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.client.AwxProjectUpdateFailedException;
import org.eclipse.slm.common.awx.client.observer.*;
import org.eclipse.slm.common.awx.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RestTemplate.class,
        AwxClient.class
})
@ContextConfiguration(initializers = {ConfigDataApplicationContextInitializer.class} )
@TestPropertySource(properties = { "spring.config.location=classpath:application.yml" })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BasicAwxClientDevTesting {

    //region Variables
    public final static Logger LOG = LoggerFactory.getLogger(BasicAwxClientDevTesting.class);
    static final DockerComposeContainer awxContainer;

    private static int AWX_PORT = 8013;
    private static int AWX_HTTPS_PORT = 8043;
    private static String AWX_WEB_SERVICE = "awx";

    @Autowired
    AwxClient awxClient;

    static {
        awxContainer = new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
                .withExposedService(AWX_WEB_SERVICE, AWX_HTTPS_PORT)
                .withExposedService(AWX_WEB_SERVICE, AWX_PORT,
                        Wait.forHttp("/#/login").forPort(AWX_PORT).withStartupTimeout(Duration.ofMinutes(5))
                )
                .withLocalCompose(true);
        awxContainer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stopContainer()));
    }

    private static void stopContainer() {
        awxContainer.stop();
    }
    //endregion

    @BeforeEach
    public void beforeEach() {
        awxClient.setAwxPort(awxContainer.getServicePort(AWX_WEB_SERVICE, AWX_PORT));
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class testAwxCredentialMethods {
        private static AwxClient staticAwxClient;
        CredentialType credentialType;
        Organization defaultOrga;
        CredentialDTOApiCreate credentialDtoApiCreate1;
        public static int credentialId1;
        CredentialDTOApiCreate credentialDtoApiCreate2;
        public static int credentialId2;

        CredentialDTOApiUpdate credentialDtoApiUpdate;

        @Autowired
        public testAwxCredentialMethods(AwxClient awxClient) {
            staticAwxClient = awxClient;
        }

        @BeforeEach
        public void init() throws JsonProcessingException {
            credentialType = awxClient.getCredentialTypeByName(CredentialTypeName.SOURCE_CONTROL);
            defaultOrga = awxClient.getDefaultOrganisation();
            credentialDtoApiCreate1 = new CredentialDTOApiCreate(
                "test-credential",
                "test-credential-description",
                defaultOrga.getId(),
                credentialType.getId(),
                new HashMap<String, String>() {{
                    put("username", "username");
                    put("password", "password");
                }}
            );

            credentialDtoApiCreate2 = new CredentialDTOApiCreate(
                    "test-credential",
                    "test-credential-description",
                    defaultOrga.getId(),
                    credentialType.getId(),
                    new HashMap<String, String>() {{
                        put("username", "user");
                        put("password", "password");
                    }}

            );

            credentialDtoApiUpdate = new CredentialDTOApiUpdate(
                    credentialDtoApiCreate1.getName(),
                    null,
                    credentialDtoApiCreate1.getOrganization(),
                    credentialDtoApiCreate1.getCredential_type(),
                    new HashMap<String, String>() {{
                        put("username", "testtesttest");
                        put("password", "password");
                    }}
            );
        }

        @Test
        @Order(10)
        public void getCredentials() {
            Results<Credential> credentials = awxClient.getCredentials();

            assertEquals(2, credentials.getCount());
        }
        @Test
        @Order(20)
        public void createCredential() throws JsonProcessingException {
            Results<Credential> credentialsBefore = awxClient.getCredentials();

            Credential credential = awxClient.createCredential(credentialDtoApiCreate1);

            Results<Credential> credentialsAfter = awxClient.getCredentials();

            assertEquals(credentialsBefore.getCount()+1, credentialsAfter.getCount());

            assertEquals(credentialDtoApiCreate1.getName(), credential.getName());
            assertEquals(credentialDtoApiCreate1.getDescription(), credential.getDescription());
            assertEquals(credentialDtoApiCreate1.getOrganization(), credential.getOrganization());
            assertEquals(credentialDtoApiCreate1.getCredential_type(), credential.getCredential_type());
            assertEquals(credentialDtoApiCreate1.getInputs().get("username"), credential.getInputs().get("username"));

            credentialId1 = credential.getId();
        }

        @Test
        @Order(30)
        public void deleteCredential() {
            Results<Credential> credentialsBeforeDelete = awxClient.getCredentials();
            awxClient.deleteCredential(credentialId1);
            Results<Credential> credentialsAfterDelete = awxClient.getCredentials();

            assertEquals(credentialsBeforeDelete.getCount()-1, credentialsAfterDelete.getCount());
        }

        @Test
        @Order(40)
        public void createCredentialTwice() throws JsonProcessingException {
            Credential credential1 = awxClient.createCredential(credentialDtoApiCreate1);
            Credential credential2 = awxClient.createCredential(credentialDtoApiCreate2);

            credentialId1 = credential1.getId();
            credentialId2 = credential2.getId();

            assertNotEquals(
                    credential1.getInputs().get("username"),
                    credential2.getInputs().get("username")
            );
        }

        @Test
        @Order(50)
        public void getCredentialByName() throws JsonProcessingException {
            Credential credentialFromLookup = awxClient.getCredentialByName(credentialDtoApiCreate2.getName());

            assertEquals(credentialDtoApiCreate2.getName(), credentialFromLookup.getName());
            assertEquals(credentialDtoApiCreate2.getOrganization(), credentialFromLookup.getOrganization());
            assertEquals(credentialDtoApiCreate2.getCredential_type(), credentialFromLookup.getCredential_type());
            assertEquals(credentialDtoApiCreate2.getInputs().get("username"), credentialFromLookup.getInputs().get("username"));
        }

        @Test
        @Order(60)
        public void updateCredential() throws JsonProcessingException {
            Credential credentialUpdateReturn = awxClient.updateCredential(
                    credentialId1,
                    credentialDtoApiUpdate
            );

            // Assert username of credential return 1 and 2 should not match
            assertNotEquals(
                    credentialDtoApiCreate1.getInputs().get("username"),
                    credentialUpdateReturn.getInputs().get("username")
            );
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class testAwxCredentialTypeMethods {
        CredentialTypeDTOApiCreate credentialTypeDTOApiCreate;

        @BeforeEach
        public void init() {
            CredentialTypeInputFieldItem textField = new CredentialTypeInputFieldItem(
                    "text_field_id",
                    "Text Field",
                    false,
                    "string"
            );

            CredentialTypeInputFieldItem secretField = new CredentialTypeInputFieldItem(
                    "secret_field_id",
                    "Secret Field",
                    true,
                    "string"
            );

            List<CredentialTypeInputFieldItem> credentialTypeInputFieldItemList = Arrays.asList(textField, secretField);

            HashMap<String, List<CredentialTypeInputFieldItem>> inputsMap = new HashMap<>();
            inputsMap.put("fields", credentialTypeInputFieldItemList);

            HashMap<String, String> envMap = new HashMap<>();
            envMap.put("TEXT_FIELD", "{{text_field_id}}");
            envMap.put("SECRET_FIELD", "{{secret_field_id}}");

            HashMap<String, Map<String, String>> injectorsMap = new HashMap<>();
            injectorsMap.put("env", envMap);

            credentialTypeDTOApiCreate = new CredentialTypeDTOApiCreate(
                    "Test Credential Type",
                    "Test Credential Type Description",
                    "cloud",
                    inputsMap,
                    injectorsMap
            );
        }
        @Test
        @Order(10)
        public void getCredentialTypes() {
            Results<CredentialType> credentialTypes = awxClient.getCredentialTypes();

            // AWX brings 28 default credential Types:
            assertEquals(28, credentialTypes.getCount());
        }

        @Test
        @Order(20)
        public void getCredentialTypeByName() {
            CredentialTypeName name = CredentialTypeName.SOURCE_CONTROL;

            CredentialType credentialType = awxClient.getCredentialTypeByName(
                    name
            );

            assertEquals(name.getPrettyName(), credentialType.getName());
        }

        @Test
        @Order(30)
        public void createCustomCredentialType() {
            awxClient.createCredentialType(credentialTypeDTOApiCreate);
            CredentialType createdCredentialType = awxClient.getCustomCredentialTypeByName(credentialTypeDTOApiCreate.getName());

            assertEquals(credentialTypeDTOApiCreate.getName(), createdCredentialType.getName());
            assertEquals(credentialTypeDTOApiCreate.getDescription(), createdCredentialType.getDescription());
        }

        @Test
        @Order(40)
        public void createCustomCredentialTypeSecondTime() {
            CredentialType returnedSecondAttemptCredentialType = awxClient.createCredentialType(credentialTypeDTOApiCreate);

            assertEquals(credentialTypeDTOApiCreate.getName(), returnedSecondAttemptCredentialType.getName());
            assertEquals(credentialTypeDTOApiCreate.getDescription(), returnedSecondAttemptCredentialType.getDescription());
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class testAwxOrganizationAndInventoryMethods {
        OrganizationCreateRequest organizationCreateRequest;
        public static int organizationId = 0;

        @BeforeEach
        public void init() {
            organizationCreateRequest = new OrganizationCreateRequest(
                    "Test Orga"
            );
        }

        @Test
        @Order(10)
        public void createOrganization() throws JsonProcessingException {
            Organization organization = awxClient.createOrganization(organizationCreateRequest);

            assertEquals(organizationCreateRequest.getName(), organization.getName());
            organizationId = organization.getId();
        }

        @Test
        @Order(20)
        public void getOrganizations() throws JsonProcessingException {
            Results<Organization> organizationsResult = awxClient.getOrganizations();

            /**
             * Orgas to expect:
             * - Default
             * - Self Service Portal ??
             * - Service Lifecycle Management
             * - Test Orga
             */
            assertEquals(3, organizationsResult.getCount());
        }

        @Test
        @Order(30)
        public void getOrganization() {
            Organization organization = awxClient.getOrganizationById(organizationId);
            assertEquals(
                    organizationCreateRequest.getName(),
                    organization.getName()
            );
        }

        @Test
        @Order(40)
        public void getDefaultOrganization() throws JsonProcessingException {
            Organization organization = awxClient.getDefaultOrganisation();

            assertEquals("Service Lifecycle Management", organization.getName());
        }

        @Test
        @Order(50)
        public void getOrganizationByName() throws JsonProcessingException {
            String name = organizationCreateRequest.getName();

            Results<Organization> getOrganizationResult =
                    awxClient.getOrganizationByName(name);

            assertEquals(1, getOrganizationResult.getCount() );
            assertEquals(
                    name,
                    getOrganizationResult.getResults().stream().collect(Collectors.toList()).get(0).getName()
            );
        }

        @Nested
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        public class testAwxInventoryMethods {
            String inventoryName = "test-inventory";
            public static Results<Inventory> initialInventoryQuery = null;
            public static int inventoryId;

            @Test
            @Order(10)
            public void getInventories() throws JsonProcessingException {
                Results<Inventory> inventoryQueryResult = awxClient.getInventories();

                initialInventoryQuery = inventoryQueryResult;
            }
            @Test
            @Order(20)
            public void createInventory() throws JsonProcessingException {
                Inventory inventory = awxClient.createInventory(
                        awxClient.getDefaultOrganisation().getId(),
                        inventoryName
                );

                assertEquals(inventoryName, inventory.getName());
                assertEquals(
                        initialInventoryQuery.getCount()+1,
                        awxClient.getInventories().getCount()
                );
            }

            @Test
            @Order(30)
            public void getInventoryByName() throws JsonProcessingException {
                Results<Inventory> inventoryQueryResult = awxClient.getInventoryByName(inventoryName);

                assertEquals(1, inventoryQueryResult.getCount());
            }

            @Test
            @Order(40)
            public void createInventoryWhenInventoryExistsAlready() throws JsonProcessingException {
                Results<Inventory> inventoriesBefore = awxClient.getInventories();

                Inventory inventory = awxClient.createInventory(
                        awxClient.getDefaultOrganisation().getId(),
                        inventoryName
                );

                assertEquals(inventoriesBefore.getCount(), awxClient.getInventories().getCount());
                assertEquals(inventoryName, inventory.getName());

                inventoryId = inventory.getId();
            }

            @Test
            @Order(50)
            public void getInventoryById() throws JsonProcessingException {
                Inventory inventory = awxClient.getInventory(inventoryId);

                assertEquals(inventoryName, inventory.getName());
            }

            @Test
            @Order(60)
            public void deleteInventory() throws JsonProcessingException, InterruptedException {
                awxClient.deleteInventory(inventoryId);

                /**
                 * Not doing assert because it takes quite a while to finsih delete. API response with 202
                 */
                return;
            }
        }

        @Nested
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        public class testAwxTeamMethods {
            Organization defaultOrganization;
            TeamCreateRequest teamCreateRequest;
            public static int teamId;

            @BeforeEach
            public void init() throws JsonProcessingException {
                defaultOrganization = awxClient.getDefaultOrganisation();
                teamCreateRequest = new TeamCreateRequest(
                        "test",
                        "test-team",
                        defaultOrganization.getId()
                );
            }

            @Test
            @Order(10)
            public void getTeams() {
                LOG.info(awxClient.getAwxUrl());

                Results<Team> teamResults = awxClient.getTeams();

                assertEquals(0, teamResults.getCount());
            }
            @Test
            @Order(20)
            public void createTeam() {
                Results<Team> teamsBefore = awxClient.getTeams();
                Team team = awxClient.createTeam(teamCreateRequest);
                Results<Team> teamsAfter = awxClient.getTeams();

                assertEquals(teamsBefore.getCount()+1, teamsAfter.getCount());
                teamId = team.getId();
            }

            @Test
            @Order(30)
            public void deleteTeam() {
                Results<Team> teamsBefore = awxClient.getTeams();
                awxClient.deleteTeam(teamId);
                Results<Team> teamsAfter = awxClient.getTeams();

                assertEquals(
                        teamsBefore.getCount()-1,
                        teamsAfter.getCount()
                );
            }
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class testAwxProjectAndProjectUpdateAndJobTemplateAndSurvey {
        private static AwxClient staticAwxClient;
        ProjectDTOApiCreate projectCreateDTO;
        ProjectDTOApiCreate projectCreateAndWaitDTO;
        public static int projectId;

        public static int projectUpdateRunId;

        @Autowired
        public testAwxProjectAndProjectUpdateAndJobTemplateAndSurvey(AwxClient awxClient) {
            staticAwxClient = awxClient;
        }
        @BeforeEach
        public void init() {
            projectCreateDTO = new ProjectDTOApiCreate(
                    "https://github.com/FabOS-AI/fabos-slm-dc-docker",
                    "1.0.0"
            );

            projectCreateAndWaitDTO = new ProjectDTOApiCreate(
                    "https://github.com/FabOS-AI/fabos-slm-dc-dummy",
                    "main"
            );
        }

        @AfterAll
        public static void cleanup() {
            Results<Project> projectQueryResult = staticAwxClient.getProjects();

            projectQueryResult.getResults().forEach(
                    p -> {
                        try {
                            staticAwxClient.deleteProject(p.getId());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

        @Test
        @Order(10)
        public void createAwxProject() throws JsonProcessingException {
            cleanup();
            Project projectCreated = awxClient.createProject(projectCreateDTO);

            assertEquals(projectCreateDTO.getName(),           projectCreated.getName());
            assertEquals(projectCreateDTO.getDescription(),    projectCreated.getDescription());
            assertEquals(projectCreateDTO.getScm_url(),        projectCreated.getScm_url());
            assertEquals(projectCreateDTO.getScm_branch(),     projectCreated.getScm_branch());

            projectId = projectCreated.getId();
            return;
        }

        @Test
        @Order(20)
        public void getAwxProjects() {
            Results<Project> projectResult = awxClient.getProjects();

            assertEquals(1, projectResult.getCount());
        }

        @Test
        @Order(30)
        public void getAwxProject() {
            Project project = awxClient.getProjectFromId(projectId);

            assertEquals(projectId, project.getId());
        }

        @Test
        @Order(40)
        public void deleteAwxProject() throws InterruptedException {
            Results<Project> projectsBeforeDelete = awxClient.getProjects();
            awxClient.deleteProject(projectId);

            for(int i = 0; i < 30; i++) {
                Results<Project> projectResult = awxClient.getProjects();

                if(projectResult.getCount() == 0) {
                    break;
                } else {
                    Thread.sleep(1000);
                }
            }

            Results<Project> projectsAfterDelete = awxClient.getProjects();

            assertEquals(projectsBeforeDelete.getCount()-1, projectsAfterDelete.getCount());
        }

        @Test
        @Order(50)
        public void createAwxProjectAndWait() throws SSLException, AwxProjectUpdateFailedException, JsonProcessingException {
            Project projectCreated = awxClient.createProjectAndWait(projectCreateAndWaitDTO);

            assertEquals(projectCreateAndWaitDTO.getName(),           projectCreated.getName());
            assertEquals(projectCreateAndWaitDTO.getDescription(),    projectCreated.getDescription());
            assertEquals(projectCreateAndWaitDTO.getScm_url(),        projectCreated.getScm_url());
            assertEquals(projectCreateAndWaitDTO.getScm_branch(),     projectCreated.getScm_branch());

            projectId = projectCreated.getId();
        }

        @Test
        @Order(60)
        public void getProjectUpdates() {
            ProjectUpdate projectUpdateReturn = awxClient.getProjectUpdates(projectId);

            assertEquals(projectId, projectUpdateReturn.getId());
            return;
        }
        @Test
        @Order(70)
        public void triggerProjectUpdate() {
            ProjectUpdate projectUpdateBefore = awxClient.getProjectUpdates(projectId);

            ProjectUpdateRun projectUpdateRun = awxClient.updateProject(projectId);

            ProjectUpdate projectUpdateAfter = awxClient.getProjectUpdates(projectId);

            assertNotEquals(
                    projectUpdateBefore.getStatus(),
                    projectUpdateAfter.getStatus()
            );

            projectUpdateRunId = projectUpdateRun.getId();
            return;
        }

        @Test
        @Order(80)
        public void cancelProjectUpdate() throws InterruptedException {
            ProjectUpdate projectUpdateBefore = awxClient.getProjectUpdates(projectId);

            assertThat(
                    projectUpdateBefore.getStatus(),
                    anyOf(
                            is("running"),
                            is("waiting")
                    )
            );

            awxClient.cancelProjectUpdate(projectUpdateRunId);

            int _try = 0;
            int retries = 30;
            ProjectUpdate projectUpdateAfter = null;

            while(_try < retries) {
                projectUpdateAfter = awxClient.getProjectUpdates(projectId);

                if(projectUpdateAfter.getStatus().equals("canceled")) {
                    break;
                } else {
                    _try++;
                    Thread.sleep(1000);
                }
            }

            assertNotEquals(retries, _try);
            assertEquals("canceled", projectUpdateAfter.getStatus());
        }

        @Nested
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        public class testAwxJobTemplatesMethods {
            //region Variables
            String playbook = "install.yml";
            public static int jobTemplateId;
            //endregion

            @AfterAll
            @BeforeAll
            public static void cleanup() {
                Results<JobTemplate> jobTemplateQueryResult = staticAwxClient.getJobTemplates();

                jobTemplateQueryResult.getResults().forEach(
                        jt -> {
                            try {
                                staticAwxClient.deleteJobTemplate(jt.getId());
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
            }
            @Test
            @Order(10)
            public void createJobTemplate() throws JsonProcessingException {
                JobTemplate jobTemplateCreated = awxClient.createJobTemplate(
                        projectId,
                        playbook,
                        new ArrayList<>()
                );

                assertEquals(playbook, jobTemplateCreated.getPlaybook());
                assertEquals(projectId, jobTemplateCreated.getProject());

                jobTemplateId = jobTemplateCreated.getId();
            }

            @Test
            @Order(20)
            public void getAwxJobTemplates() {
                Results<JobTemplate> jobTemplateQueryResult = awxClient.getJobTemplates();

                assertEquals(1, jobTemplateQueryResult.getCount());
            }

            @Test
            @Order(25)
            public void getAwxJobTemplateById() {
                Results<JobTemplate> jobTemplateQueryResult = awxClient.getJobTemplates();
                JobTemplate jobTemplate = jobTemplateQueryResult.getResults().stream().findFirst().get();

                JobTemplate jobTemplateById = awxClient.getJobTemplateById(jobTemplateId);

                assertTrue(
                        new ReflectionEquals(jobTemplate, "related", "summary_fields")
                                .matches(jobTemplateById)
                );
            }

            @Test
            @Order(30)
            public void getAwxJobTemplateByProjectAndPlaybook() {
                Results<JobTemplate> jobTemplateQueryResult = awxClient.getJobTemplateFromProjectIdAndPlaybook(projectId, playbook);

                JobTemplate jobTemplate = jobTemplateQueryResult.getResults().stream().collect(Collectors.toList()).get(0);
                assertEquals(1, jobTemplateQueryResult.getCount());
                assertEquals(playbook, jobTemplate.getPlaybook());
                assertEquals(projectId, jobTemplate.getProject());
            }

            @Test
            @Order(40)
            public void runAwxJobTemplate() throws JsonProcessingException, InterruptedException {
                //Make sure default Inventory is available:
                awxClient.createDefaultInventory();

                int jobRunId = awxClient.runJobTemplate(
                        new AwxCredential("admin", "password"),
                        projectCreateAndWaitDTO.getScm_url(),
                        projectCreateAndWaitDTO.getScm_branch(),
                        playbook,
                        new ExtraVars(new HashMap<>())
                );

                Job job = null;
                for(int i = 0; i < 30; i++) {
                    job = awxClient.getJob(jobRunId);

                    if(job.getStatus().equals(JobState.SUCCESSFUL.name().toLowerCase())) {
                        break;
                    } else {
                        Thread.sleep(1000);
                    }
                }

                assertEquals(
                        JobState.SUCCESSFUL.name().toLowerCase(),
                        job.getStatus()
                );
            }

            @Test
            @Order(42)
            public void runAwxJobTemplateAndObserveTheJobStatus() throws IOException, InterruptedException, DeploymentException {
                //Make sure default Inventory is available:
                String port = String.valueOf(awxContainer.getServicePort(AWX_WEB_SERVICE, AWX_HTTPS_PORT));
                awxClient.createDefaultInventory();
                AwxWebsocketClient awxWebsocketClient;
                awxWebsocketClient = new AwxWebsocketClient(awxClient.awxHost, awxClient.awxPort, "admin", "password");

                try {
                    awxWebsocketClient.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                class AwxJobObserverListener implements IAwxJobObserverListener{
                    public boolean stateChanged = false;
                    public boolean stateFinished = false;

                    @Override
                    public void onJobStateChanged(AwxJobObserver sender, JobState newState) {
                        stateChanged = true;
                    }

                    @Override
                    public void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState) {
                        stateFinished = true;
                    }
                }

                var listener = new AwxJobObserverListener();
                var observer = new AwxJobObserver(listener);
                awxWebsocketClient.registerObserver(observer);
                int jobRunId = awxClient.runJobTemplate(
                        new AwxCredential("admin", "password"),
                        projectCreateAndWaitDTO.getScm_url(),
                        projectCreateAndWaitDTO.getScm_branch(),
                        playbook,
                        new ExtraVars(new HashMap<>())
                );
                observer.observeJob(jobRunId, JobTarget.PROJECT, JobGoal.CREATE);

                Job job = null;
                for(int i = 0; i < 30; i++) {
                    job = awxClient.getJob(jobRunId);

                    if(job.getStatus().equals(JobState.SUCCESSFUL.name().toLowerCase())) {
                        break;
                    } else {
                        Thread.sleep(1000);
                    }
                }

                assertTrue(listener.stateChanged);
                assertTrue(listener.stateFinished);

                assertEquals(
                        JobState.SUCCESSFUL.name().toLowerCase(),
                        job.getStatus()
                );
            }

            @Test
            @Order(45)
            void updateJobTemplate() {
                String newJobTemplateName = "new job template name";

                Results<JobTemplate> jobTemplateQueryResult = awxClient.getJobTemplates();
                JobTemplate jobTemplate = jobTemplateQueryResult.getResults().stream().findFirst().get();

                jobTemplate.setName(newJobTemplateName);

                awxClient.updateJobTemplate(jobTemplate);

                JobTemplate updatedJobTemplate = awxClient.getJobTemplateById(jobTemplate.getId());

                assertEquals(
                        newJobTemplateName,
                        updatedJobTemplate.getName()
                );
            }

            @Test
            @Order(50)
            public void deleteAwxJobTemplate() throws AwxProjectUpdateFailedException, SSLException, JsonProcessingException, InterruptedException {
                Results<JobTemplate> jobTemplatesBeforeDelete = awxClient.getJobTemplates();
                awxClient.deleteJobTemplate(jobTemplateId);
                Results<JobTemplate> jobTemplatesAfterDelete = awxClient.getJobTemplates();
                assertEquals(jobTemplatesBeforeDelete.getCount()-1, jobTemplatesAfterDelete.getCount());
            }

            @Nested
            @Order(60)
            @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
            public class testAwxSurveyMethods {
                private static Survey survey = null;
                private static int jobTemplateIdForSurvey = -1;

                @BeforeEach()
                private void beforeEach() throws JsonProcessingException {
                    if(jobTemplateIdForSurvey == -1) {
                        JobTemplate jobTemplateCreated = awxClient.createJobTemplate(
                                projectId,
                                playbook,
                                new ArrayList<>()
                        );

                        jobTemplateIdForSurvey = jobTemplateCreated.getId();
                    }

                    if(survey == null) {
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

                        survey = new Survey(
                                "Test-Survey",
                                "Test-Survey-Description",
                                Arrays.asList(
                                        surveyItemText,
                                        surveyItemPassword,
                                        surveyItemInteger,
                                        surveyItemMultiplechoice,
                                        surveyItemMultiselect
                                )
                        );
                    }
                }

                @Test
                @Order(10)
                public void testGetSurveyIfNotExists() {
                    Optional<Survey> optionalSurvey = awxClient.getSurvey(jobTemplateIdForSurvey);

                    assertTrue(optionalSurvey.isEmpty());
                }

                @Test
                @Order(20)
                public void testCreateSurvey() throws JsonProcessingException {
                    awxClient.createSurvey(jobTemplateIdForSurvey, survey);

                    Optional<Survey> optionalSurvey = awxClient.getSurvey(jobTemplateIdForSurvey);
                    assertTrue(optionalSurvey.isPresent());

                    assertTrue(
                            new ReflectionEquals(survey).matches(optionalSurvey.get())
                    );
                }

                @Test
                @Order(30)
                void testEnableSurvey() {
                    awxClient.enableSurvey(jobTemplateIdForSurvey);

                    JobTemplate jobTemplate = awxClient.getJobTemplateById(jobTemplateIdForSurvey);

                    assertTrue(jobTemplate.getSurvey_enabled());
                }

                @Test
                @Order(40)
                public void testDeleteSurvey() {
                    Optional<Survey> optionalSurveyBefore = awxClient.getSurvey(jobTemplateIdForSurvey);
                    assertTrue(optionalSurveyBefore.isPresent());

                    awxClient.deleteSurvey(jobTemplateIdForSurvey);

                    Optional<Survey> optionalSurveyAfter = awxClient.getSurvey(jobTemplateIdForSurvey);
                    assertTrue(optionalSurveyAfter.isEmpty());
                }
            }
        }
    }
}
