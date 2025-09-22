package org.eclipse.slm.resource_management.common.resources;

import org.eclipse.slm.common.messaging.GenericMessageSender;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ResourceEventMessageSender extends GenericMessageSender<ResourceEventMessage> {

    public ResourceEventMessageSender(RabbitTemplate rabbitTemplate) throws Exception {
        super(rabbitTemplate);
    }

    public void sendMessage(BasicResource resource, ResourceEventType eventType) {
        var resourceMessagingDTO = ResourceMapper.INSTANCE.toDto(resource);

        var resourceEventMessage = new ResourceEventMessage(resourceMessagingDTO, eventType);
        this.sendMessage(resourceEventMessage);
    }
}
