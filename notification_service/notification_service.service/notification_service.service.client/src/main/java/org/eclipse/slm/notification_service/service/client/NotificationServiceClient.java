package org.eclipse.slm.notification_service.service.client;

import jakarta.annotation.PostConstruct;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.notification_service.model.Category;
import org.eclipse.slm.notification_service.model.JobGoal;
import org.eclipse.slm.notification_service.model.JobTarget;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class NotificationServiceClient {

    private final static Logger LOG = LoggerFactory.getLogger(NotificationServiceClient.class);

    private final static String CONSUL_SERVICE_ID = "notification-service";

    private RestTemplate restTemplate;

    private String notificationServiceScheme;

    private String notificationServiceUrl;

    private String notificationServicePath;

    private WebClient webClient;

    private final DiscoveryClient discoveryClient;

    public NotificationServiceClient(DiscoveryClient discoveryClient,
                                     @Value("${notification-service.scheme}") String notificationServiceScheme,
                                     @Value("${notification-service.url}") String notificationServiceUrl,
                                     @Value("${notification-service.path}") String notificationServicePath
    ) {
        this.discoveryClient = discoveryClient;
        this.notificationServiceScheme = notificationServiceScheme;
        this.notificationServiceUrl = notificationServiceUrl;
        this.notificationServicePath = notificationServicePath;
    }

    @PostConstruct
    private void init() throws MalformedURLException {
        this.webClient = WebClient.create(getNotificationServiceUrl());
        this.restTemplate = new RestTemplate();
    }

    public void postJobObserver(JwtAuthenticationToken jwtAuthenticationToken, AwxJobObserver awxJobObserver) {
        this.postJobObserver(jwtAuthenticationToken, awxJobObserver.jobId, awxJobObserver.jobTarget, awxJobObserver.jobGoal);
    }

    public void postJobObserver(JwtAuthenticationToken jwtAuthenticationToken, int jobId, JobTarget jobTarget, JobGoal jobGoal) {
        var userUuid = jwtAuthenticationToken.getToken().getSubject();

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
                .header("Authorization", "Bearer " + jwtAuthenticationToken.getToken().getTokenValue())
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void postNotification(JwtAuthenticationToken jwtAuthenticationToken, Category category, JobTarget jobTarget, JobGoal jobGoal) {
        try {
            this.postNotification(jwtAuthenticationToken, category, jobTarget, jobGoal, "");
        } catch (Exception e) {
            try {
                LOG.error("Error with Notification Service ('{}'), skipping post of notification: {}", this.getNotificationServiceUrl(), e.getMessage());
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void postNotification(JwtAuthenticationToken jwtAuthenticationToken, Category category, JobTarget jobTarget, JobGoal jobGoal, String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtAuthenticationToken.getToken().getTokenValue());
            HttpEntity<String> request = new HttpEntity<String>(headers);

            try {
                var uri = new URIBuilder(this.getNotificationServiceUrl() + "/notification")
                        .addParameter("category", category.name())
                        .addParameter("jobTarget", jobTarget.name())
                        .addParameter("jobGoal", jobGoal.name())
                        .addParameter("text", text)
                        .build()
                        .toString();
                var response = restTemplate.postForEntity(uri, request, String.class);
            } catch (URISyntaxException e) {
                LOG.error(e.toString());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            try {
                LOG.error("Error with Notification Service ('{}'), skipping post of notification: {}", this.getNotificationServiceUrl(), e.getMessage());
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private String getNotificationServiceUrl() throws MalformedURLException {
        var notificationServiceUrl =  this.notificationServiceUrl;

        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(CONSUL_SERVICE_ID);
            if (!instances.isEmpty()) {
                notificationServiceUrl =  this.notificationServiceScheme + "://" + instances.get(0).getHost() + ":" + instances.get(0).getPort() + this.notificationServicePath;
            }
        } catch(Exception e) {
            LOG.warn("Failed to connect to consul server or to get notification service details. Fallback to connection details of notification service from application.yml: {}", e.getMessage());
        }

        return notificationServiceUrl;
    }
}
