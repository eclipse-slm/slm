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
import java.util.Locale;
import java.util.Objects;

public class AwxWebsocketClient implements AutoCloseable{

    private final WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    private AwxJobEndpoint awxJobEndpoint = null;

    private Session session;

    private final String awxHttpScheme;
    private final String awxWsScheme;
    private final String awxHost;
    private final String  awxPort;
    private final String username;
    private final String password;

    private String xrfToken;

    public AwxWebsocketClient(String awxHost, String awxPort, String username, String password) {
        this("http", awxHost, awxPort, username, password);
    }

    public AwxWebsocketClient(String awxScheme, String awxHost, String awxPort, String username, String password) {
        this.awxHttpScheme = normalizeHttpScheme(awxScheme);
        this.awxWsScheme = this.awxHttpScheme.equals("https") ? "wss" : "ws";
        this.awxHost = awxHost;
        this.awxPort = awxPort;
        this.username = username;
        this.password = password;
    }

    private static String normalizeHttpScheme(String awxScheme) {
        if (awxScheme == null || awxScheme.isBlank()) {
            return "http";
        }

        var normalized = awxScheme.toLowerCase(Locale.ROOT);
        if (normalized.equals("https") || normalized.equals("wss")) {
            return "https";
        }

        return "http";
    }

    private String loginToAwx(String username, String password) {
        String uri = this.awxHttpScheme + "://" + this.awxHost + ":" + this.awxPort;

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

    public synchronized void start() throws IOException, DeploymentException {

        var sessionId = this.loginToAwx(username, password);
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                .configurator(new SessionConfigurator(sessionId, this.xrfToken))
                .build();

        var endpoint = new AwxJobEndpoint(this.xrfToken);
        var newSession = this.container.connectToServer(endpoint, config,
                URI.create(this.awxWsScheme + "://" + awxHost + ":" + awxPort + "/websocket/")
        );

        this.awxJobEndpoint = endpoint;
        this.session = newSession;
    }

    private synchronized void ensureConnected() throws IOException, DeploymentException {
        if (this.session != null && this.session.isOpen() && this.awxJobEndpoint != null) {
            return;
        }

        if (this.session != null) {
            try {
                this.session.close();
            } catch (Exception ignored) {
                // Ignore close failures while recovering from a broken session.
            }
        }

        this.session = null;
        this.awxJobEndpoint = null;
        this.start();
    }

    @Override
    public synchronized void close() throws Exception {
        if (this.awxJobEndpoint != null) {
            this.awxJobEndpoint.stop();
        }

        if (this.session != null && this.session.isOpen()) {
            this.session.close();
        }

        this.session = null;
        this.awxJobEndpoint = null;
    }

    public synchronized void registerObserver(AwxJobObserver observer) throws DeploymentException, IOException {
        this.ensureConnected();
        observer.listenToEndpoint(this.awxJobEndpoint);
    }
}
