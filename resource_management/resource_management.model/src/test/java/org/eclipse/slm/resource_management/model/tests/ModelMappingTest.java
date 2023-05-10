package org.eclipse.slm.resource_management.model.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.model.capabilities.*;
import org.eclipse.slm.resource_management.model.capabilities.actions.AwxCapabilityAction;
import org.eclipse.slm.resource_management.model.capabilities.actions.CapabilityAction;
import org.eclipse.slm.resource_management.model.capabilities.actions.CapabilityActionType;
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
                        CapabilityActionType.INSTALL,
                        CapabilityActionType.UNINSTALL,
                        CapabilityActionType.DEPLOY,
                        CapabilityActionType.UNDEPLOY
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
                .put(CapabilityActionType.INSTALL, new AwxCapabilityAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "install.yml"));
        dc.getActions()
                .put(CapabilityActionType.UNINSTALL, new AwxCapabilityAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "uninstall.yml"));
        dc.getActions()
                .put(CapabilityActionType.DEPLOY, new AwxCapabilityAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "deploy.yml"));
        dc.getActions()
                .put(CapabilityActionType.UNDEPLOY, new AwxCapabilityAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "undeploy.yml"));

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
                      "capabilityActionClass": "AwxCapabilityAction",
                      "capabilityActionType": "UNINSTALL",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "uninstall.yml"
                }
                """;

        CapabilityAction capabilityAction =
                objectMapper.readValue(jsonString, CapabilityAction.class);
    }

    @Test
    public void useJacksonMapperFromJsonToMapOfAwxCapabilityAction() throws JsonProcessingException {
        String jsonString = """
                {
                    "DEPLOY": {
                      "capabilityActionClass": "AwxCapabilityAction",
                      "capabilityActionType": "DEPLOY",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "deploy.yml",
                      "parameter": []
                    },
                    "UNINSTALL": {
                      "capabilityActionClass": "AwxCapabilityAction",
                      "capabilityActionType": "UNINSTALL",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "uninstall.yml",
                      "parameter": []
                    },
                    "INSTALL": {
                      "capabilityActionClass": "AwxCapabilityAction",
                      "capabilityActionType": "INSTALL",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "install.yml",
                      "parameter": []
                    },
                    "UNDEPLOY": {
                      "capabilityActionClass": "AwxCapabilityAction",
                      "capabilityActionType": "UNDEPLOY",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "undeploy.yml",
                      "parameter": []
                    }
                }
                """;

        TypeReference<HashMap<CapabilityActionType, CapabilityAction>> typeRef =
                new TypeReference<HashMap<CapabilityActionType, CapabilityAction>>(){};

        HashMap<CapabilityActionType, CapabilityAction> awxCapabilityAction =
                objectMapper.readValue(jsonString, typeRef);

        JsonNode awxCapabilityActionJsonNode = objectMapper.readTree(jsonString);

        for(Map.Entry<CapabilityActionType, CapabilityAction> entry : awxCapabilityAction.entrySet()) {
            CapabilityActionType capabilityActionType = entry.getKey();
            AwxCapabilityAction capabilityAction = (AwxCapabilityAction) entry.getValue();

            assertNotNull(
                    awxCapabilityActionJsonNode.get(capabilityActionType.name())
            );

            assertEquals(
                    awxCapabilityActionJsonNode.get(capabilityActionType.name()).get("awxRepo").asText(),
                    capabilityAction.getAwxRepo()
            );

            assertEquals(
                    awxCapabilityActionJsonNode.get(capabilityActionType.name()).get("awxBranch").asText(),
                    capabilityAction.getAwxBranch()
            );

            assertEquals(
                    awxCapabilityActionJsonNode.get(capabilityActionType.name()).get("playbook").asText(),
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
                      "capabilityActionClass": "AwxCapabilityAction",
                      "capabilityActionType": "DEPLOY",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "deploy.yml"
                    },
                    "UNINSTALL": {
                      "capabilityActionClass": "AwxCapabilityAction",
                      "capabilityActionType": "UNINSTALL",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "uninstall.yml"
                    },
                    "INSTALL": {
                      "capabilityActionClass": "AwxCapabilityAction",
                      "capabilityActionType": "INSTALL",
                      "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-docker.git",
                      "awxBranch": "1.0.0",
                      "playbook": "install.yml"
                    },
                    "UNDEPLOY": {
                      "capabilityActionClass": "AwxCapabilityAction",
                      "capabilityActionType": "UNDEPLOY",
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
