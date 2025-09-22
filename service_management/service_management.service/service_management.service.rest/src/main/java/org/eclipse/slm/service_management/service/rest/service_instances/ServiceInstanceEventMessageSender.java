package org.eclipse.slm.service_management.service.rest.service_instances;

import org.eclipse.slm.common.messaging.GenericMessageSender;
import org.eclipse.slm.service_management.model.services.ServiceInstance;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ServiceInstanceEventMessageSender extends GenericMessageSender<ServiceInstanceEventMessage> {

    public ServiceInstanceEventMessageSender(RabbitTemplate rabbitTemplate) throws Exception {
        super(rabbitTemplate);
    }

    public void sendMessage(ServiceInstance serviceInstance, ServiceInstanceEventType eventType) {
        var eventMessage = new ServiceInstanceEventMessage(serviceInstance, eventType);
        this.sendMessage(eventMessage);
    }
}
