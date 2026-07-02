package org.eclipse.slm.notification_service.service.app.messaging.services;

import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.common.messaging.GenericMessageListener;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.persistence.api.NotificationRepository;
import org.eclipse.slm.notification_service.service.app.messaging.UserUtils;
import org.eclipse.slm.service_management.features.service_deployment.api.events.ServiceInstanceEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ServiceInstanceEventMessageListener extends GenericMessageListener<ServiceInstanceEventMessage> {

    private final static Logger LOG = LoggerFactory.getLogger(ServiceInstanceEventMessageListener.class);
    private static final String POLICY_SERVICE_INSTANCE_PREFIX = "service-instance_";

    private final UserUtils userUtils;

    private final NotificationRepository notificationRepository;

    private final NotificationWsService notificationWsService;

    private final Map<UUID, List<String>> serviceInstanceIdToUserIdsCache = new HashMap<>();

    protected ServiceInstanceEventMessageListener(ConnectionFactory connectionFactory,
                                                  RabbitTemplate rabbitTemplate,
                                                  UserUtils userUtils,
                                                  NotificationRepository notificationRepository,
                                                  NotificationWsService notificationWsService) {
        super(ServiceInstanceEventMessage.EXCHANGE_NAME, AbstractEventMessage.getRoutingKeyAllEvents(ServiceInstanceEventMessage.ROUTING_KEY_PREFIX),
                connectionFactory, rabbitTemplate);
        this.userUtils = userUtils;
        this.notificationRepository = notificationRepository;
        this.notificationWsService = notificationWsService;
    }

    @Override
    public void onMessageReceived(ServiceInstanceEventMessage eventMessage) {
        try {
            List<String> userIds = new ArrayList<>();
            var serviceInstanceId = eventMessage.getServiceInstance().getId();
            var serviceInstancePolicyName = POLICY_SERVICE_INSTANCE_PREFIX + serviceInstanceId;

            switch (eventMessage.getEventType()) {
                case CREATED, UPDATED -> {
                    userIds = this.userUtils.getUserIdsAssociatedToPolicy(serviceInstancePolicyName);
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
