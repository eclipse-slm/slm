package org.eclipse.slm.resource_management.features.device_integration.discovery;

import org.eclipse.slm.common.messaging.GenericMessageSender;
import org.eclipse.slm.resource_management.features.device_integration.discovery.mapper.DiscoveryJobToDiscoveryJobDTOMapper;
import org.eclipse.slm.resource_management.features.device_integration.discovery.messaging.DiscoveryJobEventMessage;
import org.eclipse.slm.resource_management.features.device_integration.discovery.messaging.DiscoveryJobEventType;
import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveryJob;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryEventMessageSender extends GenericMessageSender<DiscoveryJobEventMessage> {

    public DiscoveryEventMessageSender(RabbitTemplate rabbitTemplate) throws Exception {
        super(rabbitTemplate);
    }

    public void sendMessage(DiscoveryJob discoveryJob, DiscoveryJobEventType eventType) {
        var messagingDTO = DiscoveryJobToDiscoveryJobDTOMapper.INSTANCE.toDto(discoveryJob);

        var eventMessage = new DiscoveryJobEventMessage(messagingDTO, eventType);
        this.sendMessage(eventMessage);
    }
}
