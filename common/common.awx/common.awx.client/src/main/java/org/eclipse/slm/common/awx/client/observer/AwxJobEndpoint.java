package org.eclipse.slm.common.awx.client.observer;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@ClientEndpoint
public class AwxJobEndpoint extends Endpoint implements MessageHandler.Partial<String> {


    public final static Logger LOG = LoggerFactory.getLogger(AwxJobObserverInitializer.class);

    HashSet<AwxJobObserver> awxJobObservers = new HashSet<>();
    ObjectMapper objectMapper = new ObjectMapper();
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
            // TODO: Logging
            LOG.error(e.getMessage());
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
        } catch (JsonProcessingException e) {
            // TODO: Logging
            LOG.error(e.getMessage());
        } catch (Exception e) {
            // TODO: Logging
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void onError(Session session, Throwable cause) {
        // TODO: Logging
        LOG.error("EofException for " + session.getRequestURI());
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
