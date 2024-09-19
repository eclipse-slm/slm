package org.eclipse.slm.resource_management.service.rest.jobs;

import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.model.Job;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/jobs")
public class JobsRestController {

    @Autowired
    AwxClient awxClient;

    @Autowired
    NotificationServiceClient notificationServiceClient;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public Iterable<Job> getJobs() {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var username = jwtAuthenticationToken.getName();
        var accessToken = jwtAuthenticationToken.getToken().getTokenValue();

        var resultsForUsernameWithoutJWT = awxClient.getJobs(accessToken, username);
        var resultsForUsernameWithJWT = awxClient.getJobs(accessToken, username + "-JWT");

        var jobs = new ArrayList<Job>();
        jobs.addAll(resultsForUsernameWithoutJWT.getResults());
        jobs.addAll(resultsForUsernameWithJWT.getResults());

        return jobs;
    }
}
