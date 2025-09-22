package org.eclipse.slm.resource_management.features.device_integration.firmware_update.dto;

import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJob;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FirmwareUpdateJobMapper {

    FirmwareUpdateJobMapper INSTANCE = Mappers.getMapper(FirmwareUpdateJobMapper.class);

    FirmwareUpdateJobDTO toDto(FirmwareUpdateJob firmwareUpdateJob);

}
