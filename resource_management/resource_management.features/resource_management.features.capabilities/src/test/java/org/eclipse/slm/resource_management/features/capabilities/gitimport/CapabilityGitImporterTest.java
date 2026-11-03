package org.eclipse.slm.resource_management.features.capabilities.gitimport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.dto.DeploymentCapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class CapabilityGitImporterTest {

    @TempDir
    Path sourceRepoDir;

    @TempDir
    Path tempRoot;

    private CapabilityGitImporter importer;
    private String repoUri;
    private Git sourceGit;

    @BeforeEach
    void setUp() throws Exception {
        byte[] fixture;
        try (InputStream in = getClass().getResourceAsStream("/gitimport/capability.json")) {
            fixture = in.readAllBytes();
        }
        sourceGit = Git.init().setDirectory(sourceRepoDir.toFile()).setInitialBranch("main").call();
        Files.write(sourceRepoDir.resolve("capability.json"), fixture);
        Files.createDirectories(sourceRepoDir.resolve("sub/dir"));
        Files.write(sourceRepoDir.resolve("sub/dir/docker.json"), fixture);
        sourceGit.add().addFilepattern(".").call();
        sourceGit.commit().setMessage("initial").setSign(false).call();
        sourceGit.tag().setName("1.0.3").call();

        importer = new CapabilityGitImporter(new ObjectMapper().findAndRegisterModules());
        importer.tempRoot = tempRoot;
        repoUri = sourceRepoDir.toUri().toString();
    }

    private void commitFile(String name, String content) throws Exception {
        Files.writeString(sourceRepoDir.resolve(name), content);
        sourceGit.add().addFilepattern(name).call();
        sourceGit.commit().setMessage("add " + name).setSign(false).call();
    }

    private HttpStatus statusOf(Runnable call) {
        var e = catchThrowableOfType(call::run, CapabilityImportException.class);
        assertThat(e).as("expected CapabilityImportException").isNotNull();
        return HttpStatus.valueOf(e.getStatusCode().value());
    }

    @Test
    void importsByBranch() {
        CapabilityDTOApi capability = importer.fetch(repoUri, "main", "capability.json");

        assertThat(capability).isInstanceOf(DeploymentCapabilityDTOApi.class);
        assertThat(capability.getName()).isEqualTo("Docker");
        assertThat(capability.getLogo()).isEqualTo("mdi-docker");
        assertThat(capability.getActions()).containsKey(ActionType.INSTALL);
        assertThat(((AwxAction) capability.getActions().get(ActionType.INSTALL)).getAwxBranch()).isEqualTo("1.0.3");
    }

    @Test
    void importsByTag() throws Exception {
        commitFile("capability.json", "{\"name\":"); // branch moves on, tag still points to the valid file

        assertThat(importer.fetch(repoUri, "1.0.3", "capability.json").getName()).isEqualTo("Docker");
    }

    @Test
    void importsFileInSubdirectory() {
        assertThat(importer.fetch(repoUri, "main", "sub/dir/docker.json").getName()).isEqualTo("Docker");
    }

    @Test
    void missingRefIs404() {
        assertThat(statusOf(() -> importer.fetch(repoUri, "nope", "capability.json"))).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void missingFileIs404() {
        assertThat(statusOf(() -> importer.fetch(repoUri, "main", "missing.json"))).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void invalidJsonIs422() throws Exception {
        commitFile("bad.json", "{\"name\":");

        assertThat(statusOf(() -> importer.fetch(repoUri, "main", "bad.json")))
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"../x.json", "/etc/passwd"})
    void rejectsPathTraversal(String path) {
        assertThat(statusOf(() -> importer.fetch(repoUri, "main", path))).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsSymlinkEscapingClone() throws Exception {
        Files.createSymbolicLink(sourceRepoDir.resolve("link.json"), Paths.get("/etc/hostname"));
        sourceGit.add().addFilepattern("link.json").call();
        sourceGit.commit().setMessage("add link").setSign(false).call();

        assertThat(statusOf(() -> importer.fetch(repoUri, "main", "link.json"))).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file:///tmp/x", "ssh://h/r.git", "git@h:r.git", "ftp://h/r"})
    void rejectsNonHttpSchemes(String url) {
        assertThat(statusOf(() -> importer.importCapability(new CapabilityGitImportRequest(url, "main", "capability.json"))))
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unreachableRepoIs502() {
        assertThat(statusOf(() -> importer.importCapability(
                new CapabilityGitImportRequest("http://127.0.0.1:9/none.git", "", ""))))
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void appliesDefaults() {
        var spyImporter = spy(importer);
        doReturn(new DeploymentCapabilityDTOApi()).when(spyImporter).fetch(any(), any(), any());

        spyImporter.importCapability(new CapabilityGitImportRequest("https://h/r.git", " ", null));

        verify(spyImporter).fetch("https://h/r.git", "main", "capability.json");
    }

    @Test
    void deletesTempDirectoryOnFailureAndSuccess() throws Exception {
        commitFile("bad.json", "{\"name\":");

        statusOf(() -> importer.fetch(repoUri, "main", "missing.json"));
        statusOf(() -> importer.fetch(repoUri, "main", "bad.json"));
        importer.fetch(repoUri, "main", "capability.json");

        try (var entries = Files.list(tempRoot)) {
            assertThat(entries).isEmpty();
        }
    }
}
