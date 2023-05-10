package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionValue;
import org.eclipse.slm.service_management.service.rest.docker_compose.DockerComposeFileLabels;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DockerComposeFileParser - Section 'services/{service}/labels'")
public class DockerComposeFileParserLabelsTest {

    @Nested
    @DisplayName("Constructors")
    public class Constructors {
        @Test
        @DisplayName("Create from existing labels map")
        public void withExistingEnvironmentMap() {
            var expectedLabels = new DockerComposeFileLabels();
            expectedLabels.put("label1", "labelVal1");
            expectedLabels.put("label2", true);

            var existingLabelsMap = Map.of("label1", "labelVal1", "label2", true);
            var labels = new DockerComposeFileLabels(existingLabelsMap);

            assertThat(labels).usingRecursiveComparison()
                    .isEqualTo(expectedLabels);
        }
    }

    @Nested
    @DisplayName("Parsing")
    public class Parsing {

        @Test
        @DisplayName("Parse labels defined as map")
        public void parseLabelsDefinedAsMap() throws JsonProcessingException {
            var composeFile = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        labels:
                          Label1: LabelVal1
                          Label2: 1
                          Label3: true
                    """;

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
            var parsedLabels = parsedComposeFile.getServices().get("test-service").getLabels();
            assertThat(parsedLabels)
                    .containsEntry("Label1", "LabelVal1")
                    .containsEntry("Label2", "1")
                    .containsEntry("Label3", "true");
        }

        @Test
        @DisplayName("Parse labels defined as array")
        public void parseLabelsDefinedAsArray() throws JsonProcessingException {
            var composeFile = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        labels:
                          - "Label1=LabelVal1"
                          - "Label2=1"
                          - "Label3=true"
                    """;

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
            var parsedLabels = parsedComposeFile.getServices().get("test-service").getLabels();
            assertThat(parsedLabels)
                    .containsEntry("Label1", "LabelVal1")
                    .containsEntry("Label2", "1")
                    .containsEntry("Label3", "true");
        }
    }

    @Nested
    @DisplayName("Service Options")
    public class ServiceOptions {
        @Test
        @DisplayName("Replace service option values for labels")
        public void replaceServiceOptionValuesForLabels() throws JSONException, JsonProcessingException {
            //region Expected Results
            var expectedComposeFile = """
                        version: '3'
                            
                        services:
                        
                          service1:
                            image: testImage:latest
                            labels:
                              label1: val1
                              label2: service1LabelVal2
                              label3: val3
                              
                          service2:
                            image: testImage2:latest
                            labels:
                              label1: service2LabelVal1
                              label2: val2
                              label3: service2LabelVal3
                    """;
            //endregion

            //region Test Input
            var incomingComposeFile = DockerComposeFileParser.parseComposeFile("""
                        version: '3'
                            
                        services:
                        
                          service1:
                            image: testImage:latest
                            labels:
                              label1: service1LabelVal1
                              label2: service1LabelVal2
                              label3: service1LabelVal3
                              
                          service2:
                            image: testImage2:latest
                            labels:
                              - "label1=service2LabelVal1"
                              - "label2=service2LabelVal2"
                              - "label3=service2LabelVal3"
                    """);

            var serviceOptionValues = List.of(
                    new ServiceOptionValue("service1|label1", "val1"),
                    new ServiceOptionValue("service1|label3", "val3"),
                    new ServiceOptionValue("service2|label2", "val2"));
            //endregion

            var resultComposeFile = DockerComposeFileParser.replaceServiceOptionValuesForLabels(serviceOptionValues, incomingComposeFile);
            DockerComposeFileParserTestUtil.assertComposeFiles(expectedComposeFile, resultComposeFile);
        }
    }
}
