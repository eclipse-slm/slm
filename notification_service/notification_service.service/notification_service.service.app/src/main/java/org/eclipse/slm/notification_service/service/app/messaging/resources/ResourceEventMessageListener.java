package org.eclipse.slm.notification_service.service.app.messaging.resources;

import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.common.messaging.GenericMessageListener;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.persistence.api.NotificationRepository;
import org.eclipse.slm.resource_management.common.resources.ResourceEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ResourceEventMessageListener extends GenericMessageListener<ResourceEventMessage> {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceEventMessageListener.class);

    private final ResourceUserResolver resourceUserResolver;

    private final NotificationRepository notificationRepository;

    private final NotificationWsService notificationWsService;

    protected ResourceEventMessageListener(ConnectionFactory connectionFactory,
                                           RabbitTemplate rabbitTemplate,
                                           ResourceUserResolver resourceUserResolver,
                                           NotificationRepository notificationRepository,
                                           NotificationWsService notificationWsService) {
        super(ResourceEventMessage.EXCHANGE_NAME, AbstractEventMessage.getRoutingKeyAllEvents(ResourceEventMessage.ROUTING_KEY_PREFIX),
                connectionFactory, rabbitTemplate);
        this.resourceUserResolver = resourceUserResolver;
        this.notificationRepository = notificationRepository;
        this.notificationWsService = notificationWsService;
    }

    @Override
    public void onMessageReceived(ResourceEventMessage eventMessage) {
        try {
            List<String> userIdsToNotify = new ArrayList<>();
            var resourceId = eventMessage.getResource().getId();

            switch (eventMessage.getEventType()) {
                case CREATED, UPDATED -> userIdsToNotify = this.resourceUserResolver.refresh(resourceId);
                case DELETED -> userIdsToNotify = this.resourceUserResolver.evict(resourceId);
            }

            for (var userId : userIdsToNotify) {
                var timestamp = new Date();
                var eventNotification = ResourceEventMessageToNotificationMapper.INSTANCE.toNotification(eventMessage, userId, timestamp);

//                notificationRepository.save(notification);
                notificationWsService.notifyFrontend(eventNotification);
                LOG.info("Created new notification: " + eventNotification);
            }

        } catch (Exception e) {
            LOG.error("Error processing ResourceEventMessage: {}", e.getMessage(), e);
        }
    }

}
