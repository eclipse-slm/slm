package org.eclipse.slm.resource_management.common.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.eclipse.slm.resource_management.common.remote_access.CredentialUsernamePassword;
import org.eclipse.slm.resource_management.common.test_utils.AuthorizationHeaderRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class ResourcesRestControllerITHelper {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorizationHeaderRequestFactory authorizationHeaderRequestFactory;

    public ResourceDTO getResource(UUID resourceId) throws Exception {
        var path = ResourcesRestControllerConfig.BASE_PATH + "/" + resourceId;

        var responseContent = mockMvc.perform(
                        get(path)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var objectMapper = new ObjectMapper();
        KotlinModule kotlinModule = new KotlinModule.Builder()
                .build();
        objectMapper.registerModule(kotlinModule);
        var resource = objectMapper.readValue(responseContent, new TypeReference<ResourceDTO>(){});

        return resource;
    }

    public List<ResourceDTO> getResources() throws Exception {
        var path = ResourcesRestControllerConfig.BASE_PATH;

        var responseContent = mockMvc.perform(
                        get(path)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var objectMapper = new ObjectMapper();
        KotlinModule kotlinModule = new KotlinModule.Builder()
                .build();
        objectMapper.registerModule(kotlinModule);
        var resources = objectMapper.readValue(responseContent, new TypeReference<List<ResourceDTO>>(){});

        return resources;
    }

    public String addExistingResource(
            String project,
            CredentialUsernamePassword credentialUsernamePassword,
            BasicResource resource,
            boolean checkResource,
            ResultMatcher resultMatcher
    ) throws Exception {
        return this.addExistingResource(
                project,
                credentialUsernamePassword,
                resource.getHostname(),
                resource.getIp(),
                checkResource,
                resultMatcher
        );
    }

    public String addExistingResource(
            String project,
            CredentialUsernamePassword credentialUsernamePassword,
            String resourceHostname,
            String resourceIp,
            boolean checkResource,
            ResultMatcher resultMatcher
    ) throws Exception {
        var path = ResourcesRestControllerConfig.BASE_PATH + "/";

        var response = mockMvc.perform(
                        put(path)
                                .queryParam("project", project)
                                .queryParam("resourceUsername", credentialUsernamePassword.getUsername())
                                .queryParam("resourcePassword", credentialUsernamePassword.getPassword())
                                .queryParam("resourceHostname", resourceHostname)
                                .queryParam("resourceIp", resourceIp)
                                .queryParam("checkResource", checkResource ? "true" : "false")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andReturn().getResponse();

        var responseBody = response.getContentAsString();
        if (response.getStatus() == 201) {
            return responseBody.substring(1, responseBody.length() - 1);
        }
        else {
            return responseBody;
        }
    }

    public static BasicResource getTestResource() {
        var resource = new BasicResource(
                UUID.randomUUID()
        );
        resource.setHostname("testHost-" + UUID.randomUUID());
        resource.setIp("1.2.3.4");
        resource.setAssetId("testAsset-" + UUID.randomUUID());
        resource.setDriverId("testDriver-" + UUID.randomUUID());
        resource.setClusterMember(false);
        resource.setLocationId(UUID.randomUUID());
        resource.setRemoteAccessIds(List.of());
        resource.setCapabilityServiceIds(List.of());
        resource.setFirmwareVersion("firmware-" + UUID.randomUUID());

        return resource;
    }

}
