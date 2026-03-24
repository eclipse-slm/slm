package org.eclipse.slm.resource_management.features.capabilities.model;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CapabilityServiceMapper {

    CapabilityServiceMapper INSTANCE = Mappers.getMapper(CapabilityServiceMapper.class);

    @Mapping(target = "capabilityId", source = "capability.id")
    @Mapping(target = "capabilityClass", source = "capability.capabilityClass")
    CapabilityServiceDTO toDto(CapabilityService capabilityService);

    List<CapabilityServiceDTO> toDtoList(List<CapabilityService> capabilityService);

}
