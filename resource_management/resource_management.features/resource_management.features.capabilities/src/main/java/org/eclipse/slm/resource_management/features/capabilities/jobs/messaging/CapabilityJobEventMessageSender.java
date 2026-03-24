package org.eclipse.slm.resource_management.features.capabilities.jobs.messaging;

import org.eclipse.slm.common.messaging.GenericMessageSender;
import org.eclipse.slm.resource_management.features.capabilities.jobs.CapabilityJob;
import org.eclipse.slm.resource_management.features.capabilities.jobs.CapabilityJobMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CapabilityJobEventMessageSender extends GenericMessageSender<CapabilityJobEventMessage> {

    public CapabilityJobEventMessageSender(RabbitTemplate rabbitTemplate) throws Exception {
        super(rabbitTemplate);
    }

    public void sendMessage(CapabilityJob capabilityJob) {
        var messagingDTO = CapabilityJobMapper.INSTANCE.toDto(capabilityJob);

        var eventMessage = new CapabilityJobEventMessage(messagingDTO);
        this.sendMessage(eventMessage);
    }
}
