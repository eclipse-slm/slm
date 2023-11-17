package org.eclipse.slm.common.awx.client.test.testcontainer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.client.AwxLoginCache;
import org.eclipse.slm.common.awx.client.AwxProjectUpdateFailedException;
import org.eclipse.slm.common.awx.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.net.ssl.SSLException;
import java.io.File;
import java.time.Duration;
import java.util.*;

import static org.junit.Assert.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RestTemplate.class,
        AwxClient.class,
        AwxLoginCache.class
})
@ContextConfiguration(initializers = {ConfigDataApplicationContextInitializer.class} )
@TestPropertySource(properties = { "spring.config.location=classpath:application.yml" })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AwxClientCapabilityTest {

    //region Variables
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
    //endregion

    private static void stopContainer() {
        awxContainer.stop();
    }

    @BeforeEach
    public void beforeEach() {
        awxClient.setAwxPort(awxContainer.getServicePort(AWX_WEB_SERVICE, AWX_PORT));
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class testCapabilityAwxMethods {
        //region Variables
        public final static Logger LOG = LoggerFactory.getLogger(testCapabilityAwxMethods.class);
        public static AwxClient staticAwxClient;
        List<String> jobTemplateCredentialNames = List.of("Consul", "HashiCorp Vault");
        String repo = "https://github.com/FabOS-AI/fabos-slm-dc-dummy";
        String branch = "main";
        String username = "username";
        String password = "password";
        String playbook = "install.yml";

        int credentialId = 0;
        //endregion

        @Autowired
        public testCapabilityAwxMethods(AwxClient awxClient) {
            staticAwxClient = awxClient;
        }
        @AfterEach
        private void afterEach() {
            //region cleanup job templates:
            LOG.info("Cleanup of job templates...");
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
            //endregion

            //region cleanup projects:
            LOG.info("Cleanup of projects...");
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
            //endregion

            //region cleanup of credentials:
            if(credentialId != 0) {
                LOG.info("Cleanup credentials...");
                awxClient.deleteCredential(credentialId);
                credentialId = 0;
            }
            //endregion
        }

        @Test
        @Order(10)
        public void createCapabilityWithoutScmCredential() throws AwxProjectUpdateFailedException, JsonProcessingException, SSLException {
            Results<Project> projectsBefore = awxClient.getProjects();
            Results<JobTemplate> jobTemplatesBefore = awxClient.getJobTemplates();

            awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(
                    repo,
                    branch,
                    playbook,
                    jobTemplateCredentialNames,
                    "");

            assertEquals(
                    projectsBefore.getCount()+1,
                    awxClient.getProjects().getCount()
            );

            assertEquals(
                    jobTemplatesBefore.getCount()+1,
                    awxClient.getJobTemplates().getCount()
            );
        }

        @Test
        @Order(20)
        public void createScmCredential() throws JsonProcessingException {
            Results<Credential> credentials = awxClient.getCredentials();

            Credential newCredential = awxClient.createSourceControlCredentialForDefaultOrga(
                    username,
                    password,
                    repo
            );
            credentialId = newCredential.getId();

            assertEquals(
                    credentials.getCount()+1,
                    awxClient.getCredentials().getCount()
            );

            Credential credentialLookup = awxClient.getCredentialByUrlAndUsername(repo, username);

            assertEquals(
                    username,
                    credentialLookup.getInputs().get("username")
            );
        }

        @Test
        @Order(30)
        public void createCapabilityWithScmCredential() throws AwxProjectUpdateFailedException, JsonProcessingException, SSLException {
            Results<Project> projectsBefore = awxClient.getProjects();
            Results<JobTemplate> jobTemplatesBefore = awxClient.getJobTemplates();
            Results<Credential> credentials = awxClient.getCredentials();

            awxClient.createJobTemplateAddExecuteRoleToDefaultTeamAddScmCredential(
                    repo,
                    branch,
                    playbook,
                    username,
                    password,
                    jobTemplateCredentialNames,
                    "");

            Credential scmCredential = awxClient.getCredentialByUrlAndUsername(repo, username);
            assertEquals(username, scmCredential.getInputs().get("username"));
            credentialId = scmCredential.getId();

            assertEquals(
                    projectsBefore.getCount()+1,
                    awxClient.getProjects().getCount()
            );

            assertEquals(
                    jobTemplatesBefore.getCount()+1,
                    awxClient.getJobTemplates().getCount()
            );

            assertEquals(
                    credentials.getCount()+1,
                    awxClient.getCredentials().getCount()
            );
        }
    }

    private void createConsulAndVaultCustomCredential() throws JsonProcessingException {
        CredentialTypeDTOApiCreate consulCustomCredentialType = getConsulCustomCredentialType();
        CredentialTypeDTOApiCreate vaultCustomCredentialType = getVaultCustomCredentialType();

        awxClient.createCredentialType(consulCustomCredentialType);
        awxClient.createCredentialType(vaultCustomCredentialType);

        awxClient.createCredential(getConsulCredentialDTO());
        awxClient.createCredential(getVaultCredentialDTO());
    }

    private CredentialTypeDTOApiCreate getVaultCustomCredentialType() {
        CredentialTypeInputFieldItem vaultUrlItem = new CredentialTypeInputFieldItem(
                "vault_url",
                "Vault URL",
                false,
                "string"
        );

        CredentialTypeInputFieldItem vaultApproleIdItem = new CredentialTypeInputFieldItem(
                "vault_approle_role_id",
                "Vault App Role - Role ID",
                false,
                "string"
        );

        CredentialTypeInputFieldItem vaultApproleSecretIdItem = new CredentialTypeInputFieldItem(
                "vault_approle_secret_id",
                "Vault App Role - Secret ID",
                true,
                "string"
        );

        List<CredentialTypeInputFieldItem> credentialTypeInputFieldItemList = Arrays.asList(
                vaultUrlItem,
                vaultApproleIdItem,
                vaultApproleSecretIdItem
        );

        HashMap<String, List<CredentialTypeInputFieldItem>> inputsMap = new HashMap<>();
        inputsMap.put("fields", credentialTypeInputFieldItemList);

        HashMap<String, String> envMap = new HashMap<>();
        envMap.put("VAULT_APPROLE_ROLE_ID", "{{ vault_approle_role_id }}");
        envMap.put("VAULT_APPROLE_SECRET_ID", "{{vault_approle_secret_id}}");
        envMap.put("VAULT_URL", "{{vault_url}}");

        HashMap<String, Map<String, String>> injectorsMap = new HashMap<>();
        injectorsMap.put("env", envMap);

        return new CredentialTypeDTOApiCreate(
                "HashiCorp Vault",
                "",
                "cloud",
                inputsMap,
                injectorsMap
        );
    }

    private CredentialDTOApiCreate getVaultCredentialDTO() throws JsonProcessingException {
        HashMap<String, String> inputs = new HashMap<>();
        inputs.put("vault_url","test");
        inputs.put("vault_approle_role_id","test");
        inputs.put("vault_approle_secret_id","test");

        return new CredentialDTOApiCreate(
                getVaultCustomCredentialType().getName(),
                "",
                awxClient.getDefaultOrganisation().getId(),
                awxClient.getCustomCredentialTypeByName(getVaultCustomCredentialType().getName()).getId(),
                inputs
        );
    }

    private CredentialTypeDTOApiCreate getConsulCustomCredentialType() {
        CredentialTypeInputFieldItem consulUrlItem = new CredentialTypeInputFieldItem(
                "consul_url",
                "Consul URL",
                false,
                "string"
        );

        CredentialTypeInputFieldItem consulTokenItem = new CredentialTypeInputFieldItem(
                "consul_token",
                "Consul Token",
                true,
                "string"
        );

        List<CredentialTypeInputFieldItem> credentialTypeInputFieldItemList = Arrays.asList(
                consulUrlItem,
                consulTokenItem
        );

        HashMap<String, List<CredentialTypeInputFieldItem>> inputsMap = new HashMap<>();
        inputsMap.put("fields", credentialTypeInputFieldItemList);

        HashMap<String, String> envMap = new HashMap<>();
        envMap.put("CONSUL_TOKEN", "{{ consul_token }}");
        envMap.put("CONSUL_URL", "{{consul_url}}");

        HashMap<String, Map<String, String>> injectorsMap = new HashMap<>();
        injectorsMap.put("env", envMap);

        return new CredentialTypeDTOApiCreate(
                "Consul",
                "",
                "cloud",
                inputsMap,
                injectorsMap
        );
    }

    private CredentialDTOApiCreate getConsulCredentialDTO() throws JsonProcessingException {
        HashMap<String, String> inputs = new HashMap<>();
        inputs.put("consul_url","test");
        inputs.put("consul_token","test");

        return new CredentialDTOApiCreate(
                getConsulCustomCredentialType().getName(),
                "",
                awxClient.getDefaultOrganisation().getId(),
                awxClient.getCustomCredentialTypeByName(getConsulCustomCredentialType().getName()).getId(),
                inputs
        );
    }

    @Configuration
    @ComponentScan("org.eclipse.slm.common.awx")
    public static class SpringConfig {

    }
}
