package org.eclipse.slm.notification_service.service.rest.endpoints;

import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.awx.client.observer.IAwxJobObserverListener;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.model.*;
import org.eclipse.slm.notification_service.persistence.api.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/observer")
@EnableAsync
public class ObserverRestController implements IAwxJobObserverListener {

    public final static Logger LOG = LoggerFactory.getLogger(ObserverRestController.class);

    private String awxUrl;

    @Value("${awx.username}")
    private String awxUsername;

    @Value("${awx.password}")
    private String awxPassword;

    @Value("${awx.polling-interval-in-s}")
    private int pollingInterval;

    private NotificationRepository notificationRepository;

    private NotificationWsService notificationWsService;

    private Map<Integer, List<String>> jobIdToNotifyUsers = new HashMap<>();

    private AwxJobObserverInitializer awxJobObserverInitializer;

    public ObserverRestController(
            @Value("${awx.scheme}") String scheme,
            @Value("${awx.host}") String host,
            @Value("${awx.port}") String port,
            @Value("${awx.username}") String awxUsername,
            @Value("${awx.password}") String awxPassword,
            @Value("${awx.polling-interval-in-s}") int pollingInterval,
            NotificationRepository notificationRepository,
            NotificationWsService notificationWsService,
            AwxJobObserverInitializer awxJobObserverInitializer
    ) {

        this.awxUrl = scheme + "://" + host + ":" + port;
        this.awxUsername = awxUsername;
        this.awxPassword = awxPassword;
        this.pollingInterval = pollingInterval;

        this.notificationRepository = notificationRepository;
        this.notificationWsService = notificationWsService;

        this.awxJobObserverInitializer = awxJobObserverInitializer;
    }

    @RequestMapping(value = "/job", method = RequestMethod.POST)
    @Operation(summary = "Create observer for running job")
    public void createJobObserver(
            @RequestParam(name="userUuid", required = true) String userUuid,
            @RequestParam(name="jobId", required = true) int jobId,
            @RequestParam(name="jobTarget", required = true) JobTarget jobTarget,
            @RequestParam(name="jobGoal", required = true) JobGoal jobGoal
    ) throws SSLException {

        var awxJobObserver = this.awxJobObserverInitializer.initNewObserver(jobId, jobTarget, jobGoal, this);
        if (!jobIdToNotifyUsers.containsKey(jobId))
        {
            jobIdToNotifyUsers.put(jobId, new ArrayList<>());
        }
        jobIdToNotifyUsers.get(jobId).add(userUuid);
    }

    @Override
    public void onJobStateChanged(AwxJobObserver sender, JobState newState) {
        if(this.jobIdToNotifyUsers.containsKey(sender.jobId))
        {
            for (var userUuid : this.jobIdToNotifyUsers.get(sender.jobId))
            {
                sendJobChangedNotification(sender.jobId, newState.name(), userUuid);
            }
        }
        else {
            LOG.error("Job '" + sender.jobId + "' observed and changed state to '" + newState + "', but no user to notify");
        }
    }

    @Override
    public void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState) {
        if(this.jobIdToNotifyUsers.containsKey(sender.jobId)) {
            for (var userUuid : this.jobIdToNotifyUsers.get(sender.jobId)) {
                sendJobChangedNotification(sender.jobId, finalState.name(), userUuid);
            }

            this.jobIdToNotifyUsers.remove(sender.jobId);
        }
        else {
            LOG.error("Job '" + sender.jobId + "' observed and finished with '" + finalState + "', but no user to notify");
        }
    }

    private void sendJobChangedNotification(int jobId, String jobState, String userUuid) {
        Category category = Category.JOBS;
        String text = "Job " + jobId + " turned into state '" + jobState + "'";
        Notification notification = new Notification(category, text, userUuid);

        notificationRepository.save(notification);
        notificationWsService.notifyFrontend(notification);

        LOG.info("Created notification for user '" + userUuid + "': " + text);
    }
}
