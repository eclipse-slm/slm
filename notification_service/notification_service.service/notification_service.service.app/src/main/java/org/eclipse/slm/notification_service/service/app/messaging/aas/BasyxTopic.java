package org.eclipse.slm.notification_service.service.app.messaging.aas;

/**
 * A recognised BaSyx MQTT topic. {@code shellId} and {@code submodelId} are decoded and only set when the topic
 * carries them.
 */
public record BasyxTopic(AasElementKind elementKind, AasEventType eventType, String shellId, String submodelId) {
}
