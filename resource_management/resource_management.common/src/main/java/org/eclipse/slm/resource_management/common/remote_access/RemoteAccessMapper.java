package org.eclipse.slm.resource_management.common.remote_access;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RemoteAccessMapper {

    RemoteAccessMapper INSTANCE = Mappers.getMapper(RemoteAccessMapper.class);

    @Mapping(source = "port", target = "connectionPort")
    RemoteAccessDTO toDto(RemoteAccessConsulService remoteAccessConsulService);

}
