package org.eclipse.slm.resource_management.features.capabilities.gitimport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityDTOApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Reads a capability definition file from a git repository via a shallow clone into a temporary directory.
 */
@Service
public class CapabilityGitImporter {

    private static final Logger LOG = LoggerFactory.getLogger(CapabilityGitImporter.class);

    static final String DEFAULT_REF = "main";
    static final String DEFAULT_FILE_PATH = "capability.json";
    private static final int TIMEOUT_SECONDS = 30;
    private static final long MAX_FILE_SIZE_BYTES = 1024 * 1024;
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    private final ObjectMapper objectMapper;

    // Package-private so tests can check that clones are cleaned up
    Path tempRoot = Paths.get(System.getProperty("java.io.tmpdir"));

    public CapabilityGitImporter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CapabilityDTOApi importCapability(CapabilityGitImportRequest request) {
        var repositoryUrl = request.repositoryUrl() == null ? "" : request.repositoryUrl().trim();
        validateRepositoryUrl(repositoryUrl);
        return fetch(repositoryUrl, orDefault(request.branch(), DEFAULT_REF), orDefault(request.filePath(), DEFAULT_FILE_PATH));
    }

    // No scheme check here, so tests can read from local file:// repositories
    CapabilityDTOApi fetch(String repositoryUrl, String ref, String filePath) {
        var relativePath = validateRelativePath(filePath);
        var fullRef = resolveRef(repositoryUrl, ref);

        Path cloneDir;
        try {
            cloneDir = Files.createTempDirectory(tempRoot, "capability-import-");
        } catch (IOException e) {
            throw new CapabilityImportException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create clone directory", e);
        }

        try {
            try (Git ignored = Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(cloneDir.toFile())
                    .setBranch(fullRef)
                    .setBranchesToClone(List.of(fullRef))
                    .setDepth(1)
                    .setTimeout(TIMEOUT_SECONDS)
                    .call()) {
                // only the working tree is needed
            } catch (GitAPIException e) {
                throw new CapabilityImportException(HttpStatus.BAD_GATEWAY,
                        "Failed to clone repository '" + repositoryUrl + "': " + e.getMessage(), e);
            }
            return readCapability(cloneDir, relativePath, filePath);
        } finally {
            deleteRecursively(cloneDir);
        }
    }

    private static void validateRepositoryUrl(String repositoryUrl) {
        URI uri;
        try {
            uri = new URI(repositoryUrl);
        } catch (URISyntaxException e) {
            throw new CapabilityImportException(HttpStatus.BAD_REQUEST, "Invalid repository URL '" + repositoryUrl + "'");
        }
        var scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        if (!ALLOWED_SCHEMES.contains(scheme) || uri.getHost() == null || uri.getHost().isBlank()) {
            throw new CapabilityImportException(HttpStatus.BAD_REQUEST,
                    "Repository URL must start with http:// or https:// and contain a host");
        }
    }

    private static Path validateRelativePath(String filePath) {
        Path path;
        try {
            path = Paths.get(filePath).normalize();
        } catch (InvalidPathException e) {
            throw new CapabilityImportException(HttpStatus.BAD_REQUEST, "Invalid file path '" + filePath + "'");
        }
        if (path.isAbsolute() || path.toString().isEmpty() || path.startsWith("..")) {
            throw new CapabilityImportException(HttpStatus.BAD_REQUEST,
                    "File path '" + filePath + "' must be relative to the repository root");
        }
        return path;
    }

    private static String resolveRef(String repositoryUrl, String ref) {
        Collection<Ref> remoteRefs;
        try {
            remoteRefs = Git.lsRemoteRepository()
                    .setRemote(repositoryUrl)
                    .setHeads(true)
                    .setTags(true)
                    .setTimeout(TIMEOUT_SECONDS)
                    .call();
        } catch (GitAPIException e) {
            throw new CapabilityImportException(HttpStatus.BAD_GATEWAY,
                    "Failed to access repository '" + repositoryUrl + "': " + e.getMessage(), e);
        }
        for (var candidate : List.of(Constants.R_HEADS + ref, Constants.R_TAGS + ref)) {
            if (remoteRefs.stream().anyMatch(r -> r.getName().equals(candidate))) {
                return candidate;
            }
        }
        throw new CapabilityImportException(HttpStatus.NOT_FOUND, "Branch or tag '" + ref + "' not found");
    }

    private CapabilityDTOApi readCapability(Path cloneDir, Path relativePath, String filePath) {
        var file = cloneDir.resolve(relativePath);
        if (!Files.exists(file)) {
            throw new CapabilityImportException(HttpStatus.NOT_FOUND, "File '" + filePath + "' not found");
        }
        Path realFile;
        try {
            realFile = file.toRealPath();
            if (!realFile.startsWith(cloneDir.toRealPath())) {
                throw new CapabilityImportException(HttpStatus.BAD_REQUEST,
                        "File path '" + filePath + "' points outside the repository");
            }
        } catch (IOException e) {
            throw new CapabilityImportException(HttpStatus.NOT_FOUND, "File '" + filePath + "' not found", e);
        }
        if (!Files.isRegularFile(realFile)) {
            throw new CapabilityImportException(HttpStatus.NOT_FOUND, "File '" + filePath + "' not found");
        }

        try {
            if (Files.size(realFile) > MAX_FILE_SIZE_BYTES) {
                throw new CapabilityImportException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "File '" + filePath + "' is larger than " + MAX_FILE_SIZE_BYTES / 1024 + " KiB");
            }
            var capability = objectMapper.readValue(Files.readAllBytes(realFile), CapabilityDTOApi.class);
            if (capability == null) {
                throw new CapabilityImportException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "File '" + filePath + "' does not contain a capability definition");
            }
            return capability;
        } catch (IOException e) {
            throw new CapabilityImportException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "File '" + filePath + "' is not a valid capability definition: " + e.getMessage(), e);
        }
    }

    private static void deleteRecursively(Path dir) {
        try {
            FileUtils.delete(dir.toFile(), FileUtils.RECURSIVE | FileUtils.RETRY | FileUtils.SKIP_MISSING);
        } catch (IOException e) {
            LOG.warn("Failed to delete clone directory '{}'", dir, e);
        }
    }

    private static String orDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
