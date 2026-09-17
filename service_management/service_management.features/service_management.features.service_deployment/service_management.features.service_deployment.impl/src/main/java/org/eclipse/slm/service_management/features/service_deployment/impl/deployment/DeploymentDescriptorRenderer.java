package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder;
import org.eclipse.slm.service_management.features.service_deployment.api.deploymentdefinitions.dockercompose.DockerComposeFile;
import org.eclipse.slm.service_management.features.service_deployment.api.deploymentdefinitions.dockercompose.DockerComposeFileParser;
import org.eclipse.slm.service_management.features.service_deployment.api.deploymentdefinitions.kubernetes.KubernetesManifestFile;
import org.eclipse.slm.service_management.features.service_deployment.api.deploymentdefinitions.kubernetes.KubernetesManifestFileParser;
import org.eclipse.slm.service_management.features.service_deployment.impl.deployment.dockercontainer.DockerContainerServiceOfferingOrderUtil;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.codesys.CodesysDeploymentDefinition;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.docker.compose.DockerComposeDeploymentDefinition;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.docker.container.DockerContainerDeploymentDefinition;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOfferingVersion;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOptionNotFoundException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rendert eine Service Offering Version samt gewaehlter Optionen zu der Nutzlast,
 * die das Deployment-Ziel entgegennimmt. Kennt weder AWX noch die AAS.
 */
@Component
public class DeploymentDescriptorRenderer {

    public static final String CONTENT_TYPE_YAML = "application/yaml";
    public static final String CONTENT_TYPE_JSON = "application/json";

    private final YAMLMapper yamlMapper = new YAMLMapper();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public RenderedDescriptor render(ServiceOfferingVersion serviceOfferingVersion, ServiceOrder serviceOrder)
            throws JsonProcessingException, ServiceOptionNotFoundException, InvalidServiceOfferingDefinitionException {

        var deploymentType = serviceOfferingVersion.getDeploymentType();
        if (deploymentType == null) {
            throw new IllegalArgumentException("Service offering version '"
                    + serviceOfferingVersion.getId() + "' has no deployment type");
        }

        switch (deploymentType) {
            case DOCKER_CONTAINER, DOCKER_COMPOSE -> {
                var composeFile = this.getDeployableComposeFile(serviceOfferingVersion, serviceOrder);
                var composeYamlMap = DockerComposeFileParser.composeFileToYAML(composeFile);
                var content = yamlMapper.writeValueAsString(composeYamlMap).getBytes(StandardCharsets.UTF_8);

                var servicePorts = this.getServicePorts(serviceOfferingVersion, composeFile);

                return new RenderedDescriptor(content, CONTENT_TYPE_YAML,
                        this.getServiceMetaData(serviceOfferingVersion, composeFile), servicePorts);
            }
            case KUBERNETES -> {
                var manifestFile = this.getDeployableManifestFile(serviceOfferingVersion, serviceOrder);
                var content = KubernetesManifestFileParser.manifestFinalizer(manifestFile)
                        .getBytes(StandardCharsets.UTF_8);

                return new RenderedDescriptor(content, CONTENT_TYPE_YAML,
                        new HashMap<>(), new ArrayList<>());
            }
            case CODESYS -> {
                var codesysDefinition =
                        (CodesysDeploymentDefinition) serviceOfferingVersion.getDeploymentDefinition();
                Map<String, String> payload =
                        Map.of("applicationPath", codesysDefinition.getApplicationPath());
                var content = jsonMapper.writeValueAsBytes(payload);

                return new RenderedDescriptor(content, CONTENT_TYPE_JSON,
                        new HashMap<>(), new ArrayList<>());
            }
            default -> throw new IllegalArgumentException(
                    "Deployment type '" + deploymentType + "' is not supported");
        }
    }

    // Die folgenden vier Methoden sind unveraendert aus ServiceDeploymentHandler uebernommen
    // (dort Zeilen 181-284). Sichtbarkeit ist package-private statt private, damit
    // ServiceDeploymentHandler beim Aufbau der AWX-ExtraVars weiterhin die rohen
    // DockerComposeFile/KubernetesManifestFile-Objekte erhalten kann (Objekt-Pfad bleibt
    // fuer AWX vorerst unveraendert, siehe Task 6).

