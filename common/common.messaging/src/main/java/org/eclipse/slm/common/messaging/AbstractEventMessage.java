package org.eclipse.slm.common.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public abstract class AbstractEventMessage<T extends MessageEventType> implements Serializable {

    private final String exchangeName;
    private final String routingKeyPrefix;
    protected final T eventType;

    protected AbstractEventMessage(String exchangeName, String routingKeyPrefix, T eventType) {
        this.exchangeName = exchangeName;
        this.routingKeyPrefix = routingKeyPrefix;
        this.eventType = eventType;
    }

    public String getExchangeName() {
        return this.exchangeName;
    }

    public String getRoutingKey() {
        var routingKey = this.routingKeyPrefix + "." + this.eventType.toString().toLowerCase();
        return routingKey;
    }

    public String getRoutingKey(MessageEventType messageEventType) {
        return this.routingKeyPrefix + "." + messageEventType.toString().toLowerCase();
    }

    public static String getRoutingKeyAllEvents(String routingKeyPrefix) {
        return routingKeyPrefix + ".*";
    }

    @JsonProperty("eventType")
    public T getEventType() {
        return this.eventType;
    }

    @Override
    public String toString() {
        return "{" +
                "exchangeName='" + exchangeName + '\'' +
                ", routingKeyPrefix='" + routingKeyPrefix + '\'' +
                ", eventType=" + eventType +
                '}';
    }
}
