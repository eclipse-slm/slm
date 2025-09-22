package org.eclipse.slm.notification_service.service.rest.messaging.resources;

import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.common.messaging.GenericMessageListener;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.persistence.api.NotificationRepository;
import org.eclipse.slm.resource_management.features.device_integration.discovery.messaging.DiscoveryJobEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DiscoveryJobEventMessageListener extends GenericMessageListener<DiscoveryJobEventMessage> {

    public final static Logger LOG = LoggerFactory.getLogger(DiscoveryJobEventMessageListener.class);

    private final KeycloakAdminClient keycloakAdminClient;

    private final NotificationRepository notificationRepository;

    private final NotificationWsService notificationWsService;

    protected DiscoveryJobEventMessageListener(ConnectionFactory connectionFactory,
                                               RabbitTemplate rabbitTemplate,
                                               KeycloakAdminClient keycloakAdminClient, NotificationRepository notificationRepository,
                                               NotificationWsService notificationWsService) {
        super(DiscoveryJobEventMessage.EXCHANGE_NAME, AbstractEventMessage.getRoutingKeyAllEvents(DiscoveryJobEventMessage.ROUTING_KEY_PREFIX),
                connectionFactory, rabbitTemplate);
        this.keycloakAdminClient = keycloakAdminClient;
        this.notificationRepository = notificationRepository;
        this.notificationWsService = notificationWsService;
    }

    @Override
    public void onMessageReceived(DiscoveryJobEventMessage eventMessage) {
        try {
            var userIds = this.keycloakAdminClient.getAllUserIds();

            for (var userId : userIds) {
                var timestamp = new Date();
                var eventNotification = DiscoveryJobEventMessageToNotificationMapper.INSTANCE.toNotification(eventMessage, userId, timestamp);

//                notificationRepository.save(notification);
                notificationWsService.notifyFrontend(eventNotification);
                LOG.info("Created new notification: " + eventNotification);
            }
        } catch (Exception e) {
            LOG.error("Error processing DiscoveryJobEventMessage: {}", e.getMessage(), e);
        }
    }

}
