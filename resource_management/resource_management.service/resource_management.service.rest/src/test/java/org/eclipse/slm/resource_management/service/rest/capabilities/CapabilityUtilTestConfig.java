package org.eclipse.slm.resource_management.service.rest.capabilities;

import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.model.capabilities.Capability;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityType;
import org.eclipse.slm.resource_management.model.capabilities.DeploymentCapability;
import org.eclipse.slm.resource_management.model.capabilities.actions.*;

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
    private static AwxCapabilityAction awxCapabilityAction = new AwxCapabilityAction(
            "repo",
            "branch",
            "playbook"
    );
    private static Map<CapabilityActionType, CapabilityAction> actions = Map.of(
            CapabilityActionType.INSTALL, awxCapabilityAction,
            CapabilityActionType.UNINSTALL, awxCapabilityAction,
            CapabilityActionType.DEPLOY, awxCapabilityAction,
            CapabilityActionType.UNDEPLOY, awxCapabilityAction
    );
    private static List<DeploymentType> deploymentTypes = Arrays.asList(
            DeploymentType.DOCKER_CONTAINER,
            DeploymentType.DOCKER_COMPOSE
    );

    private static List<CapabilityActionConfigParameter> capabilityConfigParamters = Arrays.asList(
            new CapabilityActionConfigParameter(
                    "username",
                    "Username",
                    "",
                    CapabilityActionConfigParameterValueType.STRING,
                    "",
                    CapabilityActionConfigParameterRequiredType.ALWAYS,
                    false
            ),
            new CapabilityActionConfigParameter(
                    "password",
                    "Password",
                    "",
                    CapabilityActionConfigParameterValueType.STRING,
                    "",
                    CapabilityActionConfigParameterRequiredType.ALWAYS,
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
                .get(CapabilityActionType.INSTALL)
                .setConfigParameters(capabilityConfigParamters);

        return singleHostCapability;
    }
}
