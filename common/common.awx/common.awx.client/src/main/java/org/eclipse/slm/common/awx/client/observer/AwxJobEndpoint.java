package org.eclipse.slm.common.awx.client.observer;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ClientEndpoint
public class AwxJobEndpoint extends Endpoint implements MessageHandler.Partial<String> {

    public final static Logger LOG = LoggerFactory.getLogger(AwxJobObserverInitializer.class);

    private Set<AwxJobObserver> awxJobObservers = new CopyOnWriteArraySet<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private Session session;
    private final String xrfToken;

    public AwxJobEndpoint(String xrfToken) {
        this.xrfToken = xrfToken;
    }

    public void registerObserver(AwxJobObserver awxJobObserver) {
        this.awxJobObservers.add(awxJobObserver);
    }

    public void removeObserver(AwxJobObserver awxJobObserver) {
        this.awxJobObservers.remove(awxJobObserver);
    }

    public void stop() {
        for (AwxJobObserver awxJobObserver : this.awxJobObservers) {
            awxJobObserver.stopListenToEndpoint();
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        try {
            this.session = session;
            var subscribeMessage = objectMapper.writeValueAsString(new SubscribeToJobsMessage(this.xrfToken));
            this.session.addMessageHandler(this);
            this.session.getBasicRemote().sendText(subscribeMessage);


        } catch (IOException e) {
            LOG.error("Failed to open WebSocket connection to AWX server", e);
        }
    }

    @Override
    public void onMessage(String messagePart, boolean last) {

        try {
            JsonNode jsonNode = objectMapper.readTree(messagePart);
            if(jsonNode.has("type")){
                if (jsonNode.get("type").textValue().equals("job")) {
                    var jobId = jsonNode.get("unified_job_id").asInt();
                    var jobStatus = jsonNode.get("status").asText();
                    for (AwxJobObserver awxJobObserver : this.awxJobObservers) {
                        awxJobObserver.check(jobId, jobStatus);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Could not handle message from AWX server: " + messagePart, e);
        }
    }

    @Override
    public void onError(Session session, Throwable cause) {
        LOG.error("WebSocket error occurred", cause);
    }


    private final class SubscribeToJobsMessage {
        public final String xrftoken;
        public Groups groups = new Groups();

        public SubscribeToJobsMessage(String xrfToken) {
            this.xrftoken = xrfToken;
        }
    }

    private final class Groups {
        public List<String> jobs = List.of("status_changed");
        public List<String> control = List.of("limit_reached_1");
    }

}
