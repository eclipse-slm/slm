package org.eclipse.slm.notification_service.service.rest.messaging.resources;

import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.common.messaging.GenericMessageListener;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.persistence.api.NotificationRepository;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.resources.ResourceEventMessage;
import org.eclipse.slm.resource_management.features.capabilities.jobs.messaging.CapabilityJobEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CapabilityJobEventMessageListener extends GenericMessageListener<CapabilityJobEventMessage> {

    public final static Logger LOG = LoggerFactory.getLogger(CapabilityJobEventMessageListener.class);

    private final KeycloakAdminClient keycloakAdminClient;

    private final NotificationRepository notificationRepository;

    private final NotificationWsService notificationWsService;

    protected CapabilityJobEventMessageListener(ConnectionFactory connectionFactory,
                                                RabbitTemplate rabbitTemplate,
                                                KeycloakAdminClient keycloakAdminClient,
                                                NotificationRepository notificationRepository,
                                                NotificationWsService notificationWsService) {
        super(CapabilityJobEventMessage.EXCHANGE_NAME, AbstractEventMessage.getRoutingKeyAllEvents(CapabilityJobEventMessage.ROUTING_KEY_PREFIX),
                connectionFactory, rabbitTemplate);
        this.keycloakAdminClient = keycloakAdminClient;
        this.notificationRepository = notificationRepository;
        this.notificationWsService = notificationWsService;
    }

    @Override
    public void onMessageReceived(CapabilityJobEventMessage eventMessage) {
        try {
            var capabilityJob = eventMessage.getCapabilityJob();
            var resourceId = capabilityJob.getResourceId();

            var roleName = ResourcesConsulClient.getResourcePolicyName(resourceId);
            var userIds = this.keycloakAdminClient.getUserIdsAssignedToRole(roleName);

            for (var userId : userIds) {
                var timestamp = new Date();
                var eventNotification = CapabilityJobEventMessageToNotificationMapper.INSTANCE.toNotification(eventMessage, userId, timestamp);

//                notificationRepository.save(notification);
                notificationWsService.notifyFrontend(eventNotification);
                LOG.info("Created new notification: " + eventNotification);
            }
        } catch (Exception e) {
            LOG.error("Error processing CapabilityJobEventMessage: {}", e.getMessage(), e);
        }
    }

}
