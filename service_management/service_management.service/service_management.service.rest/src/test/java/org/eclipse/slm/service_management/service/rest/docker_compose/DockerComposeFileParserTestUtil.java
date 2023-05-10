package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.service_management.service.rest.docker_compose.DockerComposeFile;
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
}
