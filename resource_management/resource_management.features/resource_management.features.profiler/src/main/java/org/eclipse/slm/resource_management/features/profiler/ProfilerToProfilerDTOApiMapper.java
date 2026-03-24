package org.eclipse.slm.resource_management.features.profiler;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProfilerToProfilerDTOApiMapper {

    ProfilerToProfilerDTOApiMapper INSTANCE = Mappers.getMapper(ProfilerToProfilerDTOApiMapper.class);

    ProfilerDTOApi toDto(Profiler profiler);

    Profiler toEntity(ProfilerDTOApi profilderDTOApi);

}
