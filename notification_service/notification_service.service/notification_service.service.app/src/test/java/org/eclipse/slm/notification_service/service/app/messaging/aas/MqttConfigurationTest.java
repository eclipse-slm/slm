package org.eclipse.slm.notification_service.service.app.messaging.aas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;

import static org.assertj.core.api.Assertions.assertThat;

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
                "aas-repository/+/shells/submodels/+/+",
                "aas-repository/+/shells/+/submodelReferences/+",
                "submodel-repository/+/submodels/+");
    }
}
