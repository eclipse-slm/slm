package org.eclipse.slm.resource_management.features.capabilities;


import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType;
import org.eclipse.slm.resource_management.common.resources.BasicResource;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityType;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionConfigParameter;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionConfigParameterRequiredType;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionConfigParameterValueType;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SingleHostCapabilitiesConsulClientTestData {
    private final static Logger LOG = LoggerFactory.getLogger(SingleHostCapabilitiesConsulClientTestData.class);

    public static final BasicResource testResource1 = new BasicResource(UUID.randomUUID(), "Test-Host-1", "192.168.0.1");
    public static final BasicResource testResource2 = new BasicResource(UUID.randomUUID(), "Test-Host-2", "192.168.0.2");

    public static DeploymentCapability testSingleHostDeploymentCapability;

    static {
        testSingleHostDeploymentCapability = new DeploymentCapability(UUID.randomUUID());
        testSingleHostDeploymentCapability.setName("Dummy");
        testSingleHostDeploymentCapability.setLogo("mdi-dummy");
        testSingleHostDeploymentCapability.setType(Arrays.asList(CapabilityType.SETUP, CapabilityType.DEPLOY));
        testSingleHostDeploymentCapability.setCapabilityClass("DeploymentCapability");
        testSingleHostDeploymentCapability.setSupportedDeploymentTypes(Arrays.asList(DeploymentType.DOCKER_CONTAINER, DeploymentType.DOCKER_COMPOSE));
        // Set AWX Capability Actions
        var deploymentCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-dummy";
        var deploymentCapabilityBranch = "main";
        testSingleHostDeploymentCapability.getActions()
                .put(ActionType.INSTALL, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "install.yml"));
        testSingleHostDeploymentCapability.getActions()
                .put(ActionType.UNINSTALL, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "uninstall.yml"));
        testSingleHostDeploymentCapability.getActions()
                .put(ActionType.DEPLOY, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "deploy.yml"));
        testSingleHostDeploymentCapability.getActions()
                .put(ActionType.UNDEPLOY, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "undeploy.yml"));

    List<ActionConfigParameter> configParamters = Arrays.asList(
            new ActionConfigParameter("username", "Username", "",
                    ActionConfigParameterValueType.STRING, "", ActionConfigParameterRequiredType.ALWAYS, false
            ),
            new ActionConfigParameter("password", "Password", "",
                    ActionConfigParameterValueType.STRING, "", ActionConfigParameterRequiredType.ALWAYS, true
            )
    );

    testSingleHostDeploymentCapability.setConnection(ConnectionType.http);

    testSingleHostDeploymentCapability
            .getActions()
            .get(ActionType.INSTALL)
            .setConfigParameters(configParamters);

    }
}