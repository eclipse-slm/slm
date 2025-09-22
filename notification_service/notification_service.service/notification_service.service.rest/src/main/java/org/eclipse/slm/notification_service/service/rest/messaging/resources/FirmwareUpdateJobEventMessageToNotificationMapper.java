package org.eclipse.slm.notification_service.service.rest.messaging.resources;

import org.eclipse.slm.resource_management.features.device_integration.firmware_update.messaging.FirmwareUpdateJobEventMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Date;

@Mapper(componentModel = "spring")
public interface FirmwareUpdateJobEventMessageToNotificationMapper {

    FirmwareUpdateJobEventMessageToNotificationMapper INSTANCE = Mappers.getMapper(FirmwareUpdateJobEventMessageToNotificationMapper.class);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "timestamp", source = "timestamp")
    FirmwareUpdateJobEventNotification toNotification(FirmwareUpdateJobEventMessage eventMessage, String userId, Date timestamp);

}
