package org.eclipse.slm.resource_management.service.rest.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.eclipse.slm.resource_management.model.resource.BasicResource;
import org.eclipse.slm.resource_management.model.resource.CredentialUsernamePassword;
import org.eclipse.slm.resource_management.service.rest.utils.AuthorizationHeaderRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class ResourcesRestControllerITHelper {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorizationHeaderRequestFactory authorizationHeaderRequestFactory;

    public BasicResource getResource(String resourceId) throws Exception {
        var path = ResourcesRestControllerIT.BASE_PATH + "/" + resourceId;

        var responseContent = mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                        get(path)
                                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new KotlinModule());
        var resource = objectMapper.readValue(responseContent, new TypeReference<BasicResource>(){});

        return resource;
    }

    public List<BasicResource> getResources() throws Exception {
        var path = ResourcesRestControllerIT.BASE_PATH + "/";

        var responseContent = mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                        get(path)
                                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new KotlinModule());
        var resources = objectMapper.readValue(responseContent, new TypeReference<List<BasicResource>>(){});

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
        var path = ResourcesRestControllerIT.BASE_PATH + "/";

        var response = mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                        put(path)
                                .queryParam("project", project)
                                .queryParam("resourceUsername", credentialUsernamePassword.getUsername())
                                .queryParam("resourcePassword", credentialUsernamePassword.getPassword())
                                .queryParam("resourceHostname", resourceHostname)
                                .queryParam("resourceIp", resourceIp)
                                .queryParam("checkResource", checkResource ? "true" : "false")
                                .contentType(MediaType.APPLICATION_JSON)))
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

}
