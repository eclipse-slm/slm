package org.eclipse.slm.resource_management.common.jobs;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/jobs")
@Tag(name = "Jobs")
public class JobsRestController {

    @Autowired
    AwxClient awxClient;

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
