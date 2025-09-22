package org.eclipse.slm.resource_management.common.resources;

import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.common.messaging.GenericMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ResourceEventMessageListener extends GenericMessageListener<ResourceEventMessage> {

    public final static Logger LOG = LoggerFactory.getLogger(ResourceEventMessageListener.class);

    private final List<ResourceEventInternalListener> listeners;

    protected ResourceEventMessageListener(ConnectionFactory connectionFactory,
                                           RabbitTemplate rabbitTemplate,
                                           List<ResourceEventInternalListener> listeners) {
        super(ResourceEventMessage.EXCHANGE_NAME, AbstractEventMessage.getRoutingKeyAllEvents(ResourceEventMessage.ROUTING_KEY_PREFIX),
                connectionFactory, rabbitTemplate);
        this.listeners = listeners;
    }

    @Override
    public void onMessageReceived(ResourceEventMessage eventMessage) {
        switch (eventMessage.getEventType()) {
            case CREATED -> {
                for (var listener : listeners) {
                    listener.onResourceCreated(eventMessage.getResource());
                }
            }
            case UPDATED -> {
                for (var listener : listeners) {
                    listener.onResourceUpdated(eventMessage.getResource());
                }
            }
            case DELETED -> {
                for (var listener : listeners) {
                    listener.onResourceDeleted(eventMessage.getResource());
                }
            }
        }
    }
}
