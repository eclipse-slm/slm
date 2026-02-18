package org.eclipse.slm.common.vault.client;

import org.eclipse.slm.common.restclient.feign.FeignClientFactory;
import org.eclipse.slm.common.restclient.feign.auth.BearerTokenAuthRequestInterceptor;
import org.eclipse.slm.common.vault.client.apiclients.VaultApiClientPki;
import org.eclipse.slm.common.vault.client.apiclients.VaultApiClientSys;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class VaultClientPkiTest {

    @Container
    private static final VaultTestContainer vaultContainer = new VaultTestContainer();

    private VaultClient vaultAdminClient;

    private final static String rootPkiName = "testrootca";
    private final static String intPkiName = "testintermediateca";

    private static VaultApiClientSys vaultApiClientSys;

    private static VaultApiClientPki vaultApiClientPki;

    @BeforeAll
    void setUp() {
        vaultAdminClient = vaultContainer.getVaultClient();
        vaultApiClientSys = FeignClientFactory.createClient(
                VaultApiClientSys.class,
                vaultContainer.getVaultUrl(),
                new BearerTokenAuthRequestInterceptor(vaultContainer.getRootToken()));
        vaultApiClientPki = FeignClientFactory.createClient(
                VaultApiClientPki.class,
                vaultContainer.getVaultUrl(),
                new BearerTokenAuthRequestInterceptor(vaultContainer.getRootToken()));
    }

    @Nested
    @Order(10)
    class CreateRootCA {
        @Test
        void shouldCreateRootCA() {
            // Act
            vaultAdminClient.pki().createRootCa(rootPkiName, "Test Root CA", "TestIssuerRoot");
            // Assert
            //TODO: Add assertions
        }
    }

    @Nested
    @Order(20)
    class CreateIntermediateCA {
        @Test
        void shouldCreateIntermediateCA() {
            // Act
            vaultAdminClient.pki().createIntermediateCA(intPkiName, "Intermediate CA Test", "TestIssuerIntermediate", rootPkiName);
            // Assert
            //TODO: Add assertions
        }
    }


    @Nested
    @Order(30)
    class CreateIntermediateRole {
        @Test
        void shouldCreateIntermediateRole() {
            // Act
            vaultAdminClient.pki().createIntermediateRole(intPkiName, List.of("testintermediatereadrole"), "pki_read");
            // Assert
            //TODO: Add assertions
        }
    }

    @Nested
    @Order(40)
    class DeleteIntermediateRole {
        @Test
        void shouldDeleteIntermediateRole() {
            // Act
            vaultAdminClient.pki().deleteIntermediateRole(intPkiName, "pki_read");
            // Assert
            //TODO: Add assertions
        }

    }

    @Order(50)
    @Nested
    class DeleteIntermediateCA {
        @Test
        void shouldDeleteIntermediateCA() {
            // Act
            vaultAdminClient.pki().deleteIntermediateCA(intPkiName);
            // Assert
            //TODO: Add assertions
        }
    }

}
