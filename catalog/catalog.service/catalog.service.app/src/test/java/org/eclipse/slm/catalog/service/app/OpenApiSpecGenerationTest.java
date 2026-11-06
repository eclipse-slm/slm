package org.eclipse.slm.catalog.service.app;

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
        assertEquals("Service Lifecycle Management Catalog", document.path("info").path("title").asText());
        assertTrue(document.path("paths").has("/catalog/aas/submodels/templates"));
        OpenApiSpecGenerator.writeYaml(Path.of("../catalog.service.client/src/main/resources/api-docs-for-ui.yaml"), Files.readString(generated));
    }
}
