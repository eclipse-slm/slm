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

@DisplayName("DockerComposeFileParser - Section 'services/{service}/extra_hosts'")
public class DockerComposeFileParserExtraHostsTest {

    @Nested
    @DisplayName("Parsing")
    public class Parsing {

        @Test
        @DisplayName("Parse compose with extra_hosts definition")
        public void parseLabelsDefinedAsArray() throws JsonProcessingException {
            var composeFile = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        extra_hosts:
                          - "somehost:162.242.195.82"
                          - "otherhost:50.31.209.229"
                    """;

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
            var parsedExtraHosts = parsedComposeFile.getServices().get("test-service").getExtraHosts();
            assertThat(parsedExtraHosts)
                    .contains("somehost:162.242.195.82")
                    .contains("otherhost:50.31.209.229");
        }
    }

    @Nested
    @DisplayName("Service Options")
    public class ServiceOptions {

    }
}
