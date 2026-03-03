package org.eclipse.slm.common.keycloak.testing;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.eclipse.slm.common.restclient.feign.ResponseErrorDecoder;

///
/// docker exec -it elated_chaum sh -c "cp -rp /opt/keycloak/data/h2 /tmp ; /opt/keycloak/bin/kc.sh export --file /tmp/testrealm-keycloak.json --realm testrealm --db dev-file --db-url 'jdbc:h2:file:/tmp/h2/keycloakdb;NON_KEYWORDS=VALUE'"
public class KeycloakTestContainer extends KeycloakContainer {

    public final static String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:26.4.0";

    public final static String TEST_REALM = "testrealm";
    public final static String TEST_CLIENT_ID = "testclient";

    public static final String TEST_USER1_USERNAME = "testuser1";
    public static final String TEST_USER1_PASSWORD = "password1";
    public static final String TEST_USER1_ID = "d2eaa306-1d90-486c-a8d1-23d1baac4b2a";
    public static final String TEST_USER1_GROUP_ID = "/users/" + TEST_USER1_ID;
    public static final String TEST_USER2_USERNAME = "testuser2";
    public static final String TEST_USER2_PASSWORD = "password2";
    public static final String TEST_USER2_ID = "583c1c4c-33a0-4d7b-bee6-3d54dd52ba19";
    public static final String TEST_USER2_GROUP_ID = "/users/" + TEST_USER2_ID;

    public KeycloakTestContainer() {
        super(KEYCLOAK_IMAGE);
        this.withRealmImportFile("testrealm-keycloak.json");
    }

    public String getTestRealmIssuerUri() {
        return String.format("%s/realms/%s", this.getAuthServerUrl(), KeycloakTestContainer.TEST_REALM);
    }

    public String getTestRealmAccessTokenForUser(String username, String password) {
        var keycloakTokenClient = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .errorDecoder(new ResponseErrorDecoder())
                .target(KeycloakTokenClient.class, this.getAuthServerUrl());
        var tokenResponse = keycloakTokenClient.passwordGrant(KeycloakTestContainer.TEST_REALM, KeycloakTestContainer.TEST_CLIENT_ID, username, password);

        return tokenResponse.accessToken;
    }

    public String getTestRealmAccessTokenForUser1() {
        return getTestRealmAccessTokenForUser(TEST_USER1_USERNAME, TEST_USER1_PASSWORD);
    }

    public String getTestRealmAccessTokenForUser2() {
        return getTestRealmAccessTokenForUser(TEST_USER2_USERNAME, TEST_USER2_PASSWORD);
    }
}
