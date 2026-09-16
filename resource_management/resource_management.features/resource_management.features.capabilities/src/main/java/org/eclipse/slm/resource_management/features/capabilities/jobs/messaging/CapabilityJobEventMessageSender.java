package org.eclipse.slm.resource_management.features.capabilities.jobs.messaging;

import org.eclipse.slm.common.messaging.GenericMessageSender;
import org.eclipse.slm.resource_management.common.access.AccessControlObjectType;
import org.eclipse.slm.common.access.AccessControlService;
import org.eclipse.slm.resource_management.features.capabilities.jobs.CapabilityJob;
import org.eclipse.slm.resource_management.features.capabilities.jobs.CapabilityJobMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CapabilityJobEventMessageSender extends GenericMessageSender<CapabilityJobEventMessage> {

    private final AccessControlService accessControlService;

    public CapabilityJobEventMessageSender(RabbitTemplate rabbitTemplate,
                                           AccessControlService accessControlService) throws Exception {
        super(rabbitTemplate);
        this.accessControlService = accessControlService;
    }

    public void sendMessage(CapabilityJob capabilityJob) {
        var messagingDTO = CapabilityJobMapper.INSTANCE.toDto(capabilityJob);
        var ownerGroups = capabilityJob.getResourceId() != null
                ? accessControlService.getSubjectsForObject(AccessControlObjectType.RESOURCE, capabilityJob.getResourceId())
                : Set.<String>of();
        var eventMessage = new CapabilityJobEventMessage(messagingDTO, ownerGroups);
        this.sendMessage(eventMessage);
    }
}
