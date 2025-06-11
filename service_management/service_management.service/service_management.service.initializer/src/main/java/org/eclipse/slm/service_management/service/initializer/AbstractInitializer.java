package org.eclipse.slm.service_management.service.initializer;

import org.eclipse.slm.common.utils.files.FilesUtil;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.service_management.service.client.ServiceManagementApiClientInitializer;
import org.eclipse.slm.service_management.service.client.handler.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
public abstract class AbstractInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractInitializer.class);

    @Autowired
    protected ServiceManagementApiClientInitializer serviceManagementApiClientInitializer;

    @Value("#{'${service-management.init-directories}'.split(',')}")
    private String[] serviceManagementInitDirectoryPathConfigs;

    @Value("${keycloak.auth-server-url}")
    protected String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    protected String keycloakRealm;

    @Value("${keycloak.username}")
    protected String keycloakUsername;

    @Value("${keycloak.password}")
    protected String keycloakPassword;

    protected ApiClient apiClient;

    @PostConstruct
    private void initApiClient() {
        var keycloakAccessToken = KeycloakTokenUtil.getAccessTokenFromKeycloakInstance(
                this.keycloakAuthServerUrl, this.keycloakRealm, this.keycloakUsername, this.keycloakPassword);
        this.apiClient = serviceManagementApiClientInitializer.init(keycloakAccessToken);
    }

    protected List<String> getInitDirectories() {
        var initDirectories = new ArrayList<String>();
        if (serviceManagementInitDirectoryPathConfigs[0].isEmpty()) {
            return initDirectories;
        }

        for (var serviceManagementInitDirectoryPathConfig : this.serviceManagementInitDirectoryPathConfigs) {
            var serviceManagementInitDirectory = "";
            if (!serviceManagementInitDirectoryPathConfig.startsWith("/") && !serviceManagementInitDirectoryPathConfig.startsWith(":", 1)) {
                // Convert relative path to absolut path
                serviceManagementInitDirectory = FilesUtil.getExecutionPath(this) + serviceManagementInitDirectoryPathConfig;
            } else {
                // Absolut path
                serviceManagementInitDirectory = serviceManagementInitDirectoryPathConfig;
            }
            if (!serviceManagementInitDirectory.endsWith("/")) {
                serviceManagementInitDirectory += "/";
            }

            if (Files.exists(FilesUtil.pathOf(serviceManagementInitDirectory))) {
                initDirectories.add(serviceManagementInitDirectory);
            }
            else {
                var errorMessage = "Init directory '" + serviceManagementInitDirectoryPathConfig + "' not found --> Skipping directory";
                LOG.error(errorMessage);
            }
        }

        return initDirectories;
    }
}
