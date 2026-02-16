package org.eclipse.slm.resource_management.common.adapters;

import org.eclipse.slm.common.consul.model.catalog.Node;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ResourcesConsulMapper {

    ResourcesConsulMapper INSTANCE = Mappers.getMapper(ResourcesConsulMapper.class);

}
