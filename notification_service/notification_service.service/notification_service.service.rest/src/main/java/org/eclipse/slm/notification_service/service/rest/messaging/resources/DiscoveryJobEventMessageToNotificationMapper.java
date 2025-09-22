package org.eclipse.slm.notification_service.service.rest.messaging.resources;

import org.eclipse.slm.notification_service.model.resources.DiscoveryJobEventNotification;
import org.eclipse.slm.resource_management.features.capabilities.jobs.messaging.CapabilityJobEventMessage;
import org.eclipse.slm.resource_management.features.device_integration.discovery.messaging.DiscoveryJobEventMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Date;

@Mapper(componentModel = "spring")
public interface DiscoveryJobEventMessageToNotificationMapper {

    DiscoveryJobEventMessageToNotificationMapper INSTANCE = Mappers.getMapper(DiscoveryJobEventMessageToNotificationMapper.class);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "timestamp", source = "timestamp")
    DiscoveryJobEventNotification toNotification(DiscoveryJobEventMessage eventMessage, String userId, Date timestamp);

}
