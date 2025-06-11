package org.eclipse.slm.common.vault.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.eclipse.slm.common.vault.model.*;
import org.eclipse.slm.common.vault.model.exceptions.GroupAliasNotFoundException;
import org.eclipse.slm.common.vault.model.exceptions.KvValueNotFound;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import jakarta.annotation.PostConstruct;;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VaultClient {
    private final Logger LOG = LoggerFactory.getLogger(VaultClient.class);

    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    private String scheme;

    private String host;

    private int port;

    private String appRoleId;

    private String appRoleSecretId;

    private String token;

    private String authentication;

    public VaultClient(
            @Value("${vault.scheme}") String scheme,
            @Value("${vault.host}") String host,
            @Value("${vault.port}") int port,
            @Value("${vault.authentication}") String authentication,
            @Value("${vault.token:}") String token,
            @Value("${vault.app-role.role-id:}") String appRoleId,
            @Value("${vault.app-role.secret-id:}") String appRoleSecretId,
            ObjectMapper objectMapper
    ) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.scheme = scheme;
        this.host = host;
        this.port = port;

        this.authentication = authentication;
        this.token = token;
        this.appRoleId = appRoleId;
        this.appRoleSecretId = appRoleSecretId;

        var httpClient = HttpClients.custom()
                .setConnectionManager(
                    PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(
                            SSLConnectionSocketFactoryBuilder.create()
                                .setSslContext(
                                        SSLContexts.custom()
                                        .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                                        .build())
                                    .setHostnameVerifier((s, sslSession) -> true)
                                .build())
                    .build())
                .build();
        var requestFactory =
                new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        this.restTemplate = new RestTemplate(requestFactory);
        this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(this.getBaseUrl()));
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void initVaultClient() {
        this.objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    private String getBaseUrl() {
        var baseUrl = scheme + "://" + host + ":" + port + "/v1";

        return baseUrl;
    }

    public void setPort(int port) {
        this.port = port;
        var baseUrl = this.getBaseUrl();
        this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));
    }

    private HttpEntity loginAndCreateRequestWithBody(VaultCredential vaultCredential, Object body) {

        var serializedBody = "";
        if (body != null) {
            try {
                serializedBody = this.objectMapper.writeValueAsString(body);
            } catch (JsonProcessingException e) {
                LOG.error("Vault login failed: " + e.getMessage());
            }
        }

        switch (vaultCredential.getVaultCredentialType()) {
            case KEYCLOAK_TOKEN -> {
                String url = "/auth/jwt/login";

                var loginBody = "";
                try {
                    loginBody = objectMapper.writeValueAsString(new LoginRequest(vaultCredential.getToken()));
                } catch (JsonProcessingException e) {
                    LOG.error("Vault login failed: " + e.getMessage());
                }
                var loginResponse = restTemplate.postForObject(url, loginBody, Response.class);

                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + loginResponse.getAuth().getClient_token());
                HttpEntity<Object> httpEntity = new HttpEntity<>(serializedBody,headers);

                return httpEntity;
            }

            case CONSUL_TOKEN -> {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + vaultCredential.getToken());

                return new HttpEntity(serializedBody, headers);
            }

            case APPLICATION_PROPERTIES -> {
                if(this.authentication.equalsIgnoreCase("approle")) {
                    String url = "/auth/approle/login";

                    var loginBody = "";
                    try {
                        loginBody = objectMapper.writeValueAsString(new ApproleLoginRequest(this.appRoleId, this.appRoleSecretId));
                    } catch (JsonProcessingException e) {
                        LOG.error("Vault login failed: " + e.getMessage());
                    }
                    var loginResponse = restTemplate.postForObject(url, loginBody, Response.class);

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Authorization", "Bearer " + loginResponse.getAuth().getClient_token());
                    return new HttpEntity(serializedBody, headers);
                }
                else if(this.authentication.equalsIgnoreCase("token"))
                {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Authorization", "Bearer " + token);

                    return new HttpEntity(serializedBody, headers);
                }
                else {
                    throw new IllegalArgumentException("Authentication method '" + this.authentication + "' not supported");
                }
            }

            default -> {
                throw new NotImplementedException("VaultCredentialType '" + vaultCredential.getVaultCredentialType() + "' not implemented");
            }
        }
    }

    public Map<String, String> getKvContentUsingApplicationProperties(VaultCredential vaultCredential, KvPath kvPath) throws KvValueNotFound {
        try {
            var httpEntity  = this.loginAndCreateRequestWithBody(vaultCredential, null);

            String url = "/" + kvPath.getFullPath();
            ResponseEntity<Response> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Response.class);
            var secretsData = (Map<String, String>)responseEntity.getBody().getData().get("data");

            return secretsData;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new KvValueNotFound("KV value at path " +  kvPath.getFullPath() + " not found");
            }
            else {
                throw e;
            }
        }
    }

    public List<String> getKeysOfPath(VaultCredential vaultCredential, String secretEngineName, String path) {
        List<String> returnList = new ArrayList<>();

        try {
            var httpEntity = this.loginAndCreateRequestWithBody(vaultCredential, null);
            ResponseEntity<Response> responseEntity;

            final String url = "/" + secretEngineName + "/metadata/" + path + "?list=true";
            responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    Response.class
            );

            returnList = (List<String>) responseEntity.getBody().getData().get("keys");
        } catch (HttpClientErrorException e) {
            if(e.getStatusCode().equals(HttpStatus.NOT_FOUND))
                return null;
            throw e;
        }


        return returnList;
    }

    public Map<String, String> getKvContent(VaultCredential vaultCredential, KvPath kvPath) {
        var httpEntity = this.loginAndCreateRequestWithBody(vaultCredential, null);

        String url = "/" + kvPath.getFullPath();
        ResponseEntity<Response> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    Response.class
            );
        } catch(HttpClientErrorException e) {
            LOG.warn("There are not secret stored in vault at path: '" + kvPath.getFullPath() + "'. Error Message: " + e.getMessage());
            return new HashMap<>();
        }

        var secretsData = (Map<String, String>)responseEntity.getBody().getData().get("data");

        return secretsData;
    }

    public void createKvSecretEngine(VaultCredential vaultCredential, String path) {
        String url = "/sys/mounts/" + path;
        SecretEngine secretEngine = new SecretEngine();
        secretEngine.setType("kv");
        secretEngine.setOptions(new SecretEngineOption("2"));

        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, secretEngine);
        try {
            ResponseEntity<Response> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Response.class);
        } catch(HttpClientErrorException e) {
            LOG.info("Skip creating SecretEngine at path '" + path +"'. Exists already.");
        }
    }

    public void addSecretToKvEngine(VaultCredential vaultCredential, String engineName, String path, Map<String, String> secret) {
        String url = "/" + engineName + "/data/" + path;
        Map<String, Map> body = new HashMap<>();
        body.put("data", secret);

        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, body);

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
    }

    public void removeSecretFromKvEngine(VaultCredential vaultCredential, String engineName, String path) {
        String url = "/" + engineName + "/metadata/" + path;
        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, null);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, String.class);
    }

    public void addRuleToPolicy(VaultCredential vaultCredential, String policyName, String rule) {

        // setup regex matcher
        String re = "\"((.*\\/+\\*?))\"";
        Pattern pattern = Pattern.compile(re);

        // parse rule to add
        String ruleName = null;
        Matcher matcher = pattern.matcher(rule);
        if (matcher.find()) {
            ruleName = matcher.group(1);
        }

        // get policy first
        String url = "/sys/policy/" + policyName;
        Map<String, String> body = new HashMap<>();
        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, body);

        Policy policy = null;
        HashMap<String, String> parsedRules = new HashMap<String, String>();
        try {
            ResponseEntity<Policy> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Policy.class);
            policy = responseEntity.getBody();

            // parse rules with regex
            var ruleLines = Arrays.asList(policy.getRules().split("\n"));

            for (String ruleLine : ruleLines) {
                matcher = pattern.matcher(ruleLine);
                String ruleNameItem = null;
                if (matcher.find()) {
                    ruleNameItem = matcher.group(1);
                    parsedRules.put(ruleNameItem, ruleLine);
                }
            }
        } catch (HttpClientErrorException error) {
            LOG.error("Caught a HTTP error when requesting the policy '" + policyName + "' at Vault. Will try to add it!");
            error.printStackTrace();
        }

        // append rule if not already in policy
        if (ruleName != null){
            if (parsedRules.containsKey(ruleName)) {
                LOG.warn("A rule for path '" + ruleName + "' is already in policy. Overwriting...");
            }
            parsedRules.put(ruleName, rule);
        }

        if (parsedRules.size() > 0){
            // join rules and add/update policy
            String joinedRules = String.join("\n", parsedRules.values());
            addPolicy(vaultCredential, policyName, joinedRules);
        } else {
            LOG.warn("No rules could be parsed in order to add to the policy. Will not add/update policy '" + policyName + "'(input: rule '" + rule + "')");
        }
    }

    public void removeRuleFromPolicy(VaultCredential vaultCredential, String policyName, String ruleName) {
        // get policy first
        String url = "/sys/policy/" + policyName;
        Map<String, String> body = new HashMap<>();
        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, body);
        ResponseEntity<Policy> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Policy.class);
        Policy policy = responseEntity.getBody();

        // parse rules with regex
        var ruleLines = Arrays.asList(policy.getRules().split("\n"));
        HashMap<String, String> parsedRules = new HashMap<>();
        String re = "\"((.*\\/+\\*?))\"";
        Pattern pattern = Pattern.compile(re);
        for (String ruleLine : ruleLines) {
            Matcher matcher = pattern.matcher(ruleLine);
            String ruleLineName = null;
            if (matcher.find()) {
                ruleLineName = matcher.group(1);
                parsedRules.put(ruleLineName, ruleLine);
            }
        }

        // remove rule if in policy
        if (!parsedRules.containsKey(ruleName)){
            LOG.warn("A rule for path '"+ ruleName + "' is not in policy.");
        } else {
            parsedRules.remove(ruleName);
        }

        // join rules and update policy
        String joinedRules = String.join("\n", parsedRules.values());
        addPolicy(vaultCredential, policyName, joinedRules);
    }

    public void addPolicy(VaultCredential vaultCredential, String policyName, String rules) {
        String url = "/sys/policy/" + policyName;
        Map<String, String> body = new HashMap<>();
        body.put("policy", rules);

        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, body);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
    }

    public void removePolicy(VaultCredential vaultCredential, String name) {
        String url = "/sys/policy/" + name;
        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, null);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, String.class);
    }

    public void assignPolicyToSSPApprole(VaultCredential vaultCredential, String policyName) {
        assignPolicyToApprole(vaultCredential, policyName, this.appRoleId);
    }

    public void assignPolicyToApprole(VaultCredential vaultCredential, String policyName, String approleName) {
        List<String> policies = getApprolePolicies(vaultCredential, approleName);

        if(!policies.contains(policyName)) {
            policies.add(policyName);
            String url = "/auth/approle/role/" + approleName + "/policies";
            Map<String, String> body = new HashMap<>();
            body.put("token_policies", String.join(", ", policies));

            HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, body);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

            LOG.info(policies.toString());
        }
    }

    public List<String> getApprolePolicies(VaultCredential vaultCredential, String approleName) {
        String url = "/auth/approle/role/" + approleName + "/policies";
        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, null);
        ResponseEntity<Response> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Response.class);
        Map<String, Object> data = responseEntity.getBody().getData();

        return (List<String>) data.get("token_policies");
    }

    public void createResourcesKVSecretEngine(VaultCredential vaultCredential) {

        // Add KV Secret Engine (if not exists)
        this.createKvSecretEngine(vaultCredential, "resources");
        // Add Policy for managing the created KV Secret Engine
        this.addPolicy(
                vaultCredential,
                "resource_management",
                "path \"resources/*\" { capabilities = [\"read\", \"list\", \"create\", \"update\", \"delete\"] }"
        );

        if(this.authentication.toLowerCase().equals("approle")) {
            this.assignPolicyToSSPApprole(vaultCredential, "resource_management");
        }
        // Assign project read policy to app role
//        // TODO: Check if role_name must be role name of AWX app role
//        this.assignPolicyToApprole(
//                vaultReadPolicyForProject,
//                this.role_id
//        );

        // TODO Assure keycloak assigns to KV read-policy
    }

    public String getJwtMountAccessor(VaultCredential vaultCredential) {
        String path = "/sys/auth";
        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, null);
        ResponseEntity<Response> responseEntity = restTemplate.exchange(path, HttpMethod.GET, httpEntity, Response.class);
        Map<String, Object> data = responseEntity.getBody().getData();

        var jwtAuthMethod = ((Map<String,Object>)data.get("jwt/"));

        try {
            return jwtAuthMethod.get("accessor").toString();
        } catch(NullPointerException e) {
            return "";
        }
    }

    public void addGroup(VaultCredential vaultCredential, String name, String type, List<String> policies) {
        String path = "/identity/group";
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("type", type);
        body.put("policies", policies);

        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, body);
        ResponseEntity<String> responseEntity = restTemplate.exchange(path, HttpMethod.POST, httpEntity, String.class);
    }

    public void removeGroup(VaultCredential vaultCredential, String groupName) {
        String path = "/identity/group/name/" + groupName;
        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, null);
        ResponseEntity<String> responseEntity = restTemplate.exchange(path, HttpMethod.DELETE, httpEntity, String.class);
    }

    public String getGroupId(VaultCredential vaultCredential, String groupName) {
        String path = "/identity/group/name/" + groupName;
        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, null);
        ResponseEntity<Response> responseEntity = restTemplate.exchange(path, HttpMethod.GET, httpEntity, Response.class);
        Map<String, Object> data = responseEntity.getBody().getData();

        return data.get("id").toString();
    }

    public String getGroupAliasIdByNameAndMountAccessor(VaultCredential vaultCredential, String name, String mountAccessor) throws GroupAliasNotFoundException {
        var path = "/identity/group-alias/id?list=true";

        try {
        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, null);
        ResponseEntity<Response> responseEntity = restTemplate.exchange(path, HttpMethod.GET, httpEntity, Response.class);
        Map<String, Object> keyInfo = (Map<String, Object>) responseEntity.getBody().getData().get("key_info");
        for (var entry : keyInfo.entrySet()) {
            var groupAlias = (Map<String, Object>)entry.getValue();
            if (groupAlias.get("mount_accessor").equals(mountAccessor) && groupAlias.get("name").equals(name)) {
                return entry.getKey();
            }
        } }
        catch (HttpClientErrorException.NotFound e) {
            throw new GroupAliasNotFoundException("Group alias for name '" + name + "' and mount accessor '" + mountAccessor + "' not found");
        }

        throw new GroupAliasNotFoundException("Group alias for name '" + name + "' and mount accessor '" + mountAccessor + "' not found");

    }

    public void addJwtGroupAlias(VaultCredential vaultCredential, String keycloakRole, String mountAccessor, String canonicalIdGroup) {
        String path = "/identity/group-alias";
        Map<String, Object> body = new HashMap<>();
        body.put("name", keycloakRole);
        body.put("mount_accessor", mountAccessor);
        body.put("mount_path", "auth/jwt/");
        body.put("mount_type", "jwt");
        body.put("canonical_id", canonicalIdGroup);

        try {
            HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, body);
            ResponseEntity<String> responseEntity = restTemplate.exchange(path, HttpMethod.POST, httpEntity, String.class);
        } catch (HttpClientErrorException.BadRequest e) {
            if (e.getMessage().contains("combination of mount and group alias name is already in use")) {
                LOG.info("Combination of mount and group alias name already exists --> Creation of JwtGroupAlias skipped");
            }
        }

    }

    private void removeJwtGroupAlias(VaultCredential vaultCredential, String groupAliasId) {
        String path = "/identity/group-alias/id/" + groupAliasId;
        HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, null);
        ResponseEntity<String> responseEntity = restTemplate.exchange(path, HttpMethod.DELETE, httpEntity, String.class);
    }



    public List<String> listSecrets(VaultCredential vaultCredential, String secretEngineName, String secretPath) {
        try {
            String uriPath = "/" + secretEngineName + "/metadata/" + secretPath + "?list=true";
            HttpEntity httpEntity = loginAndCreateRequestWithBody(vaultCredential, null);
            ResponseEntity<Response> responseEntity = restTemplate.exchange(uriPath, HttpMethod.GET, httpEntity, Response.class);
            Map<String, Object> data = responseEntity.getBody().getData();

            return (ArrayList)data.get("keys");
        } catch (HttpClientErrorException.NotFound e) {
            return new ArrayList<>();
        }
    }


    public void initKvEngineAndAddAccessForService(String capabilityServiceId){

        KvPath resourceVaultPath = new KvPath("resources", capabilityServiceId);
        this.addSecretToKvEngine(
                new VaultCredential(),
                resourceVaultPath.getSecretEngine(),
                resourceVaultPath.getPath(),
                new HashMap<String, String>()
        );
        LOG.info("Vault KV engine initialised");

        //region 2: now setup vault rules (for created secrets)

        // add policy for resource
        var resourceSecretsReadPolicyName = "policy_resource_" + capabilityServiceId;
        this.addPolicy(
                new VaultCredential(),
                resourceSecretsReadPolicyName,
                "path \"resources/data/" + capabilityServiceId + "*\" { capabilities = [\"list\", \"read\"] }"
        );

        // add group with link to new read access policy
        var resourceSecretsReadGroupName = "group_resource_" + capabilityServiceId;
        this.addGroup(
                new VaultCredential(),
                resourceSecretsReadGroupName,
                "external",
                Arrays.asList(resourceSecretsReadPolicyName)
        );
        var canonicalIdReadGroup = this.getGroupId(new VaultCredential(), resourceSecretsReadGroupName);

        // Add group alias to link Keycloak role with read access group
        var keycloakRole = "resource_" + capabilityServiceId;
        var mountAccessor = this.getJwtMountAccessor(new VaultCredential());
        if (!mountAccessor.equals(""))
            this.addJwtGroupAlias(
                    new VaultCredential(),
                    keycloakRole,
                    mountAccessor,
                    canonicalIdReadGroup
            );
        else
            LOG.warn("Keycloak mount accessor not available!");

        LOG.info("Vault policy creation done");
        //endregion
    }


    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }
}
