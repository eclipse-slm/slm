package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.options.*;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DockerComposeFileParser - Section 'services/{service}/env_file'")
public class DockerComposeFileParserEnvFileTest {

    @DisplayName("Merge compose file with environment files")
    @Test
    public void mergeComposeFileWithEnvFiles() throws JsonProcessingException, JSONException {
        //region Expected Result
        String EXPECTED_COMPOSE_FILE = """
                    version: '3'
                        
                    services:
                    
                      service1:
                        image: https://sample-registry.org/test-image-service1:1.0.0
                        restart: always
                        ports:
                          - "8080:8080"
                          - "7070:9090"
                        environment:
                          UI_ROOT_PASSWORD: password
                          list1EnvVar1: defaultVal1.1
                          list1EnvVar2: defaultVal1.2
                          list1EnvVar3: defaultVal1.3
                          list1EnvVar4: defaultVal1.4
                          list2EnvVar1: defaultVal2.1
                          list2EnvVar2: defaultVal2.2
                          list2EnvVar3: defaultVal2.3
                          list2EnvVar4: defaultVal2.4
                        volumes:
                          - /my/path/volume:/var/lib/data
                          - my_named_volume:/var/lib/config
                        labels:
                          label1: service1LabelVal1
                          label2: service1LabelVal2
                          label3: service1LabelVal3
                          
                      service2:
                        image: https://sample-registry.org/test-image-service2:1.2.3
                        restart: always
                        ports:
                          - "3333:4444"
                          - "9090:8080"
                        environment:
                          DATABASE_ROOT_PASSWORD: password
                          list2EnvVar1: defaultVal2.1
                          list2EnvVar2: defaultVal2.2
                          list2EnvVar3: defaultVal2.3
                          list2EnvVar4: defaultVal2.4
                        volumes:
                          - my_service2_volume:/var/lib/config
                          - /service/config:/etc/service/config
                        labels:
                          label1: service2LabelVal1
                          label2: service2LabelVal2
                          label3: service2LabelVal3
                """;
        //endregion

        //region Test Input
        final var DOCKER_COMPOSE_FILE_INCOMING = """
                    version: '3'
                        
                    services:
                    
                      service1:
                        image: ${REGISTRY_HOST}/test-image-service1:${SERVICE1_VERSION}
                        restart: always
                        ports:
                          - "8080:8080"
                          - "7070:9090"
                        env_file:
                          - env1.list
                          - env2.list
                        environment:
                         - "UI_ROOT_PASSWORD=${ROOT_PW}"
                        volumes:
                          - /my/path/volume:/var/lib/data
                          - my_named_volume:/var/lib/config
                        labels:
                          label1: service1LabelVal1
                          label2: service1LabelVal2
                          label3: service1LabelVal3
                          
                      service2:
                        image: ${REGISTRY_HOST}/test-image-service2:${SERVICE2_VERSION}
                        restart: always
                        ports:
                          - "3333:4444"
                          - "9090:8080"
                        env_file:
                          - env2.list
                        environment:
                          - "DATABASE_ROOT_PASSWORD=${ROOT_PW}"
                        volumes:
                          - my_service2_volume:/var/lib/config
                          - /service/config:/etc/service/config
                        labels:
                          - "label1=service2LabelVal1"
                          - "label2=service2LabelVal2"
                          - "label3=service2LabelVal3"
                """;

        final var DOT_ENV_FILE = """
                    REGISTRY_HOST=https://sample-registry.org
                    SERVICE1_VERSION=1.0.0
                    SERVICE2_VERSION=1.2.3
                    ROOT_PW=password     
                """;

        final var ENV1_LIST = """
                    list1EnvVar1=defaultVal1.1
                    list1EnvVar2=defaultVal1.2
                    list1EnvVar3=defaultVal1.3
                    list1EnvVar4=defaultVal1.4
                """;

        final var ENV2_LIST = """
                    list2EnvVar1=defaultVal2.1
                    list2EnvVar2=defaultVal2.2
                    list2EnvVar3=defaultVal2.3
                    list2EnvVar4=defaultVal2.4
                """;

        final var ENV_FILES = Map.of(
                "env1.list", ENV1_LIST,
                "env2.list", ENV2_LIST
        );
        //endregion

        var dockerComposeFile = DockerComposeFileParser.mergeComposeFileWithEnvFiles(
                DOCKER_COMPOSE_FILE_INCOMING,
                DOT_ENV_FILE,
                ENV_FILES);

        DockerComposeFileParserTestUtil.assertComposeFiles(EXPECTED_COMPOSE_FILE, dockerComposeFile);
    }

    @Nested
    @DisplayName("Service Options")
    public class ServiceOptions {
        @Test
        @DisplayName("Replace service option values in environment files")
        public void replaceServiceOptionValuesInEnvFile() throws ServiceOptionNotFoundException {
            var serviceOptionCategories = Arrays.asList(
                    new ServiceOptionCategory(0, "Test", Arrays.asList(
                            new ServiceOption("env.list", "envVar1", "Env Var 1", "",
                                    ServiceOptionType.ENVIRONMENT_VARIABLE, "defaultVal1",
                                    ServiceOptionValueType.STRING, false, false),
                            new ServiceOption("env.list", "envVar3", "Env Var 1", "",
                                    ServiceOptionType.ENVIRONMENT_VARIABLE, "defaultVal1",
                                    ServiceOptionValueType.STRING, false, false),
                            new ServiceOption("env.list", "envVar4", "Env Var 1", "",
                                    ServiceOptionType.ENVIRONMENT_VARIABLE, "defaultVal1",
                                    ServiceOptionValueType.STRING, false, false)
                    ))
            );
            var serviceOfferingVersion = new ServiceOfferingVersion(
                    UUID.randomUUID(), null, "1.0.0", null);
            serviceOfferingVersion.setServiceOptionCategories(serviceOptionCategories);

            var envFile = """
                envVar1=defaultVal1
                envVar2=defaultVal2
                envVar3=defaultVal3
                envVar4=defaultVal4
            """;
            var serviceOptionValues = List.of(
                    new ServiceOptionValue("env.list|envVar1", "newValue1"),
                    new ServiceOptionValue("env.list|envVar3", "newValue3"),
                    new ServiceOptionValue("env.list|envVar4", "newValue4"));

            var resultEnvFile = DockerComposeFileParser.replaceServiceOptionValuesInEnvFile(envFile, serviceOptionValues, serviceOfferingVersion);

            var envFileExpected = """
                envVar1=newValue1
                envVar2=defaultVal2
                envVar3=newValue3
                envVar4=newValue4
            """;

            assertThat(DockerComposeFileParser.getKeyValuePairs(resultEnvFile)).usingRecursiveComparison()
                    .isEqualTo(DockerComposeFileParser.getKeyValuePairs(envFileExpected));
        }
    }

}
