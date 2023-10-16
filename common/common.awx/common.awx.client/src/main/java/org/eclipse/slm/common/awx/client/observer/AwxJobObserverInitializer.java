package org.eclipse.slm.common.awx.client.observer;

import org.eclipse.slm.notification_service.model.JobGoal;
import org.eclipse.slm.notification_service.model.JobTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.net.MalformedURLException;
import java.net.URL;

@Component
public class AwxJobObserverInitializer {

    public final static Logger LOG = LoggerFactory.getLogger(AwxJobObserverInitializer.class);
    private final int pollingInterval;

    private String awxUrl;
    private String awxUsername;
    private String awxPassword;

    private AwxWebsocketClient websocketClient;

    public AwxJobObserverInitializer(
            @Value("${awx.scheme}") String schema,
            @Value("${awx.host}") String host,
            @Value("${awx.port}") String port,
            @Value("${awx.username}") String awxUsername,
            @Value("${awx.password}") String awxPassword,
            @Value("${awx.polling-interval-in-s}") int pollingInterval
    ) {
        this.awxUrl = schema + "://" + host + ":" + port;
        this.awxUsername = awxUsername;
        this.awxPassword = awxPassword;
        this.pollingInterval = pollingInterval;


        this.websocketClient = new AwxWebsocketClient(host, port, awxUsername, awxPassword);
        try {
            this.websocketClient.start();
        }catch (Exception e){
            LOG.error("Could not connect to AWX Websocket");
        }


    }

    public AwxJobObserver init(
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
        this.websocketClient.registerObserver(observer);
        return observer;
    }

    public AwxJobObserver init(int jobId, JobTarget jobTarget, JobGoal jobGoal) throws SSLException {
        var observer = new AwxJobObserver(
                jobId,
                jobTarget,
                jobGoal
        );
        this.websocketClient.registerObserver(observer);
        return observer;
    }

    public void setAwxPort(int port) {
        try {
            URL initialUrl = new URL(awxUrl);
            URL newURL = new URL(initialUrl.getProtocol(), initialUrl.getHost(), port, initialUrl.getFile());

            awxUrl = newURL.toString();
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
