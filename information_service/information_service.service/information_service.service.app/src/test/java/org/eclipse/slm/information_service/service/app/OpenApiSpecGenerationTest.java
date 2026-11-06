package org.eclipse.slm.information_service.service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.eclipse.slm.common.openapi.test.OpenApiSpecGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfSystemProperty(named = "generateOpenApiSpec", matches = "true")
class OpenApiSpecGenerationTest {
    @Test
    void generateOpenApiSpec(@TempDir Path temp) throws Exception {
        Path generated = temp.resolve("api.yaml");
        OpenApiSpecGenerator.generate(Application.class, generated);
        var document = new ObjectMapper(new YAMLFactory()).readTree(generated.toFile());
        assertEquals("Information Service", document.path("info").path("title").asText());
        // This service exposes no REST controllers yet. Replace this assertion when the first endpoint is added.
        assertTrue(document.path("paths").isObject(), "paths must be a mapping");
        assertTrue(document.path("paths").isEmpty(), "no endpoints are currently exposed");
        OpenApiSpecGenerator.writeYaml(Path.of("../information_service.service.client/src/main/resources/api-docs-for-ui.yaml"), Files.readString(generated));
    }
}
