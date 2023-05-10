package org.eclipse.slm.notification_service.service.client;

import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.notification_service.model.Category;
import org.eclipse.slm.notification_service.model.JobGoal;
import org.eclipse.slm.notification_service.model.JobTarget;
import org.apache.http.client.utils.URIBuilder;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;

@Component
public class NotificationServiceClient {

    private final static Logger LOG = LoggerFactory.getLogger(NotificationServiceClient.class);

    private RestTemplate restTemplate;

    @Value("${notification-service.scheme}")
    private String notificationServiceScheme;

    @Value("${notification-service.host}")
    private String notificationServiceHost;

    @Value("${notification-service.port}")
    private int notificationServicePort;

    private WebClient webClient;

    public NotificationServiceClient( ) { }

    @PostConstruct
    private void init() {
        var notificationServiceUrl =
                this.notificationServiceScheme + "://" + this.notificationServiceHost + ":" + this.notificationServicePort;

        this.webClient = WebClient.create(notificationServiceUrl);
        this.restTemplate = new RestTemplate();
    }

    public void postJobObserver(KeycloakPrincipal keycloakPrincipal, AwxJobObserver awxJobObserver) {
        this.postJobObserver(keycloakPrincipal, awxJobObserver.jobId, awxJobObserver.jobTarget, awxJobObserver.jobGoal);
    }

    public void postJobObserver(KeycloakPrincipal keycloakPrincipal, int jobId, JobTarget jobTarget, JobGoal jobGoal) {
        var realm = keycloakPrincipal.getKeycloakSecurityContext().getRealm();
        AccessToken accessToken = keycloakPrincipal.getKeycloakSecurityContext().getToken();

        String userUuid = accessToken.getSubject();

        LOG.info("Create JobObserver for job with id="+jobId+", jobTarget="+jobTarget+", jobGoal="+jobGoal);

        var response = this.webClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/observer/job")
                    .queryParam("userUuid", userUuid)
                    .queryParam("jobId", jobId)
                    .queryParam("jobTarget", jobTarget.toString())
                    .queryParam("jobGoal", jobGoal.toString())
                    .build()
                )
                .header("Realm", realm)
                .header("Authorization", "Bearer " + keycloakPrincipal.getKeycloakSecurityContext().getTokenString())
                .retrieve()
                .toBodilessEntity()
                .block();

        return;
    }

    public void postNotification(KeycloakPrincipal keycloakPrincipal, Category category, JobTarget jobTarget, JobGoal jobGoal) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(keycloakPrincipal.getKeycloakSecurityContext().getTokenString());
        headers.add("Realm", keycloakPrincipal.getKeycloakSecurityContext().getRealm());
        HttpEntity<String> request = new HttpEntity<String>(headers);

        try {
            URIBuilder builder = new URIBuilder();
            String uri = builder.setScheme(this.notificationServiceScheme)
                    .setHost(this.notificationServiceHost)
                    .setPort(this.notificationServicePort)
                    .setPath("/notification")
                    .addParameter("category", category.name())
                    .addParameter("jobTarget", jobTarget.name())
                    .addParameter("jobGoal", jobGoal.name())
                    .build()
                    .toString();
            restTemplate.postForEntity(uri, request, String.class);

        }
        catch (URISyntaxException e)
        {
            LOG.error(e.toString());
        }
    }
}
