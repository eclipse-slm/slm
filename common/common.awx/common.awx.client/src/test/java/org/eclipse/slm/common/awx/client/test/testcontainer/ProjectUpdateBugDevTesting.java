package org.eclipse.slm.common.awx.client.test.testcontainer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.client.AwxProjectUpdateFailedException;
import org.eclipse.slm.common.awx.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RestTemplate.class,
        AwxClient.class
})
@ContextConfiguration(initializers = {ConfigDataApplicationContextInitializer.class} )
@TestPropertySource(properties = { "spring.config.location=classpath:application.yml" })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectUpdateBugDevTesting {
    //region Variables
    public final static Logger LOG = LoggerFactory.getLogger(ProjectUpdateBugDevTesting.class);
    @Autowired
    private static AwxClient staticAwxClient;
    static final DockerComposeContainer awxContainer;
    private static int AWX_PORT = 8013;
    private static String AWX_WEB_SERVICE = "awx";

    @Autowired
    AwxClient awxClient;

    static {
        awxContainer = new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
                .withExposedService(AWX_WEB_SERVICE,AWX_PORT,
                        Wait.forHttp("/#/login").forPort(AWX_PORT).withStartupTimeout(Duration.ofMinutes(5))
                )
                .withLocalCompose(false);
        awxContainer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stopContainer()));
    }

    private static void stopContainer() {
        awxContainer.stop();
    }

    @Autowired
    public ProjectUpdateBugDevTesting(AwxClient awxClient) {
        staticAwxClient = awxClient;
    }

    public static ProjectDTOApiCreate projectDummyCreateAndWaitDTO = new ProjectDTOApiCreate(
            "https://github.com/FabOS-AI/fabos-slm-dc-dummy",
            "main"
    );

    public static ProjectDTOApiCreate projectDockerCreateAndWaitDTO = new ProjectDTOApiCreate(
            "https://github.com/FabOS-AI/fabos-slm-dc-docker",
            "1.0.0"
    );

    List<String> playbooks = Arrays.asList("install.yml", "uninstall.yml", "deploy.yml", "undeploy.yml");

    int retries = 50;
    //endregion

    private static Stream<ProjectDTOApiCreate> provideProjectCreateDTO() {
        return Stream.of(
            projectDummyCreateAndWaitDTO,
            projectDockerCreateAndWaitDTO
        );
    }

    @BeforeEach
    public void beforeEach() {
        awxClient.setAwxPort(awxContainer.getServicePort(AWX_WEB_SERVICE, AWX_PORT));
    }

    @AfterEach
    public void afterEach() {
        LOG.info("Cleanup: Remove all job templates and projects");

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

    @ParameterizedTest
    @MethodSource("provideProjectCreateDTO")
    @Disabled
    public void createAndDeleteProjectMultipleTimes(ProjectDTOApiCreate projectDTOApiCreate) throws AwxProjectUpdateFailedException, SSLException, JsonProcessingException, InterruptedException {
        for(int i = 1; i < retries+1; i++) {
            LOG.info("Do try #" + i + ":");
            Project createdProject = awxClient.createProject(projectDTOApiCreate);

            int _try = 0;
            int rtrs = 30;
            ProjectUpdate projectUpdate = null;

            while(_try < rtrs) {
                projectUpdate = awxClient.getProjectUpdates(createdProject.getId());
                String projectUpdateState = projectUpdate.getStatus();
                LOG.info("Current Project update state: " + projectUpdateState);

                if(projectUpdate.getStatus().equals("successful")) {
                    break;
                } else {
                    _try++;
                    Thread.sleep(1000);
                }
            }
            assertEquals("successful", projectUpdate.getStatus());

            awxClient.deleteProject(createdProject.getId());

            LOG.info("########################################");
        }

    }

    @ParameterizedTest
    @Disabled
    @MethodSource("provideProjectCreateDTO")
    public void createAndDeleteProjectAndTemplatesMultipleTimes(ProjectDTOApiCreate projectDTOApiCreate) throws AwxProjectUpdateFailedException, SSLException, JsonProcessingException {
        for(int i = 1; i < retries+1; i++) {
            LOG.info("Do try #" + i + ":");

            for(String playbook : playbooks) {
                awxClient.createJobTemplate(
                        projectDTOApiCreate.getScm_url(),
                        projectDTOApiCreate.getScm_branch(),
                        playbook,
                        new ArrayList<>()
                );
            }

            //region Cleanup JobTemplates
            Results<JobTemplate> jobTemplateQueryResult = awxClient.getJobTemplates();

            jobTemplateQueryResult.getResults().forEach(
                    jt -> {
                        try {
                            awxClient.deleteJobTemplate(jt.getId());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            //endregion

            //region Cleanup Projects
            Results<Project> projectQueryResult = awxClient.getProjects();

            projectQueryResult.getResults().forEach(
                    p -> {
                        try {
                            awxClient.deleteProject(p.getId());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            //endregion

            LOG.info("########################################");
        }
    }

    @ParameterizedTest
    @Disabled
    @MethodSource("provideProjectCreateDTO")
    public void createAndUpdateProjectMultipleTimes(ProjectDTOApiCreate projectDTOApiCreate) throws AwxProjectUpdateFailedException, SSLException, JsonProcessingException, InterruptedException {
        LOG.info("Create Project");
        Project createdProject = awxClient.createProjectAndWait(projectDTOApiCreate);

        for(int i = 1; i < retries+1; i++) {
            LOG.info("Do update #" + i + ":");
            awxClient.updateProject(createdProject.getId());

            int _try = 0;
            int rtrs = 30;
            ProjectUpdate projectUpdate = null;

            while(_try < rtrs) {
                projectUpdate = awxClient.getProjectUpdates(createdProject.getId());
                String projectUpdateState = projectUpdate.getStatus();
                LOG.info("Current Project update state: " + projectUpdateState);

                if(projectUpdate.getStatus().equals("successful")) {
                    break;
                } else {
                    _try++;
                    Thread.sleep(1000);
                }
            }

            assertEquals("successful", projectUpdate.getStatus());

            LOG.info("########################################");
        }
    }
}
