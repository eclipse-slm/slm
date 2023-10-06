package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DockerComposeFileParser - Section 'services/{service}/command'")
public class DockerComposeFileParserCommandTest {

    @Nested
    @DisplayName("Parsing")
    public class Parsing {

        @Test
        @DisplayName("Parse compose with command definition")
        public void parseCommand() throws JsonProcessingException {
            var composeFile = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        command: ls -l
                    """;

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
            var parsedCommand = parsedComposeFile.getServices().get("test-service").getCommand();
            assertThat(parsedCommand).isEqualTo("ls -l");
        }
    }

    @Nested
    @DisplayName("Service Options")
    public class ServiceOptions {

    }
}
