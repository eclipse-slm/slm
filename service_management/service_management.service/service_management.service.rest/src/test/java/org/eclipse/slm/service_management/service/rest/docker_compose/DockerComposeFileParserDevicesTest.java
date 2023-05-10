package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DockerComposeFileParser - Section 'services/{service}/devices'")
public class DockerComposeFileParserDevicesTest {

    @Nested
    @DisplayName("Parsing")
    public class Parsing {

        @Test
        @DisplayName("Parse compose with devices definition")
        public void parseLabelsDefinedAsArray() throws JsonProcessingException {
            var composeFile = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        devices:
                          - /dev/device1
                          - /dev/device2
                    """;

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
            var parsedDevices = parsedComposeFile.getServices().get("test-service").getDevices();
            assertThat(parsedDevices)
                    .contains("/dev/device1")
                    .contains("/dev/device2");
        }
    }

    @Nested
    @DisplayName("Service Options")
    public class ServiceOptions {

    }
}
