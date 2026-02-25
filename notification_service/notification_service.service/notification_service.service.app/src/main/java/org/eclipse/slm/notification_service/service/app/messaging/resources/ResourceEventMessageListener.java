package org.eclipse.slm.notification_service.service.app.messaging.resources;

import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.common.messaging.GenericMessageListener;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.persistence.api.NotificationRepository;
import org.eclipse.slm.notification_service.service.app.messaging.UserUtils;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.resources.ResourceEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ResourceEventMessageListener extends GenericMessageListener<ResourceEventMessage> {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceEventMessageListener.class);

    private final UserUtils userUtils;

    private final ConsulClientFactory consulClientFactory;

    private final NotificationRepository notificationRepository;

    private final NotificationWsService notificationWsService;

    private final Map<UUID, List<String>> resourceIdToUserIdsCache = new HashMap<>();

    protected ResourceEventMessageListener(ConnectionFactory connectionFactory,
                                           RabbitTemplate rabbitTemplate,
                                           UserUtils userUtils,
                                           ConsulClientFactory consulClientFactory,
                                           NotificationRepository notificationRepository,
                                           NotificationWsService notificationWsService) {
        super(ResourceEventMessage.EXCHANGE_NAME, AbstractEventMessage.getRoutingKeyAllEvents(ResourceEventMessage.ROUTING_KEY_PREFIX),
                connectionFactory, rabbitTemplate);
        this.userUtils = userUtils;
        this.consulClientFactory = consulClientFactory;
        this.notificationRepository = notificationRepository;
        this.notificationWsService = notificationWsService;

        // Get for all existing resources the associated userIds and cache them, so that the correct userIds can be notified when a resource is deleted
        var consulClient = this.consulClientFactory.createAdminClient();
        var consulRoles = consulClient.acl().getRoles();
        for (var role : consulRoles) {
            if (role.getPolicies() != null) {
                for (var policyLink : role.getPolicies()) {
                    if (policyLink.getName().startsWith(ResourcesConsulClient.POLICY_RESOURCE_PREFIX) && !policyLink.getName().contains("management")) {
                        var resourceIdString = policyLink.getName().substring(ResourcesConsulClient.POLICY_RESOURCE_PREFIX.length());
                        var resourceId = UUID.fromString(resourceIdString);
                        var userId = role.getName().substring("users_".length());
                        this.resourceIdToUserIdsCache.computeIfAbsent(resourceId, k -> new ArrayList<>()).add(userId);
                    }
                }
            }
        }
    }

    @Override
    public void onMessageReceived(ResourceEventMessage eventMessage) {
        try {
            List<String> userIdsToNotify = new ArrayList<>();
            var resourceId = eventMessage.getResource().getId();
            var resourceConsulPolicyName = ResourcesConsulClient.getResourcePolicyName(resourceId);

            switch (eventMessage.getEventType()) {
                case CREATED, UPDATED -> {
                    userIdsToNotify = userUtils.getUserIdsAssociatedToPolicy(resourceConsulPolicyName);
                    this.resourceIdToUserIdsCache.put(resourceId, userIdsToNotify);
                }
                case DELETED -> {
                    userIdsToNotify = this.resourceIdToUserIdsCache.getOrDefault(resourceId, new ArrayList<>());
                    this.resourceIdToUserIdsCache.remove(resourceId);
                }
            }

            for (var userId : userIdsToNotify) {
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
