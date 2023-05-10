package org.eclipse.slm.notification_service.service.rest.endpoints;

import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.model.Category;
import org.eclipse.slm.notification_service.model.JobGoal;
import org.eclipse.slm.notification_service.model.JobTarget;
import org.eclipse.slm.notification_service.model.Notification;
import org.eclipse.slm.notification_service.persistence.api.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@EnableAsync
public class NotificationRestController {

    public final static Logger LOG = LoggerFactory.getLogger(NotificationRestController.class);

    private NotificationRepository notificationRepository;

    private NotificationWsService notificationWsService;

    @Autowired
    public NotificationRestController(NotificationRepository notificationRepository, NotificationWsService notificationWsService) {
        this.notificationRepository = notificationRepository;
        this.notificationWsService = notificationWsService;
    }

    @RequestMapping(value = "/notification", method = RequestMethod.POST)
    @Operation(summary = "Create new notifications")
    public void createNotification(
//            @RequestParam(name = "userUuid", required = true) String userUuid,
            @RequestParam(name = "category", required = true) Category category,
            @RequestParam(name="jobTarget", required = true) JobTarget jobTarget,
            @RequestParam(name="jobGoal", required = true) JobGoal jobGoal
//            @RequestParam(name = "text", required = true) String text
    ) {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String text = jobTarget.name().toLowerCase() + " has been " + jobGoal.toString().toLowerCase();
        Notification notification = new Notification(category, text, keycloakPrincipal.getName());

        notificationRepository.save(notification);
        notificationWsService.notifyFrontend(notification);
        LOG.info("Create new notification: " + notification.toString());
    }

    @RequestMapping(value = "/notifications", method = RequestMethod.GET)
    @Operation(summary = "Get notifications of a user")
    public List<Notification> getNotifications(
            @RequestParam(name = "isRead", required = false) Boolean isRead
    ) {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccessToken accessToken = keycloakPrincipal.getKeycloakSecurityContext().getToken();

        String userUuid = accessToken.getSubject();
        if(isRead == null) {
            return notificationRepository.findByOwner(userUuid);
        } else {
            return notificationRepository.findByIsReadAndOwner(isRead, userUuid);
        }
    }

    @RequestMapping(value = "/notifications/read", method = RequestMethod.PATCH)
    @Operation(summary = "Set read property of notifications")
    public void setReadOfNotifications(
            @RequestParam(name = "read") boolean read,
            @RequestBody List<Notification> notifications
    ) {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccessToken accessToken = keycloakPrincipal.getKeycloakSecurityContext().getToken();

        String userUuid = accessToken.getSubject();

        notifications.forEach(n -> n.setRead(read));

        notificationRepository.saveAll(notifications);
    }
}
