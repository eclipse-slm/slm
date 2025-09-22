package org.eclipse.slm.resource_management.common.resources;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResourceMapper {

    ResourceMapper INSTANCE = Mappers.getMapper(ResourceMapper.class);

    ResourceDTO toDto(BasicResource resource);

    List<ResourceDTO> toDto(List<BasicResource> resources);

}
