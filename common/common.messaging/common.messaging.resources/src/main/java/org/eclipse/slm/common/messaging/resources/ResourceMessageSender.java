package org.eclipse.slm.common.messaging.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ResourceMessageSender {

    public final static Logger LOG = LoggerFactory.getLogger(ResourceMessageSender.class);

    public static final String EXCHANGE_NAME = "resources";

    private final RabbitTemplate rabbitTemplate;

    public ResourceMessageSender(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean
    public TopicExchange resourceExchange() {
        return new TopicExchange(ResourceMessageSender.EXCHANGE_NAME);
    }

    public void sendResourceCreatedMessage(UUID resourceId, String assetId) {
        var message = new ResourceCreatedMessage(resourceId, assetId);
        rabbitTemplate.convertAndSend(ResourceMessageSender.EXCHANGE_NAME, ResourceCreatedMessage.ROUTING_KEY, message);
        LOG.info("Sent resource created message: {}", message);
    }

    public void sendResourceInformationMessage(UUID resourceId) {
        var message = new ResourceInformationFoundMessage(resourceId, null);
        rabbitTemplate.convertAndSend(ResourceMessageSender.EXCHANGE_NAME, ResourceInformationFoundMessage.ROUTING_KEY, message);
        LOG.info("Sent resource information message: {}", message);
    }

}
