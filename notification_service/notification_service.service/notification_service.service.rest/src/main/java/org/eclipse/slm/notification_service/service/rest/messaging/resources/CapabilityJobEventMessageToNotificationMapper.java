package org.eclipse.slm.notification_service.service.rest.messaging.resources;

import org.eclipse.slm.resource_management.common.resources.ResourceEventMessage;
import org.eclipse.slm.resource_management.features.capabilities.jobs.messaging.CapabilityJobEventMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Date;

@Mapper(componentModel = "spring")
public interface CapabilityJobEventMessageToNotificationMapper {

    CapabilityJobEventMessageToNotificationMapper INSTANCE = Mappers.getMapper(CapabilityJobEventMessageToNotificationMapper.class);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "timestamp", source = "timestamp")
    CapabilityJobEventNotification toNotification(CapabilityJobEventMessage eventMessage, String userId, Date timestamp);

}
