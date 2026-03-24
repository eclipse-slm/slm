package org.eclipse.slm.notification_service.messaging;

import org.eclipse.slm.common.messaging.GenericMessageSender;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationMessageSender extends GenericMessageSender<NotificationEventMessage> {

    public NotificationMessageSender(RabbitTemplate rabbitTemplate) throws Exception {
        super(rabbitTemplate);
    }

}
