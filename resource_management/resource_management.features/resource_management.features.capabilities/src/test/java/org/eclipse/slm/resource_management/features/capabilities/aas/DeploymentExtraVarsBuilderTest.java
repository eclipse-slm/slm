package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.slm.common.aas.submodels.deployment.DeployRequest;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentExtraVarsBuilderTest {

    private static final UUID SERVICE_INSTANCE_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID CAPABILITY_SERVICE_ID = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");
    private static final String SERVICE_NAME = "docker_bbbbbbbb-0000-0000-0000-000000000002";
    private static final Set<ConnectionType> CONNECTION_TYPES = Set.of(ConnectionType.ssh);

    private DeployRequest composeRequest(List<String> credentialReferences) {
        return new DeployRequest(SERVICE_INSTANCE_ID, DeploymentType.DOCKER_COMPOSE,
                "services:\n  web:\n    image: nginx\n".getBytes(StandardCharsets.UTF_8),
                "application/yaml", credentialReferences);
    }

    @Test
    @DisplayName("Docker Compose deployment produces exactly the keys the playbook expects")
    void dockerComposeProducesExpectedKeys() {
        var builder = new DeploymentExtraVarsBuilder();

        var extraVars = builder.build(composeRequest(List.of()),
                CAPABILITY_SERVICE_ID, SERVICE_NAME, CONNECTION_TYPES, "the-token");

        assertThat(extraVars).containsOnlyKeys(
                "service_id", "keycloak_token", "service_name",
                "supported_connection_types", "docker_compose_file");
        assertThat(extraVars.get("service_id")).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(extraVars.get("service_name")).isEqualTo(SERVICE_NAME);
        assertThat(extraVars.get("keycloak_token")).isEqualTo("the-token");
        assertThat(extraVars.get("supported_connection_types")).isEqualTo(CONNECTION_TYPES);
    }

    @Test
    @DisplayName("The compose descriptor reaches the playbook as a structure, not as raw text")
    void composeDescriptorIsPassedAsStructure() {
        var builder = new DeploymentExtraVarsBuilder();

        var extraVars = builder.build(composeRequest(List.of()),
                CAPABILITY_SERVICE_ID, SERVICE_NAME, CONNECTION_TYPES, "the-token");

        assertThat(extraVars.get("docker_compose_file")).isInstanceOf(java.util.Map.class);
        @SuppressWarnings("unchecked")
        var composeFile = (java.util.Map<String, Object>) extraVars.get("docker_compose_file");
        assertThat(composeFile).containsKey("services");
    }

    @Test
    @DisplayName("Credential references are passed as vault paths under the established key")
    void credentialReferencesArePassedAsVaultPaths() {
        var builder = new DeploymentExtraVarsBuilder();

        var extraVars = builder.build(composeRequest(List.of("vendor_1/registry-a")),
                CAPABILITY_SERVICE_ID, SERVICE_NAME, CONNECTION_TYPES, "the-token");

        assertThat(extraVars.get("docker_registries_vault_paths"))
                .isEqualTo(List.of("vendor_1/registry-a"));
    }

    @Test
    @DisplayName("Kubernetes deployment carries the resource id and the manifest")
    void kubernetesCarriesResourceIdAndManifest() {
        var builder = new DeploymentExtraVarsBuilder();
        var request = new DeployRequest(SERVICE_INSTANCE_ID, DeploymentType.KUBERNETES,
                "kind: Deployment\n".getBytes(StandardCharsets.UTF_8), "application/yaml", List.of());

        var extraVars = builder.build(request, CAPABILITY_SERVICE_ID, SERVICE_NAME, CONNECTION_TYPES, "t");

        assertThat(extraVars).containsOnlyKeys(
                "resource_id", "service_id", "keycloak_token", "service_name",
                "supported_connection_types", "manifest_file");
        assertThat(extraVars.get("resource_id")).isEqualTo(CAPABILITY_SERVICE_ID);
        assertThat(extraVars.get("manifest_file")).isEqualTo("kind: Deployment\n");
    }

    @Test
    @DisplayName("Codesys deployment carries the application path taken from the descriptor")
    void codesysCarriesApplicationPath() {
        var builder = new DeploymentExtraVarsBuilder();
        var request = new DeployRequest(SERVICE_INSTANCE_ID, DeploymentType.CODESYS,
                "{\"applicationPath\":\"/app/plc\"}".getBytes(StandardCharsets.UTF_8),
                "application/json", List.of());

        var extraVars = builder.build(request, CAPABILITY_SERVICE_ID, SERVICE_NAME, CONNECTION_TYPES, "t");

        assertThat(extraVars.get("application_path")).isEqualTo("/app/plc");
    }

    @Test
    @DisplayName("Compose volumes reach the playbook in the same short-syntax shape AWX receives today")
    void composeVolumesUseShortSyntaxShape() {
        var builder = new DeploymentExtraVarsBuilder();
        var request = new DeployRequest(SERVICE_INSTANCE_ID, DeploymentType.DOCKER_COMPOSE,
                ("services:\n" +
                        "  web:\n" +
                        "    image: nginx\n" +
                        "    volumes:\n" +
                        "      - \"data-vol:/var/lib/data\"\n").getBytes(StandardCharsets.UTF_8),
                "application/yaml", List.of());

        var extraVars = builder.build(request, CAPABILITY_SERVICE_ID, SERVICE_NAME, CONNECTION_TYPES, "t");

        @SuppressWarnings("unchecked")
        var composeFile = (java.util.Map<String, Object>) extraVars.get("docker_compose_file");
        @SuppressWarnings("unchecked")
        var services = (java.util.Map<String, Object>) composeFile.get("services");
        @SuppressWarnings("unchecked")
        var web = (java.util.Map<String, Object>) services.get("web");
        assertThat(web.get("volumes")).isEqualTo(List.of("data-vol:/var/lib/data"));
    }
}
