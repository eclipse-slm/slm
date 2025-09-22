package org.eclipse.slm.resource_management.features.capabilities;

import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.resource_management.common.adapters.ResourcesVaultClient;
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType;
import org.eclipse.slm.resource_management.common.remote_access.CredentialUsernamePassword;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessDTO;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityType;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.*;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.*;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilitiesVaultClient;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;

import java.util.*;

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
    public final UUID resourceId = UUID.randomUUID();

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

        var remoteAccess = new RemoteAccessDTO(
                UUID.randomUUID(),
                new CredentialUsernamePassword("username","password"),
                22,
                ConnectionType.ssh);

//        resourcesVaultClient.addSecretsForConnectionService(remoteAccess);
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
