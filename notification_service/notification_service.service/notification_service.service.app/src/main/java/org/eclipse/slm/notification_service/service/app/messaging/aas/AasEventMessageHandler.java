package org.eclipse.slm.notification_service.service.app.messaging.aas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClientFactory;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.service.app.messaging.resources.ResourceUserResolver;
import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Maps BaSyx MQTT lifecycle events of resource shells and their submodels to {@link AasEventNotification}s and sends
 * them to the users associated with the resource.
 */
public class AasEventMessageHandler {

    private final static Logger LOG = LoggerFactory.getLogger(AasEventMessageHandler.class);

    private final ResourceUserResolver resourceUserResolver;

    private final SubmodelShellIndex submodelShellIndex;

    private final NotificationWsService notificationWsService;

    private final ObjectMapper objectMapper;

    private final SubmodelRegistryClientFactory submodelRegistryClientFactory;

    public AasEventMessageHandler(ResourceUserResolver resourceUserResolver, SubmodelShellIndex submodelShellIndex,
                                  NotificationWsService notificationWsService, ObjectMapper objectMapper,
                                  SubmodelRegistryClientFactory submodelRegistryClientFactory) {
        this.resourceUserResolver = resourceUserResolver;
        this.submodelShellIndex = submodelShellIndex;
        this.notificationWsService = notificationWsService;
        this.objectMapper = objectMapper;
        this.submodelRegistryClientFactory = submodelRegistryClientFactory;
    }

    public void handle(String topic, String payload) {
        try {
            var topicOptional = BasyxTopicParser.parse(topic);
            if (topicOptional.isEmpty()) {
                LOG.debug("Ignoring MQTT message on unsupported topic '{}'", topic);
                return;
            }
            var basyxTopic = topicOptional.get();

            JsonNode json;
            try {
                json = this.objectMapper.readTree(payload);
            } catch (JsonProcessingException e) {
                LOG.warn("Ignoring MQTT message on topic '{}' with invalid JSON payload: {}", topic, e.getMessage());
                return;
            }

            String shellId;
            String submodelId = null;
            String submodelIdShort = null;
            String semanticId = null;

            switch (basyxTopic.elementKind()) {
                case SHELL -> {
                    shellId = text(json, "id");
                    if (shellId == null) {
                        LOG.warn("Ignoring MQTT message on topic '{}' without shell id", topic);
                        return;
                    }
                    if (basyxTopic.eventType() == AasEventType.DELETED) {
                        this.submodelShellIndex.removeShell(shellId);
                    }
                }
                case SUBMODEL_REFERENCE -> {
                    if (basyxTopic.submodelId() != null) {
                        // Repository topic: payload is the shell
                        shellId = text(json, "id");
                        submodelId = basyxTopic.submodelId();
                    } else {
                        // Service topic: payload is the submodel reference
                        shellId = basyxTopic.shellId();
                        submodelId = firstKeyValue(json);
                        semanticId = firstKeyValue(json.get("referredSemanticId"));
                    }
                    if (shellId == null || submodelId == null) {
                        LOG.warn("Ignoring MQTT message on topic '{}' without shell or submodel id", topic);
                        return;
                    }
                    if (basyxTopic.eventType() == AasEventType.CREATED) {
                        this.submodelShellIndex.put(submodelId, shellId);
                    } else {
                        this.submodelShellIndex.remove(submodelId);
                    }
                }
                case SUBMODEL -> {
                    submodelId = text(json, "id");
                    if (submodelId == null) {
                        LOG.warn("Ignoring MQTT message on topic '{}' without submodel id", topic);
                        return;
                    }
                    submodelIdShort = text(json, "idShort");
                    semanticId = firstKeyValue(json.get("semanticId"));
                    var shellIdOptional = this.submodelShellIndex.getShellId(submodelId);
                    if (shellIdOptional.isEmpty()) {
                        LOG.debug("Ignoring event of submodel '{}' not referenced by a known resource shell", submodelId);
                        return;
                    }
                    // The index entry is kept on DELETED: a submodel can be re-created while its reference stays in
                    // the shell (e.g. AASX re-upload). Reference and shell DELETED events clean the entry up.
                    shellId = shellIdOptional.get();
                }
                default -> throw new IllegalStateException("Unexpected element kind: " + basyxTopic.elementKind());
            }

            if (!shellId.startsWith(ResourceAas.AAS_ID_PREFIX)) {
                LOG.debug("Ignoring event of AAS '{}' which is not a resource shell", shellId);
                return;
            }
            UUID resourceId;
            try {
                resourceId = UUID.fromString(ResourceAas.getResourceIdFromAasId(shellId));
            } catch (IllegalArgumentException e) {
                LOG.debug("Ignoring event of AAS '{}' without valid resource id", shellId);
                return;
            }

            var userIds = this.resourceUserResolver.getUserIds(resourceId);
            if (userIds.isEmpty()) {
                LOG.debug("No users to notify about event of resource '{}'", resourceId);
                return;
            }

            // Submodel references carry neither idShort nor semanticId of the submodel
            if (basyxTopic.elementKind() == AasElementKind.SUBMODEL_REFERENCE && (submodelIdShort == null || semanticId == null)) {
                var descriptorOptional = this.getSubmodelDescriptor(submodelId);
                if (descriptorOptional.isPresent()) {
                    var descriptor = descriptorOptional.get();
                    if (submodelIdShort == null) {
                        submodelIdShort = descriptor.getIdShort();
                    }
                    if (semanticId == null && descriptor.getSemanticId() != null && descriptor.getSemanticId().getKeys() != null
                            && !descriptor.getSemanticId().getKeys().isEmpty()) {
                        semanticId = descriptor.getSemanticId().getKeys().get(0).getValue();
                    }
                }
            }

            for (var userId : userIds) {
                var eventNotification = new AasEventNotification(userId, new Date(), basyxTopic.eventType(), basyxTopic.elementKind(),
                        resourceId, shellId, submodelId, submodelIdShort, semanticId);
                this.notificationWsService.notifyFrontend(eventNotification);
                LOG.info("Created new notification: " + eventNotification);
            }
        } catch (Exception e) {
            LOG.error("Error processing MQTT message on topic '{}': {}", topic, e.getMessage(), e);
        }
    }

    private Optional<SubmodelDescriptor> getSubmodelDescriptor(String submodelId) {
        try {
            var descriptorOptional = this.submodelRegistryClientFactory.getClient().getSubmodelDescriptor(submodelId);
            if (descriptorOptional.isEmpty()) {
                LOG.debug("No descriptor of submodel '{}' in submodel registry", submodelId);
            }
            return descriptorOptional;
        } catch (Exception e) {
            LOG.warn("Unable to get descriptor of submodel '{}' from submodel registry: {}", submodelId, e.getMessage());
            return Optional.empty();
        }
    }

    private static String text(JsonNode node, String fieldName) {
        var field = node.get(fieldName);
        return field != null && field.isTextual() ? field.asText() : null;
    }

    private static String firstKeyValue(JsonNode reference) {
        if (reference == null) {
            return null;
        }
        var keys = reference.get("keys");
        if (keys == null || !keys.isArray() || keys.isEmpty()) {
            return null;
        }
        return text(keys.get(0), "value");
    }
}
