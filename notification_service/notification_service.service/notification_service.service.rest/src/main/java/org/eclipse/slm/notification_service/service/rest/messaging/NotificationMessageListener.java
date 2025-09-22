package org.eclipse.slm.notification_service.service.rest.messaging;

import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.common.messaging.GenericMessageListener;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.messaging.NotificationEventMessage;
import org.eclipse.slm.notification_service.model.Notification;
import org.eclipse.slm.notification_service.persistence.api.NotificationRepository;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class NotificationMessageListener extends GenericMessageListener<NotificationEventMessage> {

    private final NotificationRepository notificationRepository;

    private final NotificationWsService notificationWsService;

    public NotificationMessageListener(NotificationRepository notificationRepository, NotificationWsService notificationWsService,
                                        ConnectionFactory connectionFactory, RabbitTemplate rabbitTemplate
    ) throws Exception {
        super(NotificationEventMessage.EXCHANGE_NAME, AbstractEventMessage.getRoutingKeyAllEvents(NotificationEventMessage.ROUTING_KEY_PREFIX),
                connectionFactory, rabbitTemplate);
        this.notificationRepository = notificationRepository;
        this.notificationWsService = notificationWsService;
    }

    @Override
    public void onMessageReceived(NotificationEventMessage notificationMessage) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var notification = new Notification(
                notificationMessage.getUserId(),
                notificationMessage.getTimestamp(),
                notificationMessage.getCategory(),
                notificationMessage.getSubCategory(),
                notificationMessage.getNotificationEventType(),
                notificationMessage.getPayload()
        );

        notificationRepository.save(notification);
        notificationWsService.notifyFrontend(notification);
        LOG.info("Create new notification: " + notification);
    }


}
