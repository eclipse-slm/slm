package org.eclipse.slm.notification_service.service.rest.messaging.resources;

import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.common.messaging.GenericMessageListener;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.persistence.api.NotificationRepository;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.resources.ResourceEventMessage;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.messaging.FirmwareUpdateJobEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FirmwareUpdateJobEventMessageListener extends GenericMessageListener<FirmwareUpdateJobEventMessage> {

    public final static Logger LOG = LoggerFactory.getLogger(FirmwareUpdateJobEventMessageListener.class);

    private final KeycloakAdminClient keycloakAdminClient;

    private final NotificationRepository notificationRepository;

    private final NotificationWsService notificationWsService;

    private final Map<UUID, List<String>> resourceIdToUserIdsCache = new HashMap<>();

    protected FirmwareUpdateJobEventMessageListener(ConnectionFactory connectionFactory,
                                                    RabbitTemplate rabbitTemplate,
                                                    KeycloakAdminClient keycloakAdminClient,
                                                    NotificationRepository notificationRepository,
                                                    NotificationWsService notificationWsService) {
        super(FirmwareUpdateJobEventMessage.EXCHANGE_NAME, AbstractEventMessage.getRoutingKeyAllEvents(FirmwareUpdateJobEventMessage.ROUTING_KEY_PREFIX),
                connectionFactory, rabbitTemplate);
        this.keycloakAdminClient = keycloakAdminClient;
        this.notificationRepository = notificationRepository;
        this.notificationWsService = notificationWsService;

        var realmRoles = this.keycloakAdminClient.getAllRolesOfRealm();
        for (var role : realmRoles) {
            if (role.getName().startsWith("resource_")) {
                var resourceId = UUID.fromString(role.getName().substring("resource_".length()));
                this.resourceIdToUserIdsCache.put(resourceId, this.keycloakAdminClient.getUserIdsAssignedToRole(role.getName()));
            }
        }
    }

    @Override
    public void onMessageReceived(FirmwareUpdateJobEventMessage eventMessage) {
        try {
            var firmwareUpdateJob = eventMessage.getFirmwareUpdateJob();
            var resourceId = firmwareUpdateJob.getResourceId();

            var roleName = ResourcesConsulClient.getResourcePolicyName(resourceId);
            var userIds = this.keycloakAdminClient.getUserIdsAssignedToRole(roleName);

            for (var userId : userIds) {
                var timestamp = new Date();
                var eventNotification = FirmwareUpdateJobEventMessageToNotificationMapper.INSTANCE.toNotification(eventMessage, userId, timestamp);

//                notificationRepository.save(notification);
                notificationWsService.notifyFrontend(eventNotification);
                LOG.info("Created new notification: " + eventNotification);
            }
        } catch (Exception e) {
            LOG.error("Error processing CapabilityJobEventMessage: {}", e.getMessage(), e);
        }
    }

}
