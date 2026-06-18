package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import org.eclipse.slm.common.messaging.GenericMessageSender;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.impl.ServiceInstanceEventMessage;
import org.eclipse.slm.service_management.features.service_deployment.impl.ServiceInstanceEventType;
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

