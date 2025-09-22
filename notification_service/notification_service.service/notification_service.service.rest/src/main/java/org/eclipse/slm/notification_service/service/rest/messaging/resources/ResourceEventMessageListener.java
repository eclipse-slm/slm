package org.eclipse.slm.notification_service.service.rest.messaging.resources;

import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.common.messaging.GenericMessageListener;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.persistence.api.NotificationRepository;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.resources.ResourceEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;
import java.util.*;

@Component
public class ResourceEventMessageListener extends GenericMessageListener<ResourceEventMessage> {

    public final static Logger LOG = LoggerFactory.getLogger(ResourceEventMessageListener.class);

    private final KeycloakAdminClient keycloakAdminClient;

    private final NotificationRepository notificationRepository;

    private final NotificationWsService notificationWsService;

    private final Map<UUID, List<String>> resourceIdToUserIdsCache = new HashMap<>();

    protected ResourceEventMessageListener(ConnectionFactory connectionFactory,
                                           RabbitTemplate rabbitTemplate,
                                           KeycloakAdminClient keycloakAdminClient,
                                           NotificationRepository notificationRepository,
                                           NotificationWsService notificationWsService) {
        super(ResourceEventMessage.EXCHANGE_NAME, AbstractEventMessage.getRoutingKeyAllEvents(ResourceEventMessage.ROUTING_KEY_PREFIX),
                connectionFactory, rabbitTemplate);
        this.keycloakAdminClient = keycloakAdminClient;
        this.notificationRepository = notificationRepository;
        this.notificationWsService = notificationWsService;

        var realmRoles = this.keycloakAdminClient.getAllRolesOfRealm();
        for (var role : realmRoles) {
            if (role.getName().startsWith(ResourcesConsulClient.KEYCLOAK_ROLE_RESOURCE_PREFIX)) {
                var resourceId = UUID.fromString(role.getName().substring(ResourcesConsulClient.KEYCLOAK_ROLE_RESOURCE_PREFIX.length()));
                this.resourceIdToUserIdsCache.put(resourceId, this.keycloakAdminClient.getUserIdsAssignedToRole(role.getName()));
            }
        }
    }

    @Override
    public void onMessageReceived(ResourceEventMessage eventMessage) {
        try {
            List<String> userIds = new ArrayList<String>();
            var resourceId = eventMessage.getResource().getId();
            var roleName = ResourcesConsulClient.getResourceKeycloakRoleName(resourceId);

            switch (eventMessage.getEventType()) {
                case CREATED, UPDATED -> {
                    try {
                        userIds = this.keycloakAdminClient.getUserIdsAssignedToRole(roleName);
                        this.resourceIdToUserIdsCache.put(resourceId, userIds);
                    } catch (NotFoundException e) {
                        userIds = this.resourceIdToUserIdsCache.getOrDefault(resourceId, new ArrayList<>());
                    }
                }
                case DELETED -> {
                    userIds = this.resourceIdToUserIdsCache.getOrDefault(resourceId, new ArrayList<>());
                    this.resourceIdToUserIdsCache.remove(resourceId);
                }
            }

            for (var userId : userIds) {
                var timestamp = new Date();
                var eventNotification = ResourceEventMessageToNotificationMapper.INSTANCE.toNotification(eventMessage, userId, timestamp);

//                notificationRepository.save(notification);
                notificationWsService.notifyFrontend(eventNotification);
                LOG.info("Created new notification: " + eventNotification);
            }

        } catch (Exception e) {
            LOG.error("Error processing ResourceEventMessage: {}", e.getMessage(), e);
        }
    }

}
