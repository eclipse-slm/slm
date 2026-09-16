package org.eclipse.slm.resource_management.features.device_integration.firmware_update.messaging;

import org.eclipse.slm.common.messaging.GenericMessageSender;
import org.eclipse.slm.resource_management.common.access.AccessControlObjectType;
import org.eclipse.slm.common.access.AccessControlService;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.dto.FirmwareUpdateJobMapper;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJob;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FirmwareUpdateJobEventMessageSender extends GenericMessageSender<FirmwareUpdateJobEventMessage> {

    private final AccessControlService accessControlService;

    public FirmwareUpdateJobEventMessageSender(RabbitTemplate rabbitTemplate,
                                               AccessControlService accessControlService) throws Exception {
        super(rabbitTemplate);
        this.accessControlService = accessControlService;
    }

    public void sendMessage(FirmwareUpdateJob firmwareUpdateJob) {
        var messagingDTO = FirmwareUpdateJobMapper.INSTANCE.toDto(firmwareUpdateJob);
        var ownerGroups = firmwareUpdateJob.getResourceId() != null
                ? accessControlService.getSubjectsForObject(AccessControlObjectType.RESOURCE, firmwareUpdateJob.getResourceId())
                : Set.<String>of();
        var eventMessage = new FirmwareUpdateJobEventMessage(messagingDTO, ownerGroups);
        this.sendMessage(eventMessage);
    }
}
