package org.eclipse.slm.common.awx.client.observer;


import jakarta.websocket.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

public class AwxWebsocketClient implements AutoCloseable{

    private final WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    private AwxJobEndpoint awxJobEndpoint = null;

    private Session session;

    private final String awxHost;
    private final String  awxPort;
    private final String username;
    private final String password;

    private String xrfToken;

    public AwxWebsocketClient(String awxHost, String awxPort, String username, String password) {
        this.awxHost = awxHost;
        this.awxPort = awxPort;
        this.username = username;
        this.password = password;
    }

    private String loginToAwx(String username, String password) {
        String uri = "http://"+ this.awxHost + ":" + this.awxPort;

        var template = new RestTemplate();
        var csrfResponse = template.getForEntity(uri + "/api/login/", String.class );
        var csrfHeaders = csrfResponse.getHeaders();
        var csrfCookies = csrfHeaders.getFirst(HttpHeaders.SET_COOKIE);

        this.xrfToken = Objects.requireNonNull(csrfCookies).split(";")[0].split("=")[1];

        Document doc = Jsoup.parse(Objects.requireNonNull(csrfResponse.getBody()));
        var element = doc.selectFirst("input[name=\"csrfmiddlewaretoken\"]");
        var csrfMiddlewareToken = Objects.requireNonNull(element).attributes().get("value");

        HttpHeaders header = new HttpHeaders();
        header.put(HttpHeaders.COOKIE, List.of(csrfCookies, "userLoggedIn=false"));
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var hashMap = new LinkedMultiValueMap<String, String>();
        hashMap.add("username", username);
        hashMap.add("password", password);
        hashMap.add("next", "/api/");
        hashMap.add("csrfmiddlewaretoken", csrfMiddlewareToken);

        var loginData = new HttpEntity<>(hashMap, header);
        var loginResponse = template.postForEntity(uri + "/api/login/", loginData, String.class);

        var loginHeaders = loginResponse.getHeaders();
        var loginCookies = loginHeaders.get(HttpHeaders.SET_COOKIE);
        var sessionIdKey = Objects.requireNonNull(loginHeaders.get("X-API-Session-Cookie-Name")).get(0);
        var sessionIdString = Objects.requireNonNull(loginCookies).stream().filter(s -> s.contains(sessionIdKey)).findFirst().get();

        return sessionIdString.split(";")[0];
    }

    public void start() throws IOException, DeploymentException {

        var sessionId = this.loginToAwx(username, password);
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                .configurator(new SessionConfigurator(sessionId, this.xrfToken))
                .build();

        this.awxJobEndpoint = new AwxJobEndpoint(this.xrfToken);
        this.session = this.container.connectToServer(this.awxJobEndpoint, config,
                URI.create("ws://" + awxHost + ":" + awxPort + "/websocket/")
        );
    }


    @Override
    public void close() throws Exception {
        this.awxJobEndpoint.stop();
        this.session.close();
    }

    public void registerObserver(AwxJobObserver observer) throws DeploymentException, IOException {
        if (this.awxJobEndpoint == null) {
            this.start();
        }
        observer.listenToEndpoint(this.awxJobEndpoint);
    }
}
