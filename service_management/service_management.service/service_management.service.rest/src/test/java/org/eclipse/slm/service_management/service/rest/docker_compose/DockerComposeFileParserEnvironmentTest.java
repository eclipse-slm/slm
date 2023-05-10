package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionValue;
import org.eclipse.slm.service_management.service.rest.docker_compose.DockerComposeFileEnvironment;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DockerComposeFileParser - Section 'services/{service}/environment'")
public class DockerComposeFileParserEnvironmentTest {

    @Nested
    @DisplayName("Constructors")
    public class Constructors {
        @Test
        @DisplayName("Create from existing environment map")
        public void withExistingEnvironmentMap() {
            var expectedEnvironment = new DockerComposeFileEnvironment();
            expectedEnvironment.put("envVar1", "envVal1");
            expectedEnvironment.put("envVar2", true);

            var existingEnvMap = Map.of("envVar1", "envVal1", "envVar2", true);
            var environment = new DockerComposeFileEnvironment(existingEnvMap);

            assertThat(environment).usingRecursiveComparison()
                    .isEqualTo(expectedEnvironment);
        }
    }

    @Nested
    @DisplayName("Parsing")
    public class Environment {
        @Test
        @DisplayName("Parse environment defined as map")
        public void parseEnvironmentDefinedAsMap() throws JsonProcessingException {
            var composeFile = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        environment:
                          EnvVar1: EnvVal1
                          EnvVar2: 1
                          EnvVar3: true
                    """;

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
            var parsedEnvironment = parsedComposeFile.getServices().get("test-service").getEnvironment();
            assertThat(parsedEnvironment)
                    .containsEntry("EnvVar1", "EnvVal1")
                    .containsEntry("EnvVar2", "1")
                    .containsEntry("EnvVar3", "true");
        }

        @Test
        @DisplayName("Parse environment defined as array")
        public void parseEnvironmentDefinedAsArray() throws JsonProcessingException {
            var composeFile = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        environment:
                          - "EnvVar1=EnvVal1"
                          - "EnvVar2=1"
                          - "EnvVar3=true"
                    """;

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
            var parsedEnvironment = parsedComposeFile.getServices().get("test-service").getEnvironment();
            assertThat(parsedEnvironment)
                    .containsEntry("EnvVar1", "EnvVal1")
                    .containsEntry("EnvVar2", "1")
                    .containsEntry("EnvVar3", "true");
        }
    }

    @Nested
    @DisplayName("Service Options")
    public class ServiceOptions {
        @Test
        @DisplayName("Replace service options values for environment variables")
        public void replaceServiceOptionValuesForEnvironmentVariables() throws JSONException, JsonProcessingException {
            //region Expected Results
            var expectedComposeFile = """
                        version: '3'
                            
                        services:
                        
                          service1:
                            image: testImage:latest
                            environment:
                              UI_ROOT_USER: admin
                              UI_ROOT_PASSWORD: customPassword
                              
                          service2:
                            image: testImage2:latest
                            environment:
                              EnvVar1: val1
                              EnvVar2: defaultVal2
                    """;
            //endregion

            //region Test Input
            var incomingComposeFile = DockerComposeFileParser.parseComposeFile("""
                        version: '3'
                            
                        services:
                        
                          service1:
                            image: testImage:latest
                            environment:
                              - "UI_ROOT_USER=root"
                              - "UI_ROOT_PASSWORD=password"
                              
                          service2:
                            image: testImage2:latest
                            environment:
                              - "EnvVar1=defaultVal1"
                              - "EnvVar2=defaultVal2"
                    """);

            var serviceOptionValues = List.of(
                    new ServiceOptionValue("service1|UI_ROOT_USER", "admin"),
                    new ServiceOptionValue("service1|UI_ROOT_PASSWORD", "customPassword"),
                    new ServiceOptionValue("service2|EnvVar1", "val1"));
            //endregion

            var resultComposeFile = DockerComposeFileParser.replaceServiceOptionValuesForEnvironmentVariables(serviceOptionValues, incomingComposeFile);
            DockerComposeFileParserTestUtil.assertComposeFiles(expectedComposeFile, resultComposeFile);
        }
    }
}