    Map<String, String> getServiceMetaData(ServiceOfferingVersion serviceOfferingVersion,
                                            DockerComposeFile deployableComposeFile) {
        var serviceMetaData = new HashMap<String, String>();
        switch (serviceOfferingVersion.getDeploymentType()) {
            case DOCKER_CONTAINER:
                var dockerContainerServiceOffering = (DockerContainerDeploymentDefinition) serviceOfferingVersion.getDeploymentDefinition();
                serviceMetaData = DockerContainerServiceOfferingOrderUtil
                        .getServiceMetaData(dockerContainerServiceOffering);
                break;

            case DOCKER_COMPOSE:
                var dockerComposeServiceOffering = (DockerComposeDeploymentDefinition) serviceOfferingVersion.getDeploymentDefinition();
                serviceMetaData = DockerComposeFileParser.getServiceMetaData(deployableComposeFile);
                break;

            case KUBERNETES:
                break;

            default:
                throw new NotImplementedException("Deployment Type '" + serviceOfferingVersion.getDeploymentType() + "' not supported");
        }

        return serviceMetaData;
    }

    List<Integer> getServicePorts(ServiceOfferingVersion serviceOfferingVersion,
                                   DockerComposeFile deployableComposeFile) {
        List<Integer> servicePorts = new ArrayList<Integer>();
        switch (serviceOfferingVersion.getDeploymentType()) {
            case DOCKER_CONTAINER:
            case DOCKER_COMPOSE:
                servicePorts = DockerComposeFileParser.getServicePorts(deployableComposeFile);
                break;

            case KUBERNETES:
                break;

            default:
                throw new NotImplementedException("Deployment Type '" + serviceOfferingVersion.getDeploymentType() + "' not supported");
        }

        servicePorts.addAll(serviceOfferingVersion.getServicePorts());

        return servicePorts;
    }

    DockerComposeFile getDeployableComposeFile(ServiceOfferingVersion serviceOfferingVersion, ServiceOrder serviceOrder)
            throws JsonProcessingException, ServiceOptionNotFoundException, InvalidServiceOfferingDefinitionException {
        DockerComposeFile deployableComposeFile = null;
        switch (serviceOfferingVersion.getDeploymentType())
        {
            case DOCKER_CONTAINER:
                deployableComposeFile = DockerContainerServiceOfferingOrderUtil
                        .generateDockerComposeFile(serviceOfferingVersion, serviceOrder);
                break;

            case DOCKER_COMPOSE:
                deployableComposeFile = DockerComposeFileParser.generateDeployableComposeFileForServiceOffering(
                        serviceOfferingVersion, serviceOrder.getServiceOptionValues());
                break;
        }

        if (deployableComposeFile == null) {
            throw new RuntimeException("Unable to create deployable Docker Compose File for order of service offering '"
                    + serviceOfferingVersion.getId() + "' version '" + serviceOfferingVersion.getVersion() + "'");
        }
        else {
            return deployableComposeFile;
        }
    }


    KubernetesManifestFile getDeployableManifestFile(ServiceOfferingVersion serviceOfferingVersion, ServiceOrder serviceOrder)
            throws InvalidServiceOfferingDefinitionException {
        KubernetesManifestFile deployableManifestFile = null;
        switch (serviceOfferingVersion.getDeploymentType())
        {
            case KUBERNETES:
                deployableManifestFile = KubernetesManifestFileParser.generateDeployableManifestFileForServiceOffering(
                        serviceOfferingVersion, serviceOrder.getServiceOptionValues()
                );
                break;
        }

        if (deployableManifestFile == null) {
            throw new RuntimeException("Unable to create deployable Docker Compose File for order of service offering '"
                    + serviceOfferingVersion.getId() + "' version '" + serviceOfferingVersion.getVersion() + "'");
        }
        else {
            return deployableManifestFile;
        }
    }
}
