package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DockerComposeFileParser - Section 'services/{service}/network_mode'")
public class DockerComposeFileParserNetworkModeTest {

    @Nested
    @DisplayName("Parsing")
    public class Parsing {

        @Test
        @DisplayName("Parse compose with network mode")
        public void parseLabelsDefinedAsArray() throws JsonProcessingException {
            var composeFile = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        network_mode: host
                    """;

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
            var parsedNetworkMode = parsedComposeFile.getServices().get("test-service").getNetworkMode();
            assertThat(parsedNetworkMode).isEqualTo("host");
        }
    }
}
