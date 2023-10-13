package org.eclipse.slm.common.awx.client.observer;

import org.springframework.http.HttpHeaders;

import javax.websocket.ClientEndpointConfig;
import java.util.List;
import java.util.Map;

public class SessionConfigurator extends ClientEndpointConfig.Configurator {
    private final String sessionId;
    private final String xrfToken;

    public SessionConfigurator(String sessionId, String xrfToken) {
        this.sessionId = sessionId;
        this.xrfToken = "csrftoken="+xrfToken;
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        headers.put(HttpHeaders.COOKIE, List.of(this.sessionId + ";"+ xrfToken));
    }
}
