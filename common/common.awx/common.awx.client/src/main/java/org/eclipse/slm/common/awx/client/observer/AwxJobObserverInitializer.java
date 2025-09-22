package org.eclipse.slm.common.awx.client.observer;

import jakarta.annotation.PostConstruct;
import jakarta.websocket.DeploymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AwxJobObserverInitializer {

    public final static Logger LOG = LoggerFactory.getLogger(AwxJobObserverInitializer.class);
    private final int pollingInterval;

    private String awxHost;
    private String awxPort;
    private String awxUrl;
    private String awxUsername;
    private String awxPassword;

    private AwxWebsocketClient websocketClient;

    public AwxJobObserverInitializer(
            @Value("${awx.scheme}") String awxSchema,
            @Value("${awx.host}") String awxHost,
            @Value("${awx.port}") String awxPort,
            @Value("${awx.username}") String awxUsername,
            @Value("${awx.password}") String awxPassword,
            @Value("${awx.polling-interval-in-s}") int pollingInterval
    ) {
        this.awxHost = awxHost;
        this.awxPort = awxPort;
        this.awxUrl = awxSchema + "://" + awxHost + ":" + awxPort;
        this.awxUsername = awxUsername;
        this.awxPassword = awxPassword;
        this.pollingInterval = pollingInterval;
    }

    @PostConstruct
    private void init() {
        this.websocketClient = new AwxWebsocketClient(this.awxHost, this.awxPort, this.awxUsername, this.awxPassword);
        try {
            this.websocketClient.start();
        }catch (Exception e){
            LOG.error("Could not connect to AWX Websocket: " + this.awxUrl);
        }
    }

    public AwxJobObserver initNewObserver(
            int jobId,
            JobTarget jobTarget,
            JobGoal jobGoal,
            IAwxJobObserverListener jobObserverListener
    ) {
        var observer = new AwxJobObserver(
                jobId,
                jobTarget,
                jobGoal,
                jobObserverListener
        );
        try {
            this.websocketClient.registerObserver(observer);
        } catch (DeploymentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return observer;
    }
}
