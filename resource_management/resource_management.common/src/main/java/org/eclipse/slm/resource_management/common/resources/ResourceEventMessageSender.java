package org.eclipse.slm.resource_management.common.resources;

import org.eclipse.slm.common.messaging.GenericMessageSender;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ResourceEventMessageSender extends GenericMessageSender<ResourceEventMessage> {

    public ResourceEventMessageSender(RabbitTemplate rabbitTemplate) throws Exception {
        super(rabbitTemplate);
    }

    public void sendMessage(BasicResource resource, ResourceEventType eventType, Set<String> ownerGroups) {
        var resourceMessagingDTO = ResourceMapper.INSTANCE.toDto(resource);
        var resourceEventMessage = new ResourceEventMessage(resourceMessagingDTO, eventType, ownerGroups);
        this.sendMessage(resourceEventMessage);
    }
}
