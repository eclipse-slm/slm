package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.docker.compose.DockerComposeDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.model.offerings.options.*;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerComposeFileParserTest {

    @DisplayName("Generate deployable compose file for service offering")
    @Test
    public void generateDeployableComposeFileForServiceOffering() throws JsonProcessingException, ServiceOptionNotFoundException, JSONException, InvalidServiceOfferingDefinitionException {
        //region Expected Result
        String EXPECTED_COMPOSE_FILE = """
            version: '3'
                
            services:
            
              service1:
                image: https://sample-registry.org/test-image-service1:1.0.0
                restart: always
                ports:
                  - "8080:8080"
                  - "6060:9090"
                environment:
                  UI_ROOT_PASSWORD: customRootPW
                  list1EnvVar1: customVal1.1
                  list1EnvVar2: defaultVal1.2
                  list1EnvVar3: defaultVal1.3
                  list1EnvVar4: defaultVal1.4
                  list2EnvVar1: defaultVal2.1
                  list2EnvVar2: customVal2.2
                  list2EnvVar3: defaultVal2.3
                  list2EnvVar4: defaultVal2.4
                volumes:
                  - /my/custom/path:/var/lib/data
                  - my_named_volume:/var/lib/config
                labels:
                  label1: customLabelService1.1
                  label2: service1LabelVal2
                  label3: service1LabelVal3
                devices:
                  - /dev/device1
                depends_on:
                  - service2
                  
              service2:
                image: https://sample-registry.org/test-image-service2:1.2.3
                restart: always
                ports:
                  - "3333:4444"
                  - "9090:8080"
                environment:
                  DATABASE_ROOT_PASSWORD: customRootPW
                  list2EnvVar1: defaultVal2.1
                  list2EnvVar2: customVal2.2
                  list2EnvVar3: defaultVal2.3
                  list2EnvVar4: defaultVal2.4
                volumes:
                  - my_custom_named_volume:/var/lib/config
                  - /service/config:/etc/service/config
                labels:
                  label1: service2LabelVal1
                  label2: service2LabelVal2
                  label3: customLabelService2.3
                depends_on:
                  service1:
                    condition: service_healthy
            
            networks:
              backend:
              frontend:
              
            volumes:
              my_custom_vol:
                external: true
        """;
        //endregion

        //region Test Input
        //region Files
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
                devices:
                  - /dev/device1
                depends_on:
                  - service2
                  
              service2:
                image: ${REGISTRY_HOST}/test-image-service2:${SERVICE2_VERSION}
                restart: always
                ports:
                  - "4444:4444"
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
                depends_on:
                  service1:
                    condition: service_healthy
            
            networks:     
              backend:
              frontend:
              
            volumes:
              my_custom_vol:
                external: true
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

        var serviceOptionCategories = Arrays.asList(
                new ServiceOptionCategory(0, "Test", Arrays.asList(

                        new ServiceOption(".env", "ROOT_PW", "Root Password", "",
                                ServiceOptionType.ENVIRONMENT_VARIABLE, "password",
                                ServiceOptionValueType.PASSWORD, false, false),

                        new ServiceOption("env1.list", "list1EnvVar1", "List 1 Env Var 1", "",
                                ServiceOptionType.ENVIRONMENT_VARIABLE, "list1EnvVar1=defaultVal1.1",
                                ServiceOptionValueType.STRING, false, false),
                        new ServiceOption("env2.list", "list2EnvVar2", "List 2 Env Var 2", "",
                                ServiceOptionType.ENVIRONMENT_VARIABLE, "defaultVal2.2",
                                ServiceOptionValueType.STRING, false, false),

                        new ServiceOption("service1", "label1", "Service 1 Label 1", "",
                                ServiceOptionType.LABEL, "service1LabelVal1",
                                ServiceOptionValueType.STRING, false, false),
                        new ServiceOption("service2", "label3", "Service 2 Label 3", "",
                                ServiceOptionType.LABEL, "service2LabelVal3",
                                ServiceOptionValueType.STRING, false, false),

                        new ServiceOption("service1", "/var/lib/data", "Volume X", "",
                                ServiceOptionType.VOLUME, "/my/path/volume",
                                ServiceOptionValueType.VOLUME, false, false),
                        new ServiceOption("service2", "/var/lib/config", "Volume Y", "",
                                ServiceOptionType.VOLUME, "my_service2_volume",
                                ServiceOptionValueType.VOLUME, false, false),

                        new ServiceOption("service1", "9090", "Port X", "",
                                ServiceOptionType.PORT_MAPPING, "7070",
                                ServiceOptionValueType.PORT, false, false),
                        new ServiceOption("service2", "4444", "Port Y", "",
                                ServiceOptionType.PORT_MAPPING, "4444",
                                ServiceOptionValueType.PORT, false, false)
                        ))
        );
        var dockerComposeDeploymentDefinition = new DockerComposeDeploymentDefinition();
        dockerComposeDeploymentDefinition.setComposeFile(DOCKER_COMPOSE_FILE_INCOMING);
        dockerComposeDeploymentDefinition.setDotEnvFile(DOT_ENV_FILE);
        dockerComposeDeploymentDefinition.setEnvFiles(ENV_FILES);
        var serviceOfferingVersion = new ServiceOfferingVersion(
                UUID.randomUUID(), null, "1.0.0", dockerComposeDeploymentDefinition);
        serviceOfferingVersion.setServiceOptionCategories(serviceOptionCategories);

        var serviceOptionValues = List.of(
                new ServiceOptionValue(".env|ROOT_PW", "customRootPW"),

                new ServiceOptionValue("env1.list|list1EnvVar1", "customVal1.1"),
                new ServiceOptionValue("env2.list|list2EnvVar2", "customVal2.2"),

                new ServiceOptionValue("service1|label1", "customLabelService1.1"),
                new ServiceOptionValue("service2|label3", "customLabelService2.3"),

                new ServiceOptionValue("service1|/var/lib/data", "/my/custom/path"),
                new ServiceOptionValue("service2|/var/lib/config", "my_custom_named_volume"),

                new ServiceOptionValue("service1|9090", "6060"),
                new ServiceOptionValue("service2|4444", "3333"));
        //endregion

        var dockerComposeFile = DockerComposeFileParser.generateDeployableComposeFileForServiceOffering(
                serviceOfferingVersion, serviceOptionValues);

        DockerComposeFileParserTestUtil.assertComposeFiles(EXPECTED_COMPOSE_FILE, dockerComposeFile);
    }
}
