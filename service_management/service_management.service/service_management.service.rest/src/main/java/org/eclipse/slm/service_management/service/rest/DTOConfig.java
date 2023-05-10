package org.eclipse.slm.service_management.service.rest;

import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.offerings.DeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.docker.compose.DockerComposeDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.docker.container.DockerContainerDeploymentDefinition;
import org.eclipse.slm.service_management.model.vendors.ServiceVendorDeveloper;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.PropertyMap;

import java.util.UUID;

public class DTOConfig {

    public static void configureModelMapper() {

        // Configure Model Mapper
        // Entity --> DTO
        ObjectMapperUtils.modelMapper.typeMap(DockerContainerDeploymentDefinition.class, DeploymentDefinition.class)
                .setProvider(provisionRequest -> ObjectMapperUtils.map(provisionRequest.getSource(), DockerContainerDeploymentDefinition.class));
        ObjectMapperUtils.modelMapper.typeMap(DockerComposeDeploymentDefinition.class, DeploymentDefinition.class)
                .setProvider(provisionRequest -> ObjectMapperUtils.map(provisionRequest.getSource(), DockerComposeDeploymentDefinition.class));
        // Keycloak UserRepresentation --> ServiceVendorDeveloperDTO
        Converter<UserRepresentation, UUID> uuidConverter = new AbstractConverter<UserRepresentation, UUID>() {
            protected UUID convert(UserRepresentation source) {
                return UUID.fromString(source.getId());
            }
        };
        ObjectMapperUtils.modelMapper.addMappings(new PropertyMap<UserRepresentation, ServiceVendorDeveloper>() {
            protected void configure() {
                try {
                    using(uuidConverter).map(source).setId(null);
                } catch (Exception ex) {
                    System.out.println("Error.");
                }
            }
        });

        // DTO --> Entity
        ObjectMapperUtils.modelMapper.typeMap(DockerContainerDeploymentDefinition.class, DeploymentDefinition.class)
                .setProvider(provisionRequest -> ObjectMapperUtils.map(provisionRequest.getSource(), DockerContainerDeploymentDefinition.class));
        ObjectMapperUtils.modelMapper.typeMap(DockerComposeDeploymentDefinition.class, DeploymentDefinition.class)
                .setProvider(provisionRequest -> ObjectMapperUtils.map(provisionRequest.getSource(), DockerComposeDeploymentDefinition.class));
    }

}
