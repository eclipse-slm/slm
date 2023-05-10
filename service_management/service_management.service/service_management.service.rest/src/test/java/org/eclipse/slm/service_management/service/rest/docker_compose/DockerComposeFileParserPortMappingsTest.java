package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.service_management.model.offerings.options.*;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DockerComposeFileParser - Section 'services/{service}/ports'")
public class DockerComposeFileParserPortMappingsTest {

    @Nested
    @DisplayName("Parsing")
    public class Parsing {

    }

    @Nested
    @DisplayName("Service Options")
    public class ServiceOptions {
        @Test
        @DisplayName("Replace service options values for port mappings")
        public void replaceServiceOptionValuesForPortMappings() throws JSONException, JsonProcessingException {
            //region Expected Results
            var expectedComposeFile = """
                        version: '3'
                            
                        services:
                        
                          service1:
                            image: testImage:latest
                            ports:
                              - "8080:8080"
                              - "7070:9090"
                              
                          service2:
                            image: testImage2:latest
                            ports:
                              - "3333:4444"
                              - "5050:8080"
                    """;
            //endregion

            //region Test Input
            var incomingComposeFile = DockerComposeFileParser.parseComposeFile("""
                        version: '3'
                            
                        services:
                        
                          service1:
                            image: testImage:latest
                            ports:
                              - "8080:8080"
                              - "9090:9090"
                              
                          service2:
                            image: testImage2:latest
                            ports:
                              - "4444:4444"
                              - "5050:8080"
                    """);
            var serviceOptionCategories = Arrays.asList(
                    new ServiceOptionCategory(0, "Test", Arrays.asList(
                            new ServiceOption("service1", "8080", "Port X", "",
                                    ServiceOptionType.PORT_MAPPING, "8080",
                                    ServiceOptionValueType.PORT, false, false),
                            new ServiceOption("service1", "9090", "Port Y", "",
                                    ServiceOptionType.PORT_MAPPING, "9090",
                                    ServiceOptionValueType.PORT, false, false),
                            new ServiceOption("service2", "4444", "Port Z", "",
                                    ServiceOptionType.PORT_MAPPING, "4444",
                                    ServiceOptionValueType.PORT, false, false)
                    ))
            );

            var serviceOptionValues = List.of(
                    new ServiceOptionValue("service1|8080", "8080"),
                    new ServiceOptionValue("service1|9090", "7070"),
                    new ServiceOptionValue("service2|4444", "3333"));
            //endregion

            var resultComposeFile = DockerComposeFileParser.replaceServiceOptionValuesForPortMappings(serviceOptionValues, incomingComposeFile);
            DockerComposeFileParserTestUtil.assertComposeFiles(expectedComposeFile, resultComposeFile);
        }
    }
}
