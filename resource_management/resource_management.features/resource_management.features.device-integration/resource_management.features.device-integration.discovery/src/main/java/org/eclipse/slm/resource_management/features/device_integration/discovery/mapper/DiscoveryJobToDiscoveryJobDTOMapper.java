package org.eclipse.slm.resource_management.features.device_integration.discovery.mapper;

import org.eclipse.slm.resource_management.features.device_integration.discovery.dto.DiscoveryJobDTO;
import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveryJob;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DiscoveryJobToDiscoveryJobDTOMapper {

    DiscoveryJobToDiscoveryJobDTOMapper INSTANCE = Mappers.getMapper(DiscoveryJobToDiscoveryJobDTOMapper.class);

    DiscoveryJobDTO toDto(DiscoveryJob discoveryJob);
}
