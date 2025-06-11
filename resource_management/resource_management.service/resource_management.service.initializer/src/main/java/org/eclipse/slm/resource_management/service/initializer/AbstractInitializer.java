package org.eclipse.slm.resource_management.service.initializer;

import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.resource_management.service.client.ResourceManagementApiClientInitializer;
import org.eclipse.slm.resource_management.service.client.handler.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;;
import java.io.FileNotFoundException;
import java.nio.file.Files;

@Component
public abstract class AbstractInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractInitializer.class);

    protected final FileUtil fileUtil;

    protected final ResourceManagementApiClientInitializer resourceManagementApiClientInitializer;

    @Value("${resource-management.init-directories:init/}")
    private String resourceManagementInitDirectoryPathConfig;

    @Value("${keycloak.auth-server-url}")
    protected String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    protected String keycloakRealm;

    @Value("${keycloak.username}")
    protected String keycloakUsername;

    @Value("${keycloak.password}")
    protected String keycloakPassword;

    protected ApiClient apiClient;

    public AbstractInitializer(FileUtil fileUtil, ResourceManagementApiClientInitializer resourceManagementApiClientInitializer) {
        this.fileUtil = fileUtil;
        this.resourceManagementApiClientInitializer = resourceManagementApiClientInitializer;
    }

    @PostConstruct
    private void initApiClient() {
        var keycloakAccessToken = KeycloakTokenUtil.getAccessTokenFromKeycloakInstance(
                this.keycloakAuthServerUrl,
                this.keycloakRealm,
                this.keycloakUsername,
                this.keycloakPassword
        );
        this.apiClient = resourceManagementApiClientInitializer.init(keycloakAccessToken);
    }

    protected String getInitDirectory() throws FileNotFoundException {
        var resourceManagementInitDirectory = "";
        if (!this.resourceManagementInitDirectoryPathConfig.startsWith("/") && !this.resourceManagementInitDirectoryPathConfig.startsWith(":", 1)) {
            // Convert relative path to absolut path
            resourceManagementInitDirectory = FileUtil.getExecutionPath(this) + this.resourceManagementInitDirectoryPathConfig;
        }
        else {
            // Absolut path
            resourceManagementInitDirectory = this.resourceManagementInitDirectoryPathConfig;
        }
        if (!resourceManagementInitDirectory.endsWith("/")) {
            resourceManagementInitDirectory += "/";
        }

        if (!Files.exists(FileUtil.pathOf(resourceManagementInitDirectory))) {
            var errorMessage = "Init directory '" + this.resourceManagementInitDirectoryPathConfig + "' not found";
            LOG.error(errorMessage);
            throw new FileNotFoundException(errorMessage);
        }

        return resourceManagementInitDirectory;
    }
}
