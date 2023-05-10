package org.eclipse.slm.resource_management.service.rest.jobs;

import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.model.Job;
import org.eclipse.slm.common.awx.model.Results;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
public class JobsRestController {

    @Autowired
    AwxClient awxClient;

    @Autowired
    NotificationServiceClient notificationServiceClient;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public Iterable<Job> getJobs() {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = keycloakPrincipal.getKeycloakSecurityContext().getToken().getPreferredUsername();
        String accessToken = keycloakPrincipal.getKeycloakSecurityContext().getTokenString();

        Results<Job> results = awxClient.getJobs(accessToken, username);

        return results.getResults();
    }
}
