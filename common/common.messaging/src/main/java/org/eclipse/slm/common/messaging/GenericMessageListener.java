package org.eclipse.slm.common.messaging;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public abstract class GenericMessageListener<T extends AbstractEventMessage> implements MessageListener {

    public final static Logger LOG = LoggerFactory.getLogger(GenericMessageListener.class);

    private ConnectionFactory connectionFactory;

    private RabbitTemplate rabbitTemplate;

    @Value("${spring.application.name}")
    private String nameOfReceivingService;

    private final String exchangeName;

    private final String routingKeyAllEvents;

    protected GenericMessageListener(String exchangeName, String routingKeyAllEvents,
                                     ConnectionFactory connectionFactory, RabbitTemplate rabbitTemplate){
        this.connectionFactory = connectionFactory;
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKeyAllEvents = routingKeyAllEvents;
    }

    @PostConstruct
    public void init()  throws Exception  {
        var queueName = this.exchangeName + "." + this.routingKeyAllEvents + "@" +this.nameOfReceivingService;
        var queue = new Queue(queueName, false);;

        var amqpAdmin = new RabbitAdmin(rabbitTemplate.getConnectionFactory());
        amqpAdmin.declareQueue(queue);

        var messageListenerContainer = new SimpleMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setQueues(queue);
        messageListenerContainer.setMessageListener(this);
        messageListenerContainer.start();

        var resourcesExchange = new TopicExchange(this.exchangeName);
        var binding = BindingBuilder.bind(queue).to(resourcesExchange).with(this.routingKeyAllEvents);
        amqpAdmin.declareBinding(binding);
    }

    @Override
    public void onMessage(Message message) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        T typedMessage = (T) converter.fromMessage(message);
        onMessageReceived(typedMessage);
    }

    public abstract void onMessageReceived(T message);
}
