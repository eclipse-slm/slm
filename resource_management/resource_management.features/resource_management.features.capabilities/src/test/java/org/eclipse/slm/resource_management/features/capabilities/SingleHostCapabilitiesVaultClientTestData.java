package org.eclipse.slm.resource_management.features.capabilities;

import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityType;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.*;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;

import java.util.*;

public class SingleHostCapabilitiesVaultClientTestData {

    public static final String TEST_USER_ID = UUID.randomUUID().toString();
    public static final String TEST_GROUP_ID = "/users/" + TEST_USER_ID;

    public static final Capability singleHostCapability = new DeploymentCapability(UUID.randomUUID());
    public static final String name = "Dummy";
    public static final String logo = "mdi-dummy";
    public static final List<CapabilityType> types = Arrays.asList(
            CapabilityType.SETUP,
            CapabilityType.DEPLOY
    );
    public static final AwxAction awxCapabilityAction = new AwxAction("repo", "branch", "playbook");
    public static final Map<ActionType, Action> actions = Map.of(
            ActionType.INSTALL, awxCapabilityAction,
            ActionType.UNINSTALL, awxCapabilityAction,
            ActionType.DEPLOY, awxCapabilityAction,
            ActionType.UNDEPLOY, awxCapabilityAction
    );

    public static final List<ActionConfigParameter> capabilityConfigParameters = Arrays.asList(
            new ActionConfigParameter("username", "Username", "",
                    ActionConfigParameterValueType.STRING, "", ActionConfigParameterRequiredType.ALWAYS, false
            ),
            new ActionConfigParameter("password", "Password", "",
                    ActionConfigParameterValueType.STRING, "", ActionConfigParameterRequiredType.ALWAYS, true
            )
    );
    public static final Map<String, String> configParameters = Map.of("username", "user", "password", "pass");

    public static final SingleHostCapabilityService singleHostCapabilityService = new SingleHostCapabilityService(
            UUID.randomUUID(),
            UUID.randomUUID(),
            singleHostCapability,
            CapabilityServiceStatus.READY,
            false,
            configParameters
    );;

    static {
        singleHostCapability.setName(name);
        singleHostCapability.setLogo(logo);
        singleHostCapability.setType(types);
        singleHostCapability.setActions(actions);
        singleHostCapability.
                getActions()
                .get(ActionType.INSTALL)
                .setConfigParameters(capabilityConfigParameters);
    }
}
