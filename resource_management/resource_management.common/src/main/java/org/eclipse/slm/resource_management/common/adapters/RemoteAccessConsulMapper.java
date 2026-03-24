package org.eclipse.slm.resource_management.common.adapters;

import org.eclipse.slm.common.consul.model.catalog.CatalogRegistration;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessDTOReadMinimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RemoteAccessConsulMapper {

    RemoteAccessConsulMapper INSTANCE = Mappers.getMapper(RemoteAccessConsulMapper.class);

    @Mapping(source = "port", target = "connectionPort")
    RemoteAccessDTOReadMinimal toDto(RemoteAccessConsulService remoteAccessConsulService);

    CatalogRegistration.Service toCatalogRegistrationService(RemoteAccessConsulService remoteAccessConsulService);
}
