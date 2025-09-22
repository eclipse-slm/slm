package org.eclipse.slm.resource_management.features.capabilities.jobs;

import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityJobDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CapabilityJobMapper {

    CapabilityJobMapper INSTANCE = Mappers.getMapper(CapabilityJobMapper.class);

    CapabilityJobDTO toDto(CapabilityJob capabilityJob);

    List<CapabilityJobDTO> toDtoList(List<CapabilityJob> capabilityJob);

}
