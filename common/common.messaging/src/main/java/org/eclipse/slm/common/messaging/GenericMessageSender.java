package org.eclipse.slm.common.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Component;

@Component
public abstract class GenericMessageSender<T extends AbstractEventMessage> {

    private final static Logger LOG = LoggerFactory.getLogger(GenericMessageSender.class);

    private final RabbitTemplate rabbitTemplate;

    public GenericMessageSender(RabbitTemplate rabbitTemplate) throws Exception {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    public void sendMessage(T message) {
        rabbitTemplate.convertAndSend(message.getExchangeName(), message.getRoutingKey(), message);
        LOG.info("Sent message: {}", message);
    }
}
