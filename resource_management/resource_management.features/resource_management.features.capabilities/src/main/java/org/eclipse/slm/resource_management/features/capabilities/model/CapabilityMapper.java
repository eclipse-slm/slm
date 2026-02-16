package org.eclipse.slm.resource_management.features.capabilities.model;

import org.eclipse.slm.resource_management.features.capabilities.dto.BaseConfigurationCapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.dto.DeploymentCapabilityDTOApi;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CapabilityMapper {

    public static final CapabilityMapper INSTANCE = Mappers.getMapper(CapabilityMapper.class);

    DeploymentCapabilityDTOApi toDto(DeploymentCapability source);
    BaseConfigurationCapabilityDTOApi toDto(BaseConfigurationCapability source);
    default CapabilityDTOApi toDto(Capability source) {
        switch (source.getCapabilityClass()) {
            case "DeploymentCapability":
                return toDto((DeploymentCapability) source);
            case "BaseConfigurationCapability":
                return toDto((BaseConfigurationCapability) source);
            default:
                throw new IllegalArgumentException("Unknown capability class: " + source.getCapabilityClass());
        }
    }

    DeploymentCapability toModel(DeploymentCapabilityDTOApi source);
    BaseConfigurationCapability toModel(BaseConfigurationCapabilityDTOApi source);
    default Capability toModel(CapabilityDTOApi source) {
        if (source instanceof DeploymentCapabilityDTOApi) {
            return toModel((DeploymentCapabilityDTOApi) source);
        } else if (source instanceof BaseConfigurationCapabilityDTOApi) {
            return toModel((BaseConfigurationCapabilityDTOApi) source);
        }
        throw new IllegalArgumentException("Unknown capability DTO: " + source.getClass().getSimpleName());
    }
}