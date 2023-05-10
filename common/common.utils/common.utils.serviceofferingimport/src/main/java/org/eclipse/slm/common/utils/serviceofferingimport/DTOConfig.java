package org.eclipse.slm.common.utils.serviceofferingimport;

import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.offerings.DeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.DeploymentDefinitionDTOFileImport;
import org.eclipse.slm.service_management.model.offerings.docker.compose.DockerComposeDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.docker.compose.DockerComposeDeploymentDefinitionDTOFileImport;
import org.eclipse.slm.service_management.model.offerings.docker.container.DockerContainerDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.docker.container.DockerContainerDeploymentDefinitionDTOFileImport;
import org.eclipse.slm.service_management.model.offerings.docker.container.DockerRestartPolicy;
import org.eclipse.slm.service_management.model.offerings.kubernetes.KubernetesDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.kubernetes.KubernetesDeploymentDefinitionDTOFileImport;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import java.util.Locale;

public class DTOConfig {

    public static void configureModelMapper() {

        var dockerRestartPolicyConverter = new Converter<String, DockerRestartPolicy>()
        {
            public DockerRestartPolicy convert(MappingContext<String, DockerRestartPolicy> context)
            {
                String sourceString = context.getSource().toUpperCase(Locale.ROOT).replace("-", "_");
                var restartPolicy = DockerRestartPolicy.valueOf(sourceString);
                return restartPolicy;
            }
        };

        var deploymentDefinitionConverter = new Converter<DeploymentDefinitionDTOFileImport, DeploymentDefinition>()
        {
            public DeploymentDefinition convert(MappingContext<DeploymentDefinitionDTOFileImport, DeploymentDefinition> context)
            {
                var deploymentDefinition = ObjectMapperUtils.modelMapper
                        .map(context.getSource(), DeploymentDefinition.class);
                return deploymentDefinition;
            }
        };

        // DTO --> Entity
        ObjectMapperUtils.modelMapper.addConverter(dockerRestartPolicyConverter);
        ObjectMapperUtils.modelMapper.addConverter(deploymentDefinitionConverter);
        ObjectMapperUtils.modelMapper.typeMap(DockerContainerDeploymentDefinitionDTOFileImport.class, DeploymentDefinition.class)
                .setProvider(provisionRequest -> ObjectMapperUtils.map(provisionRequest.getSource(), DockerContainerDeploymentDefinition.class));
        ObjectMapperUtils.modelMapper.typeMap(DockerComposeDeploymentDefinitionDTOFileImport.class, DeploymentDefinition.class)
                .setProvider(provisionRequest -> ObjectMapperUtils.map(provisionRequest.getSource(), DockerComposeDeploymentDefinition.class));
        ObjectMapperUtils.modelMapper.typeMap(KubernetesDeploymentDefinitionDTOFileImport.class, DeploymentDefinition.class)
                .setProvider(provisionRequest -> ObjectMapperUtils.map(provisionRequest.getSource(), KubernetesDeploymentDefinition.class));
    }

}
