package org.eclipse.slm.common.awx.client.observer;

import org.eclipse.slm.common.awx.model.Job;
import org.eclipse.slm.notification_service.model.JobFinalState;
import org.eclipse.slm.notification_service.model.JobGoal;
import org.eclipse.slm.notification_service.model.JobState;
import org.eclipse.slm.notification_service.model.JobTarget;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AwxJobObserver {
    public final static Logger LOG = LoggerFactory.getLogger(AwxJobObserver.class);

    public int jobId;
    public JobTarget jobTarget;
    public JobGoal jobGoal;
    private int pollingInterval;

    private List<IAwxJobObserverListener> jobObserverListeners = new ArrayList<>();

    public AwxJobObserver(String awxUrl, String awxUsername, String awxPassword, int pollingInterval,
                          int jobId, JobTarget jobTarget, JobGoal jobGoal, IAwxJobObserverListener jobObserverListener)
            throws SSLException {
        this(awxUrl, awxUsername, awxPassword, pollingInterval, jobId, jobTarget, jobGoal);
        this.addListener(jobObserverListener);
    }

    public AwxJobObserver(
            String awxUrl,
            String awxUsername,
            String awxPassword,
            int pollingInterval,
            int jobId,
            JobTarget jobTarget,
            JobGoal jobGoal
    ) throws SSLException {
        this.jobId = jobId;
        this.jobTarget = jobTarget;
        this.jobGoal = jobGoal;
        this.pollingInterval = pollingInterval;

        LOG.info ("Create observer for Job with id '" + jobId + "'");

        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        TcpClient tcpClient = TcpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        HttpClient httpClient = HttpClient.from(tcpClient);
        var webClient = WebClient.builder()
                .baseUrl(awxUrl + "/api/v2")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        List<String> finalStates = Stream.of(JobFinalState.values())
                .map(JobFinalState::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        List<String> states = Stream.of(JobState.values())
                .map(JobState::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        Flux<Job> jobFlux = webClient.get()
                .uri("/jobs/{id}/", jobId)
                .headers(headers -> headers.setBasicAuth(awxUsername, awxPassword))
                .retrieve()
                .bodyToFlux(Job.class)
                .repeatWhen(longFlux -> Flux.interval(Duration.ofSeconds(this.pollingInterval)))
                .onBackpressureDrop()
                .takeUntil(job -> finalStates.contains(job.getStatus()))
                .doFinally(signalType -> {
                    LOG.info("Stop observing job with id '" + jobId + "'");
                })
                .doOnError(e -> e.printStackTrace());


        jobFlux.subscribe(job -> {
            String jobStatusString = job.getStatus();

            if (finalStates.contains(jobStatusString))
            {
                var finalState = JobFinalState.valueOf(jobStatusString.toUpperCase());
                fireOnJobFinished(finalState);
            }
            else if (states.contains(jobStatusString)) {
                states.remove(jobStatusString);

                var jobStatus = JobState.valueOf(jobStatusString.toUpperCase());
                fireOnJobStateChanged(jobStatus);
            }
        });
    }

    public void addListener(IAwxJobObserverListener listener)
    {
        this.jobObserverListeners.add(listener);
    }

    public void removeListener(IAwxJobObserverListener listener)
    {
        this.jobObserverListeners.remove(listener);
    }

    public void fireOnJobStateChanged(JobState newState)
    {
        for(var listener : this.jobObserverListeners)
        {
            listener.onJobStateChanged(this, newState);
        }
    }

    public void fireOnJobFinished(JobFinalState finalState)
    {
        for(var listener : this.jobObserverListeners)
        {
            listener.onJobStateFinished(this, finalState);
        }
    }
}
