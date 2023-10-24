package org.eclipse.slm.service_management.service.rest.utils;

import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.ServiceOrder;
import org.eclipse.slm.service_management.model.offerings.docker.container.DockerContainerDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOption;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionType;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionValue;
import org.eclipse.slm.service_management.service.rest.docker_compose.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DockerContainerServiceOfferingOrderUtil {

    public static ServiceOption getAndCheckIfServiceOptionIsPresentAndEditable(ServiceOfferingVersion serviceOfferingVersion, String serviceOptionId) {
        for (var serviceOptionCategory : serviceOfferingVersion.getServiceOptionCategories())
        {
            for (var serviceOption : serviceOptionCategory.getServiceOptions())
            {
                if (serviceOption.getId().equals(serviceOptionId))
                {
                    if (serviceOption.getEditable())
                    {
                        return serviceOption;
                    }
                    else
                    {
                        throw new IllegalArgumentException("Service option value '" + serviceOptionId +"' provided, but service option is not defined as editable in service offering");
                    }
                }
            }
        }

        throw new IllegalArgumentException("Service option value '" + serviceOptionId +"' provided, but not defined as service option in service offering");
    }

    public static List<String> generateDockerContainerPortMappings(ServiceOfferingVersion serviceOfferingVersion, List<ServiceOptionValue> serviceOptionValues) {
        var portMappings = new ArrayList<String>();

        var deploymentDefinition = (DockerContainerDeploymentDefinition)serviceOfferingVersion.getDeploymentDefinition();

        // Add all port mappings from service offering
        for (var serviceOfferingPortMapping : deploymentDefinition.getPortMappings())
        {
            var portMappingString = serviceOfferingPortMapping.getHostPort() + ":" + serviceOfferingPortMapping.getContainerPort();
            if (!serviceOfferingPortMapping.getProtocol().isEmpty())
            {
                portMappingString += "/" + serviceOfferingPortMapping.getProtocol();
            }
            portMappings.add(portMappingString);
        }
        // Add all service options which are of type port mapping
        for (var serviceOptionValue : serviceOptionValues)
        {
            var serviceOption = getAndCheckIfServiceOptionIsPresentAndEditable(serviceOfferingVersion, serviceOptionValue.getServiceOptionId());
            if (serviceOption.getOptionType() == ServiceOptionType.PORT_MAPPING) {
                portMappings.add(serviceOptionValue.getValue() + ":" + serviceOption.getKey());
            }
        }

        return portMappings;
    }

    public static List<String> generateDockerContainerVolumes(ServiceOfferingVersion serviceOfferingVersion, List<ServiceOptionValue> serviceOptionValues) {
        var deploymentDefinition = (DockerContainerDeploymentDefinition)serviceOfferingVersion.getDeploymentDefinition();

        var volumesFromServiceOffering = deploymentDefinition.getVolumes();
        var volumes = new ArrayList<String>();

        // Add all volumes from service offering
        for (var serviceOfferingVolume : deploymentDefinition.getVolumes())
        {
            volumes.add(serviceOfferingVolume.getName() + ":" + serviceOfferingVolume.getContainerPath());
        }
        // Add all service options which are of type volume
        for (var serviceOptionValue : serviceOptionValues)
        {
            var serviceOption = getAndCheckIfServiceOptionIsPresentAndEditable(serviceOfferingVersion, serviceOptionValue.getServiceOptionId());
            if (serviceOption.getOptionType() == ServiceOptionType.VOLUME) {
                volumes.add(serviceOptionValue.getValue() + ":" + serviceOption.getKey());
            }
        }

        return volumes;
    }

    public static Map<String, String> generateDockerContainerLabels(ServiceOfferingVersion serviceOfferingVersion, List<ServiceOptionValue> serviceOptionValues) {
        var deploymentDefinition = (DockerContainerDeploymentDefinition)serviceOfferingVersion.getDeploymentDefinition();
        var labels = new HashMap<String, String>();

        // Add all labels from service offering
        for (var serviceOfferingLabel : deploymentDefinition.getLabels())
        {
            labels.put(serviceOfferingLabel.getName(), serviceOfferingLabel.getValue().toString());
        }
        // Add all service options which are of type label
        for (var serviceOptionValue : serviceOptionValues)
        {
            var serviceOption = getAndCheckIfServiceOptionIsPresentAndEditable(serviceOfferingVersion, serviceOptionValue.getServiceOptionId());
            if (serviceOption.getOptionType() == ServiceOptionType.LABEL) {
                labels.put(serviceOption.getKey(), serviceOptionValue.getValue().toString());
            }
        }

        return labels;
    }

    public static Map<String, Object> generateDockerContainerEnvironmentVariables(ServiceOfferingVersion serviceOfferingVersion, List<ServiceOptionValue> serviceOptionValues) {
        var deploymentDefinition = (DockerContainerDeploymentDefinition)serviceOfferingVersion.getDeploymentDefinition();
        Map<String, Object> environmentVariables = new HashMap<>();

        // Add all environment variables from service offering
        for (var serviceOfferingEnvVar : deploymentDefinition.getEnvironmentVariables())
        {
            environmentVariables.put(serviceOfferingEnvVar.getKey(), serviceOfferingEnvVar.getValue());
        }
        // Add all service options which are of type environment variable
        for (var serviceOptionValue : serviceOptionValues)
        {
            var serviceOption = getAndCheckIfServiceOptionIsPresentAndEditable(serviceOfferingVersion, serviceOptionValue.getServiceOptionId());
            if (serviceOption.getOptionType() == ServiceOptionType.ENVIRONMENT_VARIABLE) {
                environmentVariables.put(serviceOption.getKey(), serviceOptionValue.getValue());
            }
        }

        return environmentVariables;
    }

    public static DockerComposeFile generateDockerComposeFile(
            ServiceOfferingVersion serviceOfferingVersion, ServiceOrder serviceOrder)
    {
        var environmentVariables = DockerContainerServiceOfferingOrderUtil
                .generateDockerContainerEnvironmentVariables(serviceOfferingVersion, serviceOrder.getServiceOptionValues());
        var portMappings = DockerContainerServiceOfferingOrderUtil
                .generateDockerContainerPortMappings(serviceOfferingVersion, serviceOrder.getServiceOptionValues());

        var volumes = DockerContainerServiceOfferingOrderUtil
                .generateDockerContainerVolumes(serviceOfferingVersion, serviceOrder.getServiceOptionValues());
        var volumesList = new ArrayList<DockerComposeFileVolume>();
        var namedVolumes = new HashMap<String, Object>();
        for (var volume : volumes)
        {
            var dockerComposeFileVolume = new DockerComposeFileVolume(volume);
            if (dockerComposeFileVolume.getType().equals(DockerComposeFileVolumeType.VOLUME))
            {
                namedVolumes.put(dockerComposeFileVolume.getSource(), null);
            }

            volumesList.add(dockerComposeFileVolume);
        }

        var labels = DockerContainerServiceOfferingOrderUtil
                .generateDockerContainerLabels(serviceOfferingVersion, serviceOrder.getServiceOptionValues());

        var deploymentDefinition = (DockerContainerDeploymentDefinition)serviceOfferingVersion.getDeploymentDefinition();
        var dockerComposeFileService = new DockerComposeFileService(
                deploymentDefinition.getImageRepository() + ":" + deploymentDefinition.getImageTag(),
                deploymentDefinition.getRestartPolicy().getValue(),
                false,
                portMappings,
                new ArrayList<>(),
                new DockerComposeFileEnvironment(environmentVariables),
                volumesList,
                new HashMap<>(),
                new DockerComposeFileLabels(labels),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ""
        );

        Map<String, DockerComposeFileService> dockerComposeServices = new HashMap<>();
        dockerComposeServices.put("service", dockerComposeFileService);

        var dockerComposeFile = new DockerComposeFile("3", dockerComposeServices, null, namedVolumes);

        return dockerComposeFile;
    }

    public static HashMap<String, String> getServiceMetaData(
            DockerContainerDeploymentDefinition dockerContainerDeploymentDefinition) {
        var serviceMetaData = new HashMap<String, String>();
        serviceMetaData.put("docker_image_repo", dockerContainerDeploymentDefinition.getImageRepository());
        serviceMetaData.put("docker_image_tag", dockerContainerDeploymentDefinition.getImageTag());

        return serviceMetaData;
    }
}
