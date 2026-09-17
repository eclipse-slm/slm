package org.eclipse.slm.resource_management.features.capabilities.aas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.eclipse.slm.common.aas.submodels.deployment.DeployRequest;
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Baut aus einem DeployRequest die ExtraVars, die das Deploy-Playbook der Capability erwartet.
 * Einzige Stelle, an der die Playbook-Schnittstelle definiert ist.
 */
@Component
public class DeploymentExtraVarsBuilder {

    private final YAMLMapper yamlMapper = new YAMLMapper();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public Map<String, Object> build(DeployRequest request,
                                     UUID capabilityServiceId,
                                     String capabilityServiceName,
                                     Set<ConnectionType> supportedConnectionTypes,
                                     String keycloakToken) {

        var extraVars = new HashMap<String, Object>();
        extraVars.put("service_id", request.serviceInstanceId());
        extraVars.put("keycloak_token", keycloakToken);
        extraVars.put("service_name", capabilityServiceName);
        extraVars.put("supported_connection_types", supportedConnectionTypes);

        var descriptorText = new String(request.descriptor(), StandardCharsets.UTF_8);

        switch (request.deploymentType()) {
            case DOCKER_CONTAINER, DOCKER_COMPOSE -> {
                try {
                    extraVars.put("docker_compose_file", yamlMapper.readValue(descriptorText, Map.class));
                } catch (IOException e) {
                    throw new IllegalArgumentException("Deployment descriptor is not valid YAML", e);
                }
            }
            case KUBERNETES -> {
                extraVars.put("resource_id", capabilityServiceId);
                extraVars.put("manifest_file", descriptorText);
            }
            case CODESYS -> {
                try {
                    var payload = jsonMapper.readValue(descriptorText, Map.class);
                    extraVars.put("application_path", payload.get("applicationPath"));
                } catch (IOException e) {
                    throw new IllegalArgumentException("Deployment descriptor is not valid JSON", e);
                }
            }
            default -> throw new IllegalArgumentException(
                    "Deployment type '" + request.deploymentType() + "' is not supported");
        }

        if (!request.credentialReferences().isEmpty()) {
            extraVars.put("docker_registries_vault_paths", request.credentialReferences());
        }

        return extraVars;
    }
}
