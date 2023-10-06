package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DockerComposeFileParser - Section 'services/{service}/depends_on'")
public class DockerComposeFileParserDependsOnTest {

    @Nested
    @DisplayName("Deserialization")
    public class Deserialization {

        @Test
        @DisplayName("Deserialize depends_on defined as map")
        public void deserializeDependsOnDefinedAsMap() throws JsonProcessingException {
            var composeFile = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        depends_on:
                          service1:
                            condition: service_healthy
                          service2:
                            condition: service_healthy
                    """;

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
            var parsedDependsOn = parsedComposeFile.getServices().get("test-service").getDependsOn();
            assertThat(parsedDependsOn)
                .hasEntrySatisfying("service1", e -> assertThat(e).usingRecursiveComparison().isEqualTo(
                        new DockerComposeFileDependsOnDefinition("service1",
                                DockerComposeFileDependsOnConditionType.SERVICE_HEALTHY)))
                .hasEntrySatisfying("service2", e -> assertThat(e).usingRecursiveComparison().isEqualTo(
                        new DockerComposeFileDependsOnDefinition("service2",
                                DockerComposeFileDependsOnConditionType.SERVICE_HEALTHY)));
        }

        @Test
        @DisplayName("Deserialize depends_on defined as list")
        public void deserializeDependsOnDefinedAsArray() throws JsonProcessingException {
            var composeFile = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        depends_on:
                          - service1
                          - service2
                    """;

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
            var parsedDependsOn = parsedComposeFile.getServices().get("test-service").getDependsOn();
            assertThat(parsedDependsOn)
                    .hasEntrySatisfying("service1", e -> assertThat(e).usingRecursiveComparison().isEqualTo(
                            new DockerComposeFileDependsOnDefinition("service1")))
                    .hasEntrySatisfying("service2", e -> assertThat(e).usingRecursiveComparison().isEqualTo(
                            new DockerComposeFileDependsOnDefinition("service2")));
        }
    }

    @Nested
    @DisplayName("Serialization")
    public class Serialization {

        @Test
        @DisplayName("Serialize depends_on with condition")
        public void serializeDependsOnWithCondition() throws JsonProcessingException, JSONException {
            var expectedCompose = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        depends_on:
                          service1:
                            condition: service_healthy
                          service2:
                            condition: service_healthy
                    """;

            var dependsOn = new DockerComposeFileDependsOn();
            dependsOn.put("service1", new DockerComposeFileDependsOnDefinition("service1", DockerComposeFileDependsOnConditionType.SERVICE_HEALTHY));
            dependsOn.put("service2", new DockerComposeFileDependsOnDefinition("service2", DockerComposeFileDependsOnConditionType.SERVICE_HEALTHY));
            var service = new DockerComposeFileService("test-image:1.0.0",
                    null, null, new ArrayList<>(), new ArrayList<>(), new DockerComposeFileEnvironment(),
                   null, null, new DockerComposeFileLabels(), null, null, null,
                    dependsOn, null, null, null, "");
            var composeFile = new DockerComposeFile("3", Map.of("test-service", service), null, null);

            var actualComposeYAML = DockerComposeFileParser.composeFileToYAML(composeFile);
            DockerComposeFileParserTestUtil.assertYAMLFiles(expectedCompose, actualComposeYAML);
        }

        @Test
        @DisplayName("Serialize depends_on without condition")
        public void serializeDependsOnWithoutCondition() throws JsonProcessingException, JSONException {
            var expectedCompose = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        depends_on:
                          - service1
                          - service2
                    """;

            var dependsOn = new DockerComposeFileDependsOn();
            dependsOn.put("service1", new DockerComposeFileDependsOnDefinition("service1"));
            dependsOn.put("service2", new DockerComposeFileDependsOnDefinition("service2"));
            var service = new DockerComposeFileService("test-image:1.0.0",
                    null, null, new ArrayList<>(), new ArrayList<>(), new DockerComposeFileEnvironment(),
                    null, null, new DockerComposeFileLabels(), null, null, null,
                    dependsOn, null, null, null, "");
            var composeFile = new DockerComposeFile("3", Map.of("test-service", service), null, null);

            var actualComposeYAML = DockerComposeFileParser.composeFileToYAML(composeFile);
            DockerComposeFileParserTestUtil.assertYAMLFiles(expectedCompose, actualComposeYAML);
        }
    }
}
