package org.eclipse.slm.resource_management.common.resources;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mirrors GenericMessageListener.onMessage, which converts with a plain Jackson2JsonMessageConverter
 * outside the listener's own try/catch — so a creator misconfiguration here silently kills every
 * notification instead of surfacing as a handled error.
 */
public class ResourceEventMessageSerializationTest {

    private final Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

    private ResourceEventMessage roundTrip(ResourceEventMessage message) {
        var amqpMessage = converter.toMessage(message, new MessageProperties());
        return (ResourceEventMessage) converter.fromMessage(amqpMessage);
    }

    @Test
    public void roundTripsWithOwnerGroups() {
        var message = new ResourceEventMessage(null, ResourceEventType.CREATED, Set.of("/Org/CustomerA"));

        var back = roundTrip(message);

        assertThat(back.getOwnerGroups()).containsExactly("/Org/CustomerA");
        assertThat(back.getEventType()).isEqualTo(ResourceEventType.CREATED);
    }

    @Test
    public void roundTripsWithoutOwnerGroups() {
        var message = new ResourceEventMessage(null, ResourceEventType.DELETED, null);

        var back = roundTrip(message);

        assertThat(back.getOwnerGroups()).isNull();
        assertThat(back.getEventType()).isEqualTo(ResourceEventType.DELETED);
    }
}
