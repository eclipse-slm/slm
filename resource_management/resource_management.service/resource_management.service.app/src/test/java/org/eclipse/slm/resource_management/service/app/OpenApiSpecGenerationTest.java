package org.eclipse.slm.resource_management.service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.eclipse.slm.common.openapi.test.OpenApiSpecGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfSystemProperty(named = "generateOpenApiSpec", matches = "true")
class OpenApiSpecGenerationTest {
    @Test
    void generateOpenApiSpec(@TempDir Path temp) throws Exception {
        Path generated = temp.resolve("api.yaml");
        OpenApiSpecGenerator.generate(Application.class, generated);
        var paths = new ObjectMapper(new YAMLFactory()).readTree(generated.toFile()).path("paths");
        assertTrue(paths.has("/resources"));
        assertTrue(paths.has("/resources/profiler"));
        OpenApiSpecGenerator.writeYaml(Path.of("../resource_management.service.client/src/main/resources/api-docs-for-ui.yaml"), Files.readString(generated));
    }
}
