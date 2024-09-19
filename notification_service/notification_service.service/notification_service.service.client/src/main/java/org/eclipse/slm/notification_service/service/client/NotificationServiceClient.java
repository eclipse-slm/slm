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
import java.net.URL;
import java.util.List;

@Component
public class NotificationServiceClient {

    private final static Logger LOG = LoggerFactory.getLogger(NotificationServiceClient.class);

    private final static String CONSUL_SERVICE_ID = "notification-service";

    private RestTemplate restTemplate;

    @Value("${notification-service.scheme}")
    private String notificationServiceScheme;

    @Value("${notification-service.host}")
    private String notificationServiceHost;

    @Value("${notification-service.port}")
    private int notificationServicePort;

    private WebClient webClient;

    private final DiscoveryClient discoveryClient;

    public NotificationServiceClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @PostConstruct
    private void init() throws MalformedURLException {
        this.webClient = WebClient.create(getNotificationServiceUrl().toString());
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

        return;
    }

    public void postNotification(JwtAuthenticationToken jwtAuthenticationToken, Category category, JobTarget jobTarget, JobGoal jobGoal) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtAuthenticationToken.getToken().getTokenValue());
        HttpEntity<String> request = new HttpEntity<String>(headers);

        try {
            URIBuilder builder = new URIBuilder();
            URL url = getNotificationServiceUrl();
            String uri = builder.setScheme(url.getProtocol())
                    .setHost(url.getHost())
                    .setPort(url.getPort())
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
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private URL getNotificationServiceUrl() throws MalformedURLException {
        URL notificationServiceUrl =  new URL(
                this.notificationServiceScheme,
                this.notificationServiceHost,
                this.notificationServicePort,
                ""
        );

        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(CONSUL_SERVICE_ID);

            if (instances.size() > 0) {
                notificationServiceUrl = new URL(
                        this.notificationServiceScheme,
                        instances.get(0).getHost(),
                        instances.get(0).getPort(),
                        ""
                );
            }
        } catch(Exception e) {
            LOG.warn("Failed to connect to consul server. Fallback to connection details of notification service from application.yml");
        }

        return notificationServiceUrl;
    }
}
