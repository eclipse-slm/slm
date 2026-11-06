package org.eclipse.slm.common.openapi.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiSpecGeneratorTest {
    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    @TempDir
    Path directory;

    @SpringBootApplication(scanBasePackages = {"org.eclipse.slm.common.openapi.test.fixtures", "org.eclipse.slm.common.openapi.test.shared"})
    static class FixtureApplication { }

    @SpringBootApplication(scanBasePackages = "org.eclipse.slm.common.openapi.test.no_controllers")
    static class EmptyApplication { }

    @Test
    void generatesOnlyControllersInApplicationScanPackages() throws Exception {
        Path output = directory.resolve("api.yaml");
        OpenApiSpecGenerator.generate(FixtureApplication.class, output);
        JsonNode document = YAML.readTree(output.toFile());
        assertTrue(document.path("openapi").isTextual());
        assertTrue(document.path("info").isObject());
        assertEquals("Fixture API", document.path("info").path("title").asText());
        assertEquals("http://localhost:9000/fixture-service", document.path("servers").get(0).path("url").asText());
        assertTrue(document.path("paths").has("/fixture"));
        assertTrue(document.path("paths").has("/shared"));
        assertFalse(document.path("paths").has("/outside"));
    }

    @Test
    void generatesValidDocumentWithoutControllers() throws Exception {
        Path output = directory.resolve("empty.yaml");
        OpenApiSpecGenerator.generate(EmptyApplication.class, output);
        JsonNode document = YAML.readTree(output.toFile());
        assertTrue(document.path("openapi").isTextual());
        assertTrue(document.path("paths").isMissingNode() || document.path("paths").isEmpty());
    }

    @Test
    void invalidResponsesDoNotReplaceExistingDocument() throws Exception {
        Path output = directory.resolve("api.yaml");
        Files.writeString(output, "original");
        assertThrows(Exception.class, () -> OpenApiSpecGenerator.writeYaml(output, ""));
        assertThrows(Exception.class, () -> OpenApiSpecGenerator.writeYaml(output, "openapi: ["));
        assertThrows(Exception.class, () -> OpenApiSpecGenerator.writeYaml(output, "openapi: 3.0.1"));
        assertThrows(Exception.class, () -> OpenApiSpecGenerator.writeYaml(output,
                "openapi: not-a-version\ninfo: {title: test, version: '1'}\npaths: {}\n"));
        assertThrows(Exception.class, () -> OpenApiSpecGenerator.writeYaml(output,
                "openapi: 3.0.1\ninfo: {title: '', version: '1'}\npaths: {}\n"));
        assertThrows(Exception.class, () -> OpenApiSpecGenerator.writeYaml(output,
                "openapi: 3.0.1\ninfo: {title: test, version: '1'}\n"));
        assertEquals("original", Files.readString(output));
    }

    @Test
    void writeFailureDoesNotReplaceExistingDocument() throws Exception {
        Path output = directory.resolve("existing-directory");
        Files.createDirectory(output);
        Path existing = output.resolve("keep");
        Files.writeString(existing, "original");
        assertThrows(Exception.class, () -> OpenApiSpecGenerator.writeYaml(output,
                "openapi: 3.0.1\ninfo:\n  title: test\n  version: 1\npaths: {}\n"));
        assertEquals("original", Files.readString(existing));
    }

    @Test
    void writesIdenticalBytesForUnorderedResponseMaps() throws Exception {
        String header = "openapi: 3.0.1\ninfo: {title: test, version: '1'}\npaths:\n  /x:\n    get:\n      responses:\n";
        String first = header + "        '404': {description: missing}\n        '200': {description: ok}\n"
                + "        '403': {description: forbidden}\n        '401': {description: unauthorized}\n"
                + "        '400': {description: bad}\n        default: {description: fallback}\n";
        String second = header + "        '200': {description: ok}\n        '400': {description: bad}\n"
                + "        '401': {description: unauthorized}\n        '403': {description: forbidden}\n"
                + "        '404': {description: missing}\n        default: {description: fallback}\n";

        Path firstOutput = directory.resolve("first.yaml");
        Path secondOutput = directory.resolve("second.yaml");
        OpenApiSpecGenerator.writeYaml(firstOutput, first);
        OpenApiSpecGenerator.writeYaml(secondOutput, second);

        assertEquals(Files.readString(firstOutput), Files.readString(secondOutput));
        var responses = YAML.readTree(firstOutput.toFile()).path("paths").path("/x").path("get").path("responses");
        var statuses = new java.util.ArrayList<String>();
        responses.fieldNames().forEachRemaining(statuses::add);
        assertEquals(java.util.List.of("200", "400", "401", "403", "404", "default"), statuses);
    }
}
