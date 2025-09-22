package org.eclipse.slm.resource_management.features.device_integration.discovery.mapper;

import org.eclipse.slm.resource_management.features.device_integration.discovery.dto.DiscoveredResourceDTO;
import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveredResource;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DiscoveredResourceMapper {

    DiscoveredResourceMapper INSTANCE = Mappers.getMapper(DiscoveredResourceMapper.class);

    DiscoveredResourceDTO toDto(DiscoveredResource entity);

    List<DiscoveredResourceDTO> toDtoList(List<DiscoveredResource> entities);
}
