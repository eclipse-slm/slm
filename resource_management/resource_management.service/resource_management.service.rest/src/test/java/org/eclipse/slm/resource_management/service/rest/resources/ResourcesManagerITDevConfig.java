package org.eclipse.slm.resource_management.service.rest.resources;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.common.vault.client.VaultCredentialType;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityType;
import org.eclipse.slm.resource_management.model.capabilities.DeploymentCapability;
import org.eclipse.slm.resource_management.model.actions.AwxAction;
import org.eclipse.slm.resource_management.model.actions.Action;
import org.eclipse.slm.resource_management.model.actions.ActionType;
import org.eclipse.slm.resource_management.model.resource.ConnectionType;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class ResourcesManagerITDevConfig {
    public final static int CONSUL_PORT = 8500;
    public final static String CONSUL_SERVICE_NAME = "consul";
    public final static int VAULT_PORT = 8200;
    public final static String VAULT_SERVICE_NAME = "vault";
    public final static DockerComposeContainer dockerCompose;
    public final static File dockerComposeFile = new File("src/test/resources/docker-compose-consul-vault.yml");
    public static final ConsulCredential cCred = new ConsulCredential();
    public static final VaultCredential vCred = new VaultCredential(VaultCredentialType.APPLICATION_PROPERTIES);
    public static DeploymentCapability dockerDeploymentCapability = new DeploymentCapability();
    public static AccessToken accessToken = new AccessToken();
    public static String keycloakSubject = "9141256b-3094-47f1-b1f6-11016c59cd2b";
    public static KeycloakPrincipal keycloakPrincipal;

    static {
        //region docker compose
        dockerCompose = new DockerComposeContainer(dockerComposeFile)
                .withExposedService(
                        CONSUL_SERVICE_NAME,
                        CONSUL_PORT,
                        Wait.forListeningPort()
                )
                .withExposedService(
                        VAULT_SERVICE_NAME,
                        VAULT_PORT,
                        Wait.forListeningPort())
                .withLocalCompose(false);
        dockerCompose.start();

        Runtime.getRuntime().addShutdownHook(new Thread(()-> dockerCompose.stop()));
        //endregion

        //region init capability
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
        HashMap<ActionType, Action> capabilityActions = new HashMap();
        capabilityActions.put(
                ActionType.INSTALL,
                new AwxAction(
                        "awxRepo",
                        "awxBranch",
                        "playbook",
                        new HashSet<>(Arrays.asList(ConnectionType.ssh, ConnectionType.WinRM))
                )
        );
        capabilityActions.put(
                ActionType.UNINSTALL,
                new AwxAction(
                        "awxRepo",
                        "awxBranch",
                        "playbook",
                        new HashSet<>(Arrays.asList(ConnectionType.ssh, ConnectionType.WinRM))
                )
        );
        capabilityActions.put(
                ActionType.DEPLOY,
                new AwxAction(
                        "awxRepo",
                        "awxBranch",
                        "playbook",
                        new HashSet<>(Arrays.asList(ConnectionType.ssh, ConnectionType.WinRM, ConnectionType.tcp))
                )
        );
        capabilityActions.put(
                ActionType.UNDEPLOY,
                new AwxAction(
                        "awxRepo",
                        "awxBranch",
                        "playbook",
                        new HashSet<>(Arrays.asList(ConnectionType.ssh, ConnectionType.WinRM, ConnectionType.tcp))
                )
        );

        dockerDeploymentCapability.setActions(capabilityActions);
        //endregion

        //region Keycloak
        accessToken.setSubject(keycloakSubject);
        keycloakPrincipal = new KeycloakPrincipal<>("testUser", new KeycloakSecurityContext("",accessToken, "", null));
        //endregion
    }


}
