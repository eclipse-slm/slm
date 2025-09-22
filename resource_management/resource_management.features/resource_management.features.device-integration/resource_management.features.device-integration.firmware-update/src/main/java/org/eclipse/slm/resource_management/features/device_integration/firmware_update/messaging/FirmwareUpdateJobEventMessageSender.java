package org.eclipse.slm.resource_management.features.device_integration.firmware_update.messaging;

import org.eclipse.slm.common.messaging.GenericMessageSender;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.dto.FirmwareUpdateJobMapper;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJob;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class FirmwareUpdateJobEventMessageSender extends GenericMessageSender<FirmwareUpdateJobEventMessage> {

    public FirmwareUpdateJobEventMessageSender(RabbitTemplate rabbitTemplate) throws Exception {
        super(rabbitTemplate);
    }

    public void sendMessage(FirmwareUpdateJob firmwareUpdateJob) {
        var messagingDTO = FirmwareUpdateJobMapper.INSTANCE.toDto(firmwareUpdateJob);

        var eventMessage = new FirmwareUpdateJobEventMessage(messagingDTO);
        this.sendMessage(eventMessage);
    }
}
