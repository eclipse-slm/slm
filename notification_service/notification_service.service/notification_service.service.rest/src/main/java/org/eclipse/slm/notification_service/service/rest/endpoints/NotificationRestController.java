package org.eclipse.slm.notification_service.service.rest.endpoints;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.model.*;
import org.eclipse.slm.notification_service.persistence.api.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("")
@EnableAsync
public class NotificationRestController {

    public final static Logger LOG = LoggerFactory.getLogger(NotificationRestController.class);

    private final NotificationRepository notificationRepository;

    private final NotificationWsService notificationWsService;

    @Autowired
    public NotificationRestController(NotificationRepository notificationRepository, NotificationWsService notificationWsService) {
        this.notificationRepository = notificationRepository;
        this.notificationWsService = notificationWsService;
    }

    @RequestMapping(value = "/notification", method = RequestMethod.POST)
    @Operation(summary = "Create new notification")
    public void createNotification(@RequestBody Notification notification) {
        notificationRepository.save(notification);
        notificationWsService.notifyFrontend(notification);
        LOG.info("Createed new notification: " + notification);
    }

    @RequestMapping(value = "/notifications", method = RequestMethod.GET)
    @Operation(summary = "Get notifications of a user")
    public List<Notification> getNotifications(
            @RequestParam(name = "isRead", required = false) Boolean isRead
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String userId = jwtAuthenticationToken.getToken().getSubject();
        if(isRead == null) {
            return notificationRepository.findByUserId(userId);
        } else {
            return notificationRepository.findByIsReadAndUserId(isRead, userId);
        }
    }

    @RequestMapping(value = "/notifications/read", method = RequestMethod.PATCH)
    @Operation(summary = "Set read property of notifications")
    public void setReadOfNotifications(
            @RequestParam(name = "read") boolean read,
            @RequestBody List<Long> notificationIds
    ) {
        var notifications = new ArrayList<Notification>();
        for (var notificationId : notificationIds) {
                notificationRepository.findById(notificationId).ifPresent(notification -> {
                notification.setRead(read);
                notifications.add(notification);
            });
        }

        notificationRepository.saveAll(notifications);
    }

    @RequestMapping(value = "/notifications/event-notification-model", method = RequestMethod.GET)
    @Operation(summary = "Get event notification model")
    public ResponseEntity<EventNotificationModel> getEventNotificationModel() {
        return ResponseEntity.ok(null);
    }
}
