package org.eclipse.slm.notification_service.service.rest.messaging.services;

import org.eclipse.slm.notification_service.service.rest.messaging.resources.ServiceInstanceEventNotification;
import org.eclipse.slm.service_management.service.rest.service_instances.ServiceInstanceEventMessage;
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
