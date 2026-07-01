package org.eclipse.slm.notification_service.service.app.messaging.services;

import org.eclipse.slm.service_management.features.service_deployment.api.events.ServiceInstanceEventMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Date;

@Mapper(componentModel = "spring")
public interface ServiceInstanceEventMessageToNotificationMapper {

    ServiceInstanceEventMessageToNotificationMapper INSTANCE = Mappers.getMapper(ServiceInstanceEventMessageToNotificationMapper.class);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "timestamp", source = "timestamp")
    ServiceInstanceEventNotification toNotification(ServiceInstanceEventMessage eventMessage, String userId, Date timestamp);

}
