package org.eclipse.slm.notification_service.service.app.messaging.aas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClientFactory;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.service.app.messaging.resources.ResourceUserResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.Arrays;

/**
 * Subscribes to the MQTT broker of the BaSyx services and forwards lifecycle events of resource shells and their
 * submodels as notifications.
 */
@Configuration
@ConditionalOnProperty(name = "aas.broker.enabled", havingValue = "true")
@EnableConfigurationProperties(AasBrokerProperties.class)
public class MqttConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(MqttConfiguration.class);

    private static final String INPUT_CHANNEL = "aasMqttInputChannel";

    @Bean
    public MqttPahoClientFactory mqttClientFactory(AasBrokerProperties properties) {
        var options = new MqttConnectOptions();
        options.setServerURIs(new String[] { properties.url() });
        if (properties.username() != null && !properties.username().isBlank()) {
            options.setUserName(properties.username());
            options.setPassword(properties.password() != null ? properties.password().toCharArray() : new char[0]);
        }
        // Paho retries the initial connect and lost connections with a backoff starting at 1s, capped at this delay
        options.setAutomaticReconnect(true);
        options.setMaxReconnectDelay((int) properties.recoveryIntervalMs());
        options.setCleanSession(true);

        var factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean(name = INPUT_CHANNEL)
    public MessageChannel aasMqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter aasMqttInbound(AasBrokerProperties properties, MqttPahoClientFactory mqttClientFactory) {
        var topics = properties.topics().toArray(String[]::new);
        var adapter = new MqttPahoMessageDrivenChannelAdapter(properties.clientId(), mqttClientFactory, topics);
        var qos = new int[topics.length];
        Arrays.fill(qos, properties.qos());
        adapter.setQos(qos);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setOutputChannel(this.aasMqttInputChannel());
        return adapter;
    }

    @Bean
    public AasRepositoryClientFactory aasRepositoryClientFactory(@Value("${aas.aas-repository.url}") String aasRepositoryUrl,
                                                                 ObjectProvider<DiscoveryClient> discoveryClient) {
        return new AasRepositoryClientFactory(aasRepositoryUrl, discoveryClient.getIfAvailable());
    }

    @Bean
    public SubmodelShellIndex submodelShellIndex() {
        return new SubmodelShellIndex();
    }

    @Bean
    public AasEventMessageHandler aasEventMessageHandler(ResourceUserResolver resourceUserResolver, SubmodelShellIndex submodelShellIndex,
                                                         NotificationWsService notificationWsService, ObjectMapper objectMapper) {
        return new AasEventMessageHandler(resourceUserResolver, submodelShellIndex, notificationWsService, objectMapper);
    }

    @Bean
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public MessageHandler aasMqttMessageHandler(AasEventMessageHandler aasEventMessageHandler) {
        return message -> aasEventMessageHandler.handle(
                message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class), (String) message.getPayload());
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> submodelShellIndexInitializer(SubmodelShellIndex submodelShellIndex,
                                                                                    ResourceUserResolver resourceUserResolver,
                                                                                    AasRepositoryClientFactory aasRepositoryClientFactory) {
        return event -> {
            try {
                submodelShellIndex.initialize(resourceUserResolver.getKnownResourceIds(), aasRepositoryClientFactory.getClient());
            } catch (Exception e) {
                LOG.warn("Unable to initialize submodel index from AAS repository: {}", e.getMessage());
            }
        };
    }
}
