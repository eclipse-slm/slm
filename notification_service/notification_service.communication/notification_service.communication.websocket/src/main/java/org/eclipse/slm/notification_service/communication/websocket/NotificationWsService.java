package org.eclipse.slm.notification_service.communication.websocket;

import org.eclipse.slm.notification_service.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
public class NotificationWsService {
    private final Logger logger = LoggerFactory.getLogger(NotificationWsService.class);

    private final SimpMessagingTemplate simpleMessageTemplate;

    @Autowired
    SimpUserRegistry simpUserRegistry;

    @Autowired
    public NotificationWsService(SimpMessagingTemplate simpleMessageTemplate) {
        this.simpleMessageTemplate = simpleMessageTemplate;
    }

    public void notifyFrontend(final Notification notification) {
        String prefix = simpleMessageTemplate.getUserDestinationPrefix();

        logger.info("Send notification '" + notification.getText() + "' to user '" + notification.getOwner() + "'");

        simpleMessageTemplate.convertAndSendToUser(notification.getOwner(), "/topic/notifications", notification);
        simpleMessageTemplate.convertAndSend("/topic/notifications/"+notification.getOwner(), notification);
    }
}
