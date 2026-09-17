package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.ServiceOffering;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.docker.compose.DockerComposeDeploymentDefinition;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOfferingVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeploymentDescriptorRendererTest {

    private static final String COMPOSE_YAML = """
            services:
              web:
                image: nginx:1.25
                ports:
                  - "8080:80"
            """;

    private ServiceOfferingVersion dockerComposeOfferingVersion() {
        var serviceOffering = new ServiceOffering();
        serviceOffering.setId(UUID.randomUUID());
        serviceOffering.setName("Demo Service");

        var deploymentDefinition = new DockerComposeDeploymentDefinition();
        deploymentDefinition.setComposeFile(COMPOSE_YAML);

        var version = new ServiceOfferingVersion();
        version.setId(UUID.randomUUID());
        version.setVersion("1.0.0");
        version.setServiceOffering(serviceOffering);
        version.setDeploymentDefinition(deploymentDefinition);
        version.setServicePorts(new ArrayList<>());
        version.setServiceRepositories(new ArrayList<>());
        return version;
    }

    @Test
    @DisplayName("Docker Compose offering renders to a YAML descriptor")
    void dockerComposeRendersToYaml() throws Exception {
        var renderer = new DeploymentDescriptorRenderer();
        var order = new ServiceOrder();
        order.setServiceOptionValues(new ArrayList<>());

        var rendered = renderer.render(dockerComposeOfferingVersion(), order);

        assertThat(rendered.contentType()).isEqualTo("application/yaml");
        assertThat(new String(rendered.content(), StandardCharsets.UTF_8)).contains("nginx:1.25");
    }

    @Test
    @DisplayName("Docker Compose offering exposes the published ports it declares")
    void dockerComposeExposesDeclaredPorts() throws Exception {
        var renderer = new DeploymentDescriptorRenderer();
        var order = new ServiceOrder();
        order.setServiceOptionValues(new ArrayList<>());

        var rendered = renderer.render(dockerComposeOfferingVersion(), order);

        assertThat(rendered.servicePorts()).contains(8080);
    }

    @Test
    @DisplayName("An unsupported deployment type is refused instead of rendering nothing")
    void unsupportedDeploymentTypeIsRefused() {
        var renderer = new DeploymentDescriptorRenderer();
        var version = dockerComposeOfferingVersion();
        version.setDeploymentDefinition(null);
        var order = new ServiceOrder();
        order.setServiceOptionValues(new ArrayList<>());

        assertThatThrownBy(() -> renderer.render(version, order))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
