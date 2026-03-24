package org.eclipse.slm.tests.stack;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;

@DisplayName("Vault")
public class VaultTests {

    @BeforeAll
    public static void init()
    {
        RestAssured.baseURI = TestConfig.VAULT_BASE_URL;
        RestAssured.port = TestConfig.VAULT_PORT;
        RestAssured.basePath = "";
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    @DisplayName("Vault is reachable")
    public void checkVaultReachable() {
        get()
            .then().assertThat() .statusCode(200);
    }

}
