package org.eclipse.slm.notification_service.service.rest.messaging.resources;

import org.eclipse.slm.resource_management.common.resources.ResourceEventMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Date;

@Mapper(componentModel = "spring")
public interface ResourceEventMessageToNotificationMapper {

    ResourceEventMessageToNotificationMapper INSTANCE = Mappers.getMapper(ResourceEventMessageToNotificationMapper.class);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "timestamp", source = "timestamp")
    ResourceEventNotification toNotification(ResourceEventMessage resourceEventMessage, String userId, Date timestamp);

}
