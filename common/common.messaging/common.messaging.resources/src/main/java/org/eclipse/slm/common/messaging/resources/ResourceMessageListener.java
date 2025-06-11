package org.eclipse.slm.common.messaging.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public abstract class ResourceMessageListener {

    public final static Logger LOG = LoggerFactory.getLogger(ResourceMessageListener.class);

    @Value("${spring.application.name}")
    private String nameOfReceivingService;


    public String getResourceCreatedQueueName() {
        return ResourceCreatedMessage.QUEUE_NAME_PREFIX_RESOURCE_CREATED + this.nameOfReceivingService;
    }

    public String getResourceInformationQueueName() {
        return ResourceInformationFoundMessage.QUEUE_NAME_PREFIX_RESOURCE_INFORMATION + this.nameOfReceivingService;
    }

    @Bean
    public Queue resourceCreatedQueue() {
        return new Queue(this.getResourceCreatedQueueName(), false);
    }

    @Bean
    public Queue resourceInformationQueue() {
        return new Queue(this.getResourceInformationQueueName(), false);
    }

    @Bean
    public Binding declareBindingResourceCreated() {
        var resourcesExchange = new TopicExchange(ResourceMessageSender.EXCHANGE_NAME);
        return BindingBuilder.bind(resourceCreatedQueue()).to(resourcesExchange).with(ResourceCreatedMessage.ROUTING_KEY);
    }

    @Bean
    public Binding declareBindingResourceInformation() {
        var resourcesExchange = new TopicExchange(ResourceMessageSender.EXCHANGE_NAME);
        return BindingBuilder.bind(resourceInformationQueue()).to(resourcesExchange).with(ResourceCreatedMessage.ROUTING_KEY);
    }

    @RabbitListener(queues =  ResourceCreatedMessage.QUEUE_NAME_PREFIX_RESOURCE_CREATED + "${spring.application.name}")
    public abstract void onResourceCreated(ResourceCreatedMessage resource);

    @RabbitListener(queues =  ResourceInformationFoundMessage.QUEUE_NAME_PREFIX_RESOURCE_INFORMATION + "${spring.application.name}")
    public abstract void onResourceInformationFound(ResourceInformationFoundMessage resourceInformationFoundMessage);

}
