package org.eclipse.slm.resource_management.features.capabilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterMemberType;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityType;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CapabilityInitTest {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void initDockerCapability() throws JsonProcessingException {
        DeploymentCapability capability = new DeploymentCapability(UUID.randomUUID());

        capability.setName("Docker");
        capability.setLogo("mdi-docker");
        capability.setType(Arrays.asList(
                CapabilityType.SETUP,
                CapabilityType.DEPLOY
        ));

        capability.setCapabilityClass("DeploymentCapability");

        capability.setSupportedDeploymentTypes(Arrays.asList(
                DeploymentType.DOCKER_CONTAINER,
                DeploymentType.DOCKER_COMPOSE
        ));

        // Set AWX Capability Actions
        var deploymentCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-docker.git";
        var deploymentCapabilityBranch = "1.0.0";
        capability.getActions()
                .put(ActionType.INSTALL, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "install.yml"));
        capability.getActions()
                .put(ActionType.UNINSTALL, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "uninstall.yml"));
        capability.getActions()
                .put(ActionType.DEPLOY, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "deploy.yml"));
        capability.getActions()
                .put(ActionType.UNDEPLOY, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "undeploy.yml"));


        String jsonString = mapper.writeValueAsString(capability);

        return;
    }

    @Test
    public void initDockerSwarmCapability() throws JsonProcessingException {
        DeploymentCapability capability = new DeploymentCapability(UUID.randomUUID());

        capability.setName("Docker Swarm");
        capability.setLogo("mdi-docker-swarm");
        capability.setType(Arrays.asList(
                CapabilityType.SETUP,
                CapabilityType.DEPLOY,
                CapabilityType.SCALE
        ));

        capability.setCapabilityClass("DeploymentCapability");

        capability.setSupportedDeploymentTypes(Arrays.asList(
                DeploymentType.DOCKER_CONTAINER,
                DeploymentType.DOCKER_COMPOSE
        ));

        // Set AWX Capability Actions
        var dockerSwarmDeploymentCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-docker-swarm.git";
        var dockerSwarmDeploymentCapabilityBranch = "1.0.0";
        capability.getActions().put(ActionType.INSTALL,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "install.yml"));
        capability.getActions().put(ActionType.UNINSTALL,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "uninstall.yml"));
        capability.getActions().put(ActionType.DEPLOY,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "deploy.yml"));
        capability.getActions().put(ActionType.UNDEPLOY,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "undeploy.yml"));
        capability.getActions().put(ActionType.SCALE_UP,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "scaleup.yml"));
        capability.getActions().put(ActionType.SCALE_DOWN,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "scaledown.yml"));

        capability.setClusterMemberTypes(Arrays.asList(
                new ClusterMemberType("Manager", "docker_manager", 3, false),
                new ClusterMemberType("Worker","docker_worker", 1, true)
        ));

        String jsonString = mapper.writeValueAsString(capability);

        return;
    }


    @Test
    public void compareDockerCapability() {
        //region Create Capability1
        DeploymentCapability capability1 = new DeploymentCapability(UUID.randomUUID());

        capability1.setName("Docker");
        capability1.setLogo("mdi-docker");
        capability1.setType(Arrays.asList(
                CapabilityType.SETUP,
                CapabilityType.DEPLOY
        ));

        capability1.setCapabilityClass("DeploymentCapability");

        capability1.setSupportedDeploymentTypes(Arrays.asList(
                DeploymentType.DOCKER_CONTAINER,
                DeploymentType.DOCKER_COMPOSE
        ));

        // Set AWX Capability Actions
        var deploymentCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-docker.git";
        var deploymentCapabilityBranch = "1.0.0";
        capability1.getActions()
                .put(ActionType.INSTALL, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "install.yml"));
        capability1.getActions()
                .put(ActionType.UNINSTALL, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "uninstall.yml"));
        capability1.getActions()
                .put(ActionType.DEPLOY, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "deploy.yml"));
        capability1.getActions()
                .put(ActionType.UNDEPLOY, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "undeploy.yml"));
        //endregion

        //region Create Capability2
        DeploymentCapability capability2 = new DeploymentCapability(UUID.randomUUID());

        capability2.setName("Docker");
        capability2.setLogo("mdi-docker");
        capability2.setType(Arrays.asList(
                CapabilityType.SETUP,
                CapabilityType.DEPLOY
        ));

        capability2.setCapabilityClass("DeploymentCapability");

        capability2.setSupportedDeploymentTypes(Arrays.asList(
                DeploymentType.DOCKER_CONTAINER,
                DeploymentType.DOCKER_COMPOSE
        ));

        // Set AWX Capability Actions
        capability2.getActions()
                .put(ActionType.INSTALL, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "install.yml"));
        capability2.getActions()
                .put(ActionType.UNINSTALL, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "uninstall.yml"));
        capability2.getActions()
                .put(ActionType.DEPLOY, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "deploy.yml"));
        capability2.getActions()
                .put(ActionType.UNDEPLOY, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "undeploy.yml"));
        //endregion

        compareCapabilities(capability1, capability2);
    }

    @Test
    public void compareDockerSwarmCapability() {
        //region Capability 1
        DeploymentCapability capability1 = new DeploymentCapability(UUID.randomUUID());

        capability1.setName("Docker Swarm");
        capability1.setLogo("mdi-docker-swarm");
        capability1.setType(Arrays.asList(
                CapabilityType.SETUP,
                CapabilityType.DEPLOY,
                CapabilityType.SCALE
        ));

        capability1.setCapabilityClass("DeploymentCapability");

        capability1.setSupportedDeploymentTypes(Arrays.asList(
                DeploymentType.DOCKER_CONTAINER,
                DeploymentType.DOCKER_COMPOSE
        ));

        // Set AWX Capability Actions
        var dockerSwarmDeploymentCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-docker-swarm.git";
        var dockerSwarmDeploymentCapabilityBranch = "1.0.0";
        capability1.getActions().put(ActionType.INSTALL,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "install.yml"));
        capability1.getActions().put(ActionType.UNINSTALL,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "uninstall.yml"));
        capability1.getActions().put(ActionType.DEPLOY,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "deploy.yml"));
        capability1.getActions().put(ActionType.UNDEPLOY,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "undeploy.yml"));
        capability1.getActions().put(ActionType.SCALE_UP,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "scaleup.yml"));
        capability1.getActions().put(ActionType.SCALE_DOWN,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "scaledown.yml"));

        capability1.setClusterMemberTypes(Arrays.asList(
                new ClusterMemberType("Manager", "docker_manager", 3, false),
                new ClusterMemberType("Worker","docker_worker", 1, true)
        ));
        //endregion

        //region Capability 2
        DeploymentCapability capability2 = new DeploymentCapability(UUID.randomUUID());

        capability2.setName("Docker Swarm");
        capability2.setLogo("mdi-docker-swarm");
        capability2.setType(Arrays.asList(
                CapabilityType.SETUP,
                CapabilityType.DEPLOY,
                CapabilityType.SCALE
        ));

        capability2.setCapabilityClass("DeploymentCapability");

        capability2.setSupportedDeploymentTypes(Arrays.asList(
                DeploymentType.DOCKER_CONTAINER,
                DeploymentType.DOCKER_COMPOSE
        ));

        // Set AWX Capability Actions
        capability2.getActions().put(ActionType.INSTALL,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "install.yml"));
        capability2.getActions().put(ActionType.UNINSTALL,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "uninstall.yml"));
        capability2.getActions().put(ActionType.DEPLOY,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "deploy.yml"));
        capability2.getActions().put(ActionType.UNDEPLOY,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "undeploy.yml"));
        capability2.getActions().put(ActionType.SCALE_UP,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "scaleup.yml"));
        capability2.getActions().put(ActionType.SCALE_DOWN,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "scaledown.yml"));

        capability2.setClusterMemberTypes(Arrays.asList(
                new ClusterMemberType("Manager", "docker_manager", 3, false),
                new ClusterMemberType("Worker","docker_worker", 1, true)
        ));
        //endregion

        compareCapabilities(capability1, capability2);
    }

    private void compareCapabilities(DeploymentCapability capability1, DeploymentCapability capability2) {
        assertEquals(capability1.getCapabilityClass(), capability2.getCapabilityClass());
        assertEquals(capability1.getName(), capability2.getName());
        assertEquals(capability1.getLogo(), capability2.getLogo());
        assertEquals(capability1.getType(), capability2.getType());

        // Assert Actions:
        assertTrue(capability1.getActions().keySet().equals(capability2.getActions().keySet()));
        capability1.getActions().values().stream().forEach(capAction -> {
            assertTrue(capability2.getActions().values().contains(capAction));
        });

        //Assert clusterMemberTypes:
        capability1.getClusterMemberTypes().stream().forEach(clusMemType -> {
            assertTrue(capability2.getClusterMemberTypes().contains(clusMemType));
        });

        //Assert supported Deployment Types:
        capability1.getSupportedDeploymentTypes().stream().forEach(deplType -> {
            assertTrue(capability2.getSupportedDeploymentTypes().contains(deplType));
        });
    }
}
