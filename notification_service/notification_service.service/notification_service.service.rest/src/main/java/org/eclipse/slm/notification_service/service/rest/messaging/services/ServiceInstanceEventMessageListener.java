package org.eclipse.slm.notification_service.service.rest.messaging.services;

import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.common.messaging.GenericMessageListener;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.persistence.api.NotificationRepository;
import org.eclipse.slm.service_management.service.rest.service_instances.ServiceInstanceEventMessage;
import org.eclipse.slm.service_management.service.rest.service_instances.ServiceInstancesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ServiceInstanceEventMessageListener extends GenericMessageListener<ServiceInstanceEventMessage> {

    public final static Logger LOG = LoggerFactory.getLogger(ServiceInstanceEventMessageListener.class);

    private final KeycloakAdminClient keycloakAdminClient;

    private final NotificationRepository notificationRepository;

    private final NotificationWsService notificationWsService;

    private final Map<UUID, List<String>> serviceInstanceIdToUserIdsCache = new HashMap<>();

    protected ServiceInstanceEventMessageListener(ConnectionFactory connectionFactory,
                                                  RabbitTemplate rabbitTemplate,
                                                  KeycloakAdminClient keycloakAdminClient,
                                                  NotificationRepository notificationRepository,
                                                  NotificationWsService notificationWsService) {
        super(ServiceInstanceEventMessage.EXCHANGE_NAME, AbstractEventMessage.getRoutingKeyAllEvents(ServiceInstanceEventMessage.ROUTING_KEY_PREFIX),
                connectionFactory, rabbitTemplate);
        this.keycloakAdminClient = keycloakAdminClient;
        this.notificationRepository = notificationRepository;
        this.notificationWsService = notificationWsService;

        var realmRoles = this.keycloakAdminClient.getAllRolesOfRealm();
        for (var role : realmRoles) {
            if (role.getName().startsWith(ServiceInstancesConsulClient.KEYCLOAK_ROLE_SERVICE_INSTANCE_PREFIX)) {
                var serviceInstanceId = UUID.fromString(role.getName().substring(ServiceInstancesConsulClient.KEYCLOAK_ROLE_SERVICE_INSTANCE_PREFIX.length()));
                this.serviceInstanceIdToUserIdsCache.put(serviceInstanceId, this.keycloakAdminClient.getUserIdsAssignedToRole(role.getName()));
            }
        }
    }

    @Override
    public void onMessageReceived(ServiceInstanceEventMessage eventMessage) {
        try {
            List<String> userIds = new ArrayList<>();
            var serviceInstanceId = eventMessage.getServiceInstance().getId();
            var roleName = ServiceInstancesConsulClient.getServiceInstanceKeycloakRoleName(serviceInstanceId);

            switch (eventMessage.getEventType()) {
                case CREATED -> {
                    userIds = this.keycloakAdminClient.getUserIdsAssignedToRole(roleName);
                    this.serviceInstanceIdToUserIdsCache.put(serviceInstanceId, userIds);
                }
                case DELETED -> {
                    userIds = this.serviceInstanceIdToUserIdsCache.getOrDefault(serviceInstanceId, new ArrayList<>());
                    this.serviceInstanceIdToUserIdsCache.remove(serviceInstanceId);
                }
            }

            for (var userId : userIds) {
                var timestamp = new Date();
                var eventNotification = ServiceInstanceEventMessageToNotificationMapper.INSTANCE.toNotification(eventMessage, userId, timestamp);

//                notificationRepository.save(notification);
                notificationWsService.notifyFrontend(eventNotification);
                LOG.info("Created new notification: " + eventNotification);
            }

        } catch (Exception e) {
            LOG.error("Error processing ResourceEventMessage: {}", e.getMessage(), e);
        }
    }

}
