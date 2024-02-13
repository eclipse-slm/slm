package org.eclipse.slm.resource_management.service.rest.capabilities;

import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.resource_management.model.actions.*;
import org.eclipse.slm.resource_management.model.capabilities.Capability;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityType;
import org.eclipse.slm.resource_management.model.capabilities.DeploymentCapability;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.model.consul.capability.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.model.resource.ConnectionType;
import org.eclipse.slm.resource_management.model.resource.CredentialUsernamePassword;
import org.eclipse.slm.resource_management.model.resource.RemoteAccessService;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesVaultClient;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@TestConfiguration
public class CapabilitiesVaultClientTestConfig {
    //region Vault Properties
    VaultCredential vaultCredential = new VaultCredential();
    public static String VAULT_VERSION = "1.11.11";
    public static String VAULT_HOST = "localhost";
    public static int VAULT_PORT = 8200;
    public static String VAULT_TOKEN = "myroot";
    //endregion


    @Autowired
    public ResourcesVaultClient resourcesVaultClient;
    @Autowired
    VaultClient vaultClient;
    public UUID resourceId = UUID.randomUUID();

    private static SingleHostCapabilityService singleHostCapabilityService;
    private static Capability singleHostCapability = new DeploymentCapability(UUID.randomUUID());
    private static String name = "Dummy";
    private static String logo = "mdi-dummy";
    private static List<CapabilityType> types = Arrays.asList(
            CapabilityType.SETUP,
            CapabilityType.DEPLOY
    );
    private static AwxAction awxCapabilityAction = new AwxAction(
            "repo",
            "branch",
            "playbook"
    );
    private static Map<ActionType, Action> actions = Map.of(
            ActionType.INSTALL, awxCapabilityAction,
            ActionType.UNINSTALL, awxCapabilityAction,
            ActionType.DEPLOY, awxCapabilityAction,
            ActionType.UNDEPLOY, awxCapabilityAction
    );
    private static List<DeploymentType> deploymentTypes = Arrays.asList(
            DeploymentType.DOCKER_CONTAINER,
            DeploymentType.DOCKER_COMPOSE
    );

    private static List<ActionConfigParameter> capabilityConfigParamters = Arrays.asList(
            new ActionConfigParameter(
                    "username",
                    "Username",
                    "",
                    ActionConfigParameterValueType.STRING,
                    "",
                    ActionConfigParameterRequiredType.ALWAYS,
                    false
            ),
            new ActionConfigParameter(
                    "password",
                    "Password",
                    "",
                    ActionConfigParameterValueType.STRING,
                    "",
                    ActionConfigParameterRequiredType.ALWAYS,
                    true
            )
    );

    public Map<String, String> configParameter = Map.of(
            "username", "user",
            "password", "pass"
    );

    public void createRemoteAccessService() {
        AccessToken accessToken = new AccessToken();
        accessToken.setSubject(UUID.randomUUID().toString());
        KeycloakPrincipal keycloakPrincipal = new KeycloakPrincipal<>("testUser", new KeycloakSecurityContext("", accessToken, "", null));

        RemoteAccessService remoteAccessService = new RemoteAccessService(
                ConnectionType.ssh,
                22,
                new CredentialUsernamePassword("username","password")
        );

        resourcesVaultClient.addSecretsForConnectionService(
                keycloakPrincipal,
                remoteAccessService
        );
    }
    public void createResourcesSecretEngine() {
        vaultClient.createKvSecretEngine(
                new VaultCredential(),
                SingleHostCapabilitiesVaultClient.VAULT_ENGINE_NAME
        );
    }

    public Capability getSingleHostCapability() {
        singleHostCapability.setName(name);
        singleHostCapability.setLogo(logo);
        singleHostCapability.setType(types);
        singleHostCapability.setActions(actions);
        singleHostCapability.
                getActions()
                .get(ActionType.INSTALL)
                .setConfigParameters(capabilityConfigParamters);

        return singleHostCapability;
    }

    public SingleHostCapabilityService getSingleHostCapabilityService() {
        if(singleHostCapabilityService==null)
            singleHostCapabilityService = new SingleHostCapabilityService(
                    getSingleHostCapability(),
                    resourceId,
                    CapabilityServiceStatus.READY,
                    false,
                    this.configParameter
            );

        return singleHostCapabilityService;
    }
}
