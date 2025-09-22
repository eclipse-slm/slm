package org.eclipse.slm.resource_management.features.capabilities;

import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityType;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.*;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.*;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CapabilityUtilTestConfig {
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

    public static Map<String, String> configParameter = Map.of(
            "username", "user",
            "password", "pass"
    );

    public static Capability getSingleHostCapability() {
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
}
