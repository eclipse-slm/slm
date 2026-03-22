package org.eclipse.slm.service_management.service.app.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DockerComposeFileParser - Section 'version'")
public class DockerComposeFileParserNoVersionTest {

    @Nested
    @DisplayName("Parsing")
    public class Parsing {

        @Test
        @DisplayName("Parse compose without version definition")
        public void parseWithoutVersion() throws JsonProcessingException {
            var composeFile = """    
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                    """;

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
        }
    }
}
