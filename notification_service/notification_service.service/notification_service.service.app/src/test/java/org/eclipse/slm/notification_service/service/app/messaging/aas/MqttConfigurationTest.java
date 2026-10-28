package org.eclipse.slm.notification_service.service.app.messaging.aas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.model.IEventNotification;
import org.eclipse.slm.notification_service.service.app.messaging.resources.ResourceUserResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MqttConfigurationTest {

    @Test
    void noMqttBeansWhenDisabled() {
        new ApplicationContextRunner()
                .withUserConfiguration(MqttConfiguration.class)
                .withPropertyValues("aas.broker.enabled=false")
                .run(context -> assertThat(context)
                        .doesNotHaveBean(MqttPahoMessageDrivenChannelAdapter.class)
                        .doesNotHaveBean(AasEventMessageHandler.class)
                        .doesNotHaveBean(SubmodelShellIndex.class));
    }

    @Test
    void enabledConfigurationRoutesMqttMessagesToWebSocketAndMonitorsConnection() {
        var resourceId = UUID.randomUUID();
        var resolver = mock(ResourceUserResolver.class);
        when(resolver.getUserIds(resourceId)).thenReturn(List.of("u1"));
        when(resolver.getKnownResourceIds()).thenReturn(Set.of());
        var ws = mock(NotificationWsService.class);

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(IntegrationAutoConfiguration.class))
                .withUserConfiguration(MqttConfiguration.class)
                .withBean(ResourceUserResolver.class, () -> resolver)
                .withBean(NotificationWsService.class, () -> ws)
                .withBean(ObjectMapper.class, ObjectMapper::new)
                .withPropertyValues("aas.broker.enabled=true", "aas.broker.url=tcp://localhost:1", "aas.broker.client-id=test",
                        "aas.broker.qos=1", "aas.broker.recovery-interval-ms=10000", "aas.broker.topics=aas-repository/+/shells/+",
                        "aas.aas-repository.url=http://localhost:1",
                        "spring.integration.endpoint.no-auto-startup=aasMqttInbound")
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(AasBrokerConnectionMonitor.class);

                    context.getBean("aasMqttInputChannel", MessageChannel.class).send(MessageBuilder
                            .withPayload("{\"id\":\"Resource_" + resourceId + "\"}")
                            .setHeader(MqttHeaders.RECEIVED_TOPIC, "aas-repository/r/shells/updated")
                            .build());
                    verify(ws).notifyFrontend(any(IEventNotification.class));

                    // connection failures published by the adapter reach the monitor without breaking the context
                    context.publishEvent(new MqttConnectionFailedEvent(this, new RuntimeException("lost")));
                    assertThat(context).hasNotFailed();
                });
    }

    @Test
    void dockerProfileUsesContainerHostnames() throws Exception {
        var propertySources = new MutablePropertySources();
        new YamlPropertySourceLoader().load("application-docker", new ClassPathResource("application-docker.yml"))
                .forEach(propertySources::addLast);
        new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml"))
                .forEach(propertySources::addLast);
        var binder = new Binder(ConfigurationPropertySources.from(propertySources),
                new PropertySourcesPlaceholdersResolver(propertySources));

        assertThat(binder.bind("aas.broker", AasBrokerProperties.class).get().url()).isEqualTo("tcp://aas-broker:1884");
        assertThat(binder.bind("aas.aas-repository.url", String.class).get()).isEqualTo("http://aas-environment:8081/aas/environment");
    }

    @Test
    void propertiesBindDefaultsFromYaml() throws Exception {
        var propertySources = new MutablePropertySources();
        new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml"))
                .forEach(propertySources::addLast);
        var binder = new Binder(ConfigurationPropertySources.from(propertySources),
                new PropertySourcesPlaceholdersResolver(propertySources));

        var properties = binder.bind("aas.broker", AasBrokerProperties.class).get();

        assertThat(properties.enabled()).isTrue();
        assertThat(properties.url()).isEqualTo("tcp://localhost:1884");
        assertThat(properties.qos()).isEqualTo(1);
        assertThat(properties.recoveryIntervalMs()).isEqualTo(10000);
        assertThat(properties.topics()).containsExactly(
                "aas-repository/+/shells/+",
                "aas-repository/+/shells/submodels/#",
                "aas-repository/+/shells/+/submodelReferences/+",
                "sm-repository/+/submodels/+");
    }
}
