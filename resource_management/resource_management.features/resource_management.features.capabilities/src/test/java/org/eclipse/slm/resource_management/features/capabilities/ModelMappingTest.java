package org.eclipse.slm.resource_management.features.capabilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.dto.DeploymentCapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityType;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.Action;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ModelMappingTest {
    private static ModelMapper modelMapper = new ModelMapper();
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void init() {
        // DTO >>> Entity
        modelMapper.typeMap(DeploymentCapabilityDTOApi.class, Capability.class)
                .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), DeploymentCapability.class));

        // Entity >>> DTO
        modelMapper.typeMap(DeploymentCapability.class, CapabilityDTOApi.class)
                .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), DeploymentCapabilityDTOApi.class));
    }

    private void compareDeploymentCapabilityEntityAndDTO(Capability c, DeploymentCapabilityDTOApi dcDTOApi) {
        assertEquals(c.getId(), dcDTOApi.getId());
        assertEquals(c.getCapabilityClass(), dcDTOApi.getCapabilityClass());
        assertEquals(c.getName(),dcDTOApi.getName());
        assertEquals(c.getLogo(),dcDTOApi.getLogo());
        assertEquals(c.getType().size(), dcDTOApi.getType().size());
        assertEquals(c.getActions().size(), dcDTOApi.getActions().size());
    }

    @Test
    public void checkModelMapperConfig() {

        System.out.println("Type Maps of ModelMapper:");

        modelMapper.getTypeMaps()
                .stream()
                .forEach(tm -> System.out.println(
                        "SourceType: " + tm.getSourceType().getName() + "; " +
                        "Destination Type: " + tm.getDestinationType().getName()
                )
        );

        return;
    }

    @Test
    public void deploymentCapabilityDTOtoEntity() {
        //Create DTO instance
        DeploymentCapabilityDTOApi dcDTOApi = new DeploymentCapabilityDTOApi();
        dcDTOApi.setName("Docker");
        dcDTOApi.setLogo("mdi-docker");
        dcDTOApi.setType(Arrays.asList(
                CapabilityType.SETUP,
                CapabilityType.DEPLOY
        ));
        dcDTOApi.setCapabilityClass("DeploymentCapability");

        dcDTOApi.setSupportedDeploymentTypes(Arrays.asList(
                DeploymentType.DOCKER_CONTAINER,
                DeploymentType.DOCKER_COMPOSE
        ));

        // Set AWX Capability Actions
        dcDTOApi.setActions(
                "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                "1.0.0",
                Arrays.asList(
                        ActionType.INSTALL,
                        ActionType.UNINSTALL,
                        ActionType.DEPLOY,
                        ActionType.UNDEPLOY
                )
        );

        //Create Entity via modelMapper
        DeploymentCapability dc = modelMapper.map(dcDTOApi, DeploymentCapability.class);
        Capability cp = modelMapper.map(dcDTOApi, Capability.class);

        //Compare DTO and Entity
        compareDeploymentCapabilityEntityAndDTO(dc, dcDTOApi);
        compareDeploymentCapabilityEntityAndDTO(cp, dcDTOApi);

        return;
    }

    @Test
    public void deploymentCapabilityEntityToDTO() throws JsonProcessingException {

        //region Do mapping of single Instance:
        //Create Entity instance
        DeploymentCapability dc = new DeploymentCapability();
        dc.setName("Docker");
        dc.setLogo("mdi-docker");
        dc.setType(Arrays.asList(
                CapabilityType.SETUP,
                CapabilityType.DEPLOY
        ));
        dc.setCapabilityClass("DeploymentCapability");

        dc.setSupportedDeploymentTypes(Arrays.asList(
                DeploymentType.DOCKER_CONTAINER,
                DeploymentType.DOCKER_COMPOSE
        ));

        // Set AWX Capability Actions
        var deploymentCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-docker.git";
        var deploymentCapabilityBranch = "1.0.0";
        dc.getActions()
                .put(ActionType.INSTALL, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "install.yml"));
        dc.getActions()
                .put(ActionType.UNINSTALL, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "uninstall.yml"));
        dc.getActions()
                .put(ActionType.DEPLOY, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "deploy.yml"));
        dc.getActions()
                .put(ActionType.UNDEPLOY, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "undeploy.yml"));

        //Create DTO via modelMapper
        DeploymentCapabilityDTOApi dcDTOApi = modelMapper.map(dc, DeploymentCapabilityDTOApi.class);
        CapabilityDTOApi cpDTOApi = modelMapper.map(dc, CapabilityDTOApi.class);

        //Compare DTO and Entity
        compareDeploymentCapabilityEntityAndDTO(dc, dcDTOApi);

        String jsonString = objectMapper.writeValueAsString(cpDTOApi);
        //endregion

        //region Do mapping of Lists:
        // Create Lists:
        List<Capability> capabilityList = new ArrayList<>();
        capabilityList.add(dc);

        List<CapabilityDTOApi> capabilityDTOApiList = capabilityList
                .stream()
                .map(cap -> modelMapper.map(cap, CapabilityDTOApi.class))
                .collect(Collectors.toList());

        String jsonListString = objectMapper.writeValueAsString(capabilityDTOApiList);
        //endregion

        return;
    }

    @Test
    public void useJacksonMapperForJsonToAwxCapabilityAction() throws JsonProcessingException {
        String jsonString = """
                {
                      "actionClass": "AwxAction",
                      "actionType": "UNINSTALL",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "uninstall.yml"
                }
                """;

        Action action =
                objectMapper.readValue(jsonString, Action.class);
    }

    @Test
    public void useJacksonMapperFromJsonToMapOfAwxCapabilityAction() throws JsonProcessingException {
        String jsonString = """
                {
                    "DEPLOY": {
                      "actionClass": "AwxAction",
                      "actionType": "DEPLOY",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "deploy.yml",
                      "parameter": []
                    },
                    "UNINSTALL": {
                      "actionClass": "AwxAction",
                      "actionType": "UNINSTALL",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "uninstall.yml",
                      "parameter": []
                    },
                    "INSTALL": {
                      "actionClass": "AwxAction",
                      "actionType": "INSTALL",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "install.yml",
                      "parameter": []
                    },
                    "UNDEPLOY": {
                      "actionClass": "AwxAction",
                      "actionType": "UNDEPLOY",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "undeploy.yml",
                      "parameter": []
                    }
                }
                """;

        TypeReference<HashMap<ActionType, Action>> typeRef =
                new TypeReference<HashMap<ActionType, Action>>(){};

        HashMap<ActionType, Action> awxCapabilityAction =
                objectMapper.readValue(jsonString, typeRef);

        JsonNode awxCapabilityActionJsonNode = objectMapper.readTree(jsonString);

        for(Map.Entry<ActionType, Action> entry : awxCapabilityAction.entrySet()) {
            ActionType actionType = entry.getKey();
            AwxAction capabilityAction = (AwxAction) entry.getValue();

            assertNotNull(
                    awxCapabilityActionJsonNode.get(actionType.name())
            );

            assertEquals(
                    awxCapabilityActionJsonNode.get(actionType.name()).get("awxRepo").asText(),
                    capabilityAction.getAwxRepo()
            );

            assertEquals(
                    awxCapabilityActionJsonNode.get(actionType.name()).get("awxBranch").asText(),
                    capabilityAction.getAwxBranch()
            );

            assertEquals(
                    awxCapabilityActionJsonNode.get(actionType.name()).get("playbook").asText(),
                    capabilityAction.getPlaybook()
            );
        }

        return;
    }

    @Test
    public void useJacksonMapperFromJsonToDeploymentCapability() throws JsonProcessingException {
        String jsonString = """
                {
                  "uuid": "08c5b8de-5d4a-4116-a73f-1d1f616c7c70",
                  "capabilityClass": "DeploymentCapability",
                  "name": "Docker",
                  "logo": "mdi-docker",
                  "type": [
                    "SETUP",
                    "DEPLOY"
                  ],
                  "actions": {
                    "DEPLOY": {
                      "actionClass": "AwxAction",
                      "actionType": "DEPLOY",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "deploy.yml"
                    },
                    "UNINSTALL": {
                      "actionClass": "AwxAction",
                      "actionType": "UNINSTALL",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "uninstall.yml"
                    },
                    "INSTALL": {
                      "actionClass": "AwxAction",
                      "actionType": "INSTALL",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "install.yml"
                    },
                    "UNDEPLOY": {
                      "actionClass": "AwxAction",
                      "actionType": "UNDEPLOY",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "undeploy.yml"
                    }
                  },
                  "supportedDeploymentTypes": [
                    "DOCKER_CONTAINER",
                    "DOCKER_COMPOSE"
                  ],
                  "clusterMemberTypes": []
                }
                """;

        DeploymentCapabilityDTOApi deploymentCapabilityDTOApi = objectMapper.readValue(
                jsonString,
                DeploymentCapabilityDTOApi.class
        );

        CapabilityDTOApi capabilityDTOApi =
                objectMapper.readValue(jsonString, CapabilityDTOApi.class);

        assertEquals("DeploymentCapability", capabilityDTOApi.getCapabilityClass());

        Capability capability = modelMapper.map(capabilityDTOApi, Capability.class);

        return;
    }
}
