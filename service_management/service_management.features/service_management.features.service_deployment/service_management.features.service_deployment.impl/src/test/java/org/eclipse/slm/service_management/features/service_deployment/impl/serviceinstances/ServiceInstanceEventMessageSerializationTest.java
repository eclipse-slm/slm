package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import org.eclipse.slm.service_management.features.service_deployment.api.events.ServiceInstanceEventMessage;
import org.eclipse.slm.service_management.features.service_deployment.api.events.ServiceInstanceEventType;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mirrors GenericMessageListener.onMessage, which converts with a plain Jackson2JsonMessageConverter
 * outside the listener's own try/catch — so a creator misconfiguration here silently kills every
 * notification instead of surfacing as a handled error.
 */
public class ServiceInstanceEventMessageSerializationTest {

    private final Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

    private ServiceInstanceEventMessage roundTrip(ServiceInstanceEventMessage message) {
        var amqpMessage = converter.toMessage(message, new MessageProperties());
        return (ServiceInstanceEventMessage) converter.fromMessage(amqpMessage);
    }

    private ServiceInstance instance() {
        return new ServiceInstance(
                UUID.randomUUID(), List.of("alpha"), Map.of("custom", "x"),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                List.of(8080), List.of());
    }

    @Test
    public void roundTripsWithOwnerGroups() {
        var message = new ServiceInstanceEventMessage(
                instance(), ServiceInstanceEventType.CREATED, Set.of("/users/user-1"));

        var back = roundTrip(message);

        assertThat(back.getOwnerGroups()).containsExactly("/users/user-1");
        assertThat(back.getEventType()).isEqualTo(ServiceInstanceEventType.CREATED);
    }

    @Test
    public void roundTripsWithoutOwnerGroups() {
        var message = new ServiceInstanceEventMessage(
                instance(), ServiceInstanceEventType.DELETED, null);

        var back = roundTrip(message);

        assertThat(back.getOwnerGroups()).isNull();
        assertThat(back.getEventType()).isEqualTo(ServiceInstanceEventType.DELETED);
    }
}
