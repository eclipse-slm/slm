package org.eclipse.slm.service_management.features.service_deployment.impl;

import org.eclipse.slm.service_management.features.service_deployment.api.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class DockerComposeFileParserTestUtil {

    public static void assertComposeFiles(String expectedComposeFile, DockerComposeFile actualComposeFile)
            throws JsonProcessingException, JSONException {
        var dockerComposeJson = actualComposeFile.toJsonString();

        Yaml yaml = new Yaml();
        Map<String, Object> expectedDockerComposeYaml = yaml.load(expectedComposeFile);
        var objectMapper = new ObjectMapper();
        var expectedDockerComposeJson = objectMapper.writeValueAsString(expectedDockerComposeYaml);

        JSONAssert.assertEquals(expectedDockerComposeJson, dockerComposeJson, JSONCompareMode.LENIENT);
    }

    public static void assertYAMLFiles(String expectedYAML, Map<String,Object> actualYAML) throws JsonProcessingException, JSONException {
        Yaml yaml = new Yaml();
        Map<String, Object> expectedDockerComposeYaml = yaml.load(expectedYAML);
        var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var expectedDockerComposeJson = objectMapper.writeValueAsString(expectedDockerComposeYaml);

        JSONAssert.assertEquals(expectedDockerComposeJson, objectMapper.writeValueAsString(actualYAML), JSONCompareMode.LENIENT);
    }
}
