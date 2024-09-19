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
import java.util.Objects;
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

    public AbstractConsulApiClient(
            String consulScheme,
            String consulHost,
            int consulPort,
            String consulToken,
            String consulDatacenter
    ) {
        this.consulScheme = consulScheme;
        this.consulHost = consulHost;
        this.consulPort = consulPort;
        this.consulToken = consulToken;
        this.consulDatacenter = consulDatacenter;
    }

    private class ClientsKey {
        String credentialIdentifier;
        String consulHost;

        public ClientsKey(String credentialType, String consulHost) {
            this.credentialIdentifier = credentialType;
            this.consulHost = consulHost;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClientsKey that)) return false;
            return Objects.equals(credentialIdentifier, that.credentialIdentifier) && Objects.equals(consulHost, that.consulHost);
        }

        @Override
        public int hashCode() {
            return Objects.hash(credentialIdentifier, consulHost);
        }
    }

    private Map<ClientsKey, Consul> consulClients = new HashMap<>();

    private Map<ClientsKey, Consul> keycloakTokenClients = new HashMap<>();

    public Consul getConsulClient(ConsulCredential consulCredential, String consulHost, int consulPort) throws ConsulLoginFailedException {
        ClientsKey clientsKey;
        switch (consulCredential.getConsulCredentialType()) {
            case APPLICATION_PROPERTIES -> {
                clientsKey = new ClientsKey(consulCredential.getConsulCredentialType().toString(), consulHost);
                if (consulClients.containsKey(clientsKey)) {
                    return consulClients.get(clientsKey);
                } else {
                    var consulToken = this.consulToken;
                    var consulClient = Consul.builder()
                            .withHostAndPort(HostAndPort.fromParts(consulHost, consulPort))
                            .withReadTimeoutMillis(Duration.ofSeconds(2).toMillis())
                            .withTokenAuth(consulToken)
                            .build();

                    this.consulClients.put(clientsKey, consulClient);
                    return consulClient;
                }
            }

            case CONSUL_TOKEN -> {
                clientsKey = new ClientsKey(consulCredential.getConsulToken(), consulHost);
                if (consulClients.containsKey(clientsKey)) {
                    return consulClients.get(clientsKey);
                } else {
                    var consulClient = Consul.builder()
                            .withHostAndPort(HostAndPort.fromParts(consulHost, consulPort))
                            .withReadTimeoutMillis(Duration.ofSeconds(2).toMillis())
                            .withTokenAuth(consulCredential.getConsulToken())
                            .build();
                    this.consulClients.put(clientsKey, consulClient);
                    return consulClient;
                }
            }

            case JWT -> {
                var jwt = consulCredential.getJwtAuthenticationToken().getToken().getTokenValue();
                var username = consulCredential.getJwtAuthenticationToken().getName();
                clientsKey = new ClientsKey(jwt, consulHost);
                ClientsKey keycloakClientsKey = new ClientsKey(username, consulHost);

                Consul consulClient;
                // Check if Keycloak token a consul client exists
                if (consulClients.containsKey(clientsKey)) {
                    consulClient = consulClients.get(clientsKey);
                }
                else {
                    // Create new consul client for new Keycloak token
                    var consulTokenFromKeycloakToken = this.getConsulToken(jwt);
                    var connectionPool = new ConnectionPool(20,1L, TimeUnit.MILLISECONDS);
                    consulClient = Consul.builder()
                            .withHostAndPort(HostAndPort.fromParts(consulHost, consulPort))
                            .withConnectionPool(connectionPool)
                            .withReadTimeoutMillis(Duration.ofSeconds(2).toMillis())
                            .withTokenAuth(consulTokenFromKeycloakToken)
                            .build();
                    // Check if an old consul client exists for Keycloak user
                    if (this.keycloakTokenClients.containsKey(keycloakClientsKey)) {
                        var oldConsulClient = this.keycloakTokenClients.get(keycloakClientsKey);
                        oldConsulClient.aclClient().logout();
                    }

                    this.consulClients.put(clientsKey, consulClient);
                    this.keycloakTokenClients.put(keycloakClientsKey, consulClient);
                }

                return consulClient;
            }
        }

        throw new ConsulLoginFailedException("Unable to login to consul with consul credentials: " + consulCredential);
    }

    public Consul getConsulClient(ConsulCredential consulCredential) throws ConsulLoginFailedException {
        return getConsulClient(consulCredential, this.consulHost, this.consulPort);
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
            case JWT -> {
                return getConsulToken(
                        consulCredential.getJwtAuthenticationToken().getToken().getTokenValue()
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
