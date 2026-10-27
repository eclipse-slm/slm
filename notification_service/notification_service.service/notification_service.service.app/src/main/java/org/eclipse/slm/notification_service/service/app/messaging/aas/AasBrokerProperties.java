package org.eclipse.slm.notification_service.service.app.messaging.aas;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Connection and subscription settings for the MQTT broker the BaSyx services publish their events to.
 * {@code aas.broker.scheme/host/port} only feed {@code url} via placeholders and are not bound.
 */
@ConfigurationProperties("aas.broker")
public record AasBrokerProperties(
        boolean enabled,
        String url,
        String clientId,
        String username,
        String password,
        int qos,
        long recoveryIntervalMs,
        List<String> topics
) {
}
