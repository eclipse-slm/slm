package org.eclipse.slm.common.consul.client.apis;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.model.acl.ImmutableLogin;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import okhttp3.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class AbstractConsulApiClient {

    public final static Logger LOG = LoggerFactory.getLogger(AbstractConsulApiClient.class);
    @Autowired
    protected RestTemplate restTemplate;

    protected String consulHost;

    protected int consulPort;

    protected String consulToken;

    protected String consulScheme;

    protected String consulDatacenter;

    public AbstractConsulApiClient(String consulScheme, String consulHost, int consulPort,
                                   String consulToken, String consulDatacenter) {
        this.consulScheme = consulScheme;
        this.consulHost = consulHost;
        this.consulPort = consulPort;
        this.consulToken = consulToken;
        this.consulDatacenter = consulDatacenter;
    }

    private Map<String, Consul> consulClients = new HashMap<>();

    private Map<String, Consul> keycloakTokenClients = new HashMap<>();

    public Consul getConsulClient(ConsulCredential consulCredential) throws ConsulLoginFailedException {
        switch (consulCredential.getConsulCredentialType()) {
            case APPLICATION_PROPERTIES -> {
                if (consulClients.containsKey("APPLICATION_PROPERTIES")) {
                    return consulClients.get("APPLICATION_PROPERTIES");
                } else {
                    var consulToken = this.consulToken;
                    var consulClient = Consul.builder()
                            .withHostAndPort(HostAndPort.fromParts(consulHost, consulPort))
                            .withReadTimeoutMillis(Duration.ofSeconds(2).toMillis())
                            .withTokenAuth(consulToken)
                            .build();
                    this.consulClients.put("APPLICATION_PROPERTIES", consulClient);
                    return consulClient;
                }
            }

            case CONSUL_TOKEN -> {
                var consulToken = consulCredential.getConsulToken();
                if (consulClients.containsKey(consulToken)) {
                    return consulClients.get(consulToken);
                } else {
                    var consulClient = Consul.builder()
                            .withHostAndPort(HostAndPort.fromParts(consulHost, consulPort))
                            .withReadTimeoutMillis(Duration.ofSeconds(2).toMillis())
                            .withTokenAuth(consulToken)
                            .build();
                    this.consulClients.put(consulToken, consulClient);
                    return consulClient;
                }
            }

            case KEYCLOAK_TOKEN -> {
                var keycloakToken = consulCredential.getKeycloakPrincipal().getKeycloakSecurityContext().getTokenString();
                var keycloakName = consulCredential.getKeycloakPrincipal().getName();
                Consul consulClient;
                // Check if Keycloak token a consul client exists
                if (consulClients.containsKey(keycloakToken)) {
                    consulClient = consulClients.get(keycloakToken);
                }
                else {
                    // Create new consul client for new Keycloak token
                    var consulTokenFromKeycloakToken = this.getConsulToken(consulCredential.getKeycloakPrincipal().getKeycloakSecurityContext().getTokenString());
                    var connectionPool = new ConnectionPool(20,1L, TimeUnit.MILLISECONDS);
                    consulClient = Consul.builder()
                            .withHostAndPort(HostAndPort.fromParts(consulHost, consulPort))
                            .withConnectionPool(connectionPool)
                            .withReadTimeoutMillis(Duration.ofSeconds(2).toMillis())
                            .withTokenAuth(consulTokenFromKeycloakToken)
                            .build();
                    // Check if an old consul client exists for Keycloak user
                    if (this.keycloakTokenClients.containsKey(keycloakName)) {
                        var oldConsulClient = this.keycloakTokenClients.get(keycloakName);
                        oldConsulClient.aclClient().logout();
                    }

                    this.consulClients.put(keycloakToken, consulClient);
                    this.keycloakTokenClients.put(keycloakName, consulClient);
                }

                return consulClient;
            }
        }

        throw new ConsulLoginFailedException("Unable to login to consul with consul credentials: " + consulCredential);
    }

    protected org.springframework.http.HttpHeaders getAuthHeaders(String consulToken)
    {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setBearerAuth(consulToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }

    protected org.springframework.http.HttpHeaders getAuthHeaders(ConsulCredential consulCredential) throws ConsulLoginFailedException {
        return getAuthHeaders(getConsulToken(consulCredential));
    }

    protected String getConsulToken(ConsulCredential consulCredential) throws ConsulLoginFailedException {
        switch (consulCredential.getConsulCredentialType()) {
            case CONSUL_TOKEN -> {
                return consulCredential.getConsulToken();
            }
            case KEYCLOAK_TOKEN -> {
                return getConsulToken(
                        consulCredential.getKeycloakPrincipal().getKeycloakSecurityContext().getTokenString()
                );
            }
            case APPLICATION_PROPERTIES -> {
                return this.consulToken;
            }

            default -> {
                 LOG.error("Unknown ConsulCredentialType '" + consulCredential.getConsulCredentialType() + "'");
                 return "";
            }
        }
    }

    protected String getConsulToken(String token) throws ConsulLoginFailedException {
        if (this.isJwtToken(token)) {
            try {
                var consulClientLogin = Consul.builder()
                        .withHostAndPort(HostAndPort.fromParts(consulHost, consulPort))
                        .withReadTimeoutMillis(Duration.ofSeconds(2).toMillis())
                        .build();
                var response = consulClientLogin.aclClient().login(ImmutableLogin.builder().setBearerToken(token).setAuthMethod("keycloak").build());
                consulClientLogin.destroy();
                return response.getSecretId();
            }
            catch (ConsulException e){
                throw new ConsulLoginFailedException("Consul login at '"+ getConsulUrl() + "/acl/login" +  "' with token '" + token +"' failed: "
                        + e.getMessage());
            }
        }
        else {
            return token;
        }
    }

//   protected void logoutConsulToken(String consulToken) {
//        try {
//            this.bearerAuthInterceptor.setBearerToken(consulToken);
//            this.getConsulClient(consulCredential).aclClient().logout();
//        } catch(HttpClientErrorException e) {
//            if(e.getMessage().equals("401 Unauthorized: [ACL support disabled]")) {
//                LOG.warn("ACL disabled => Not able to logout token = " + consulToken);
//                return;
//            }
//            else
//                LOG.error(e.toString());
//        }
//    }

    protected boolean isJwtToken(String token)
    {
        String[] chunks = token.split("\\.");
        if (chunks.length > 1) {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String header = new String(decoder.decode(chunks[0]));
            String payload = new String(decoder.decode(chunks[1]));

            if (header.contains("JWT")) {
                return true;
            }
        }

        return false;
    }


    public String getConsulUrl() {
        return this.consulScheme + "://" + this.consulHost + ":" + this.consulPort + "/v1";
    }

    public String getConsulHost() {
        return consulHost;
    }

    public void setConsulHost(String consulHost) {
        this.consulHost = consulHost;
    }

    public int getConsulPort() {
        return consulPort;
    }

    public void setConsulPort(int consulPort) {
        this.consulPort = consulPort;
    }

    public String getConsulScheme() {
        return consulScheme;
    }

    public void setConsulScheme(String consulScheme) {
        this.consulScheme = consulScheme;
    }

}
