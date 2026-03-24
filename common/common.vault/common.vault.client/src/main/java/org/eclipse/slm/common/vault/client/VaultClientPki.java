package org.eclipse.slm.common.vault.client;

import org.eclipse.slm.common.restclient.feign.FeignResponseException;
import org.eclipse.slm.common.vault.client.auth.VaultAuthentication;
import org.eclipse.slm.common.vault.client.exceptions.VaultRuntimeException;
import org.eclipse.slm.common.vault.model.mounts.SecretsEngine;
import org.eclipse.slm.common.vault.model.pki.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VaultClientPki extends AbstractVaultClient {
    private final Logger LOG = LoggerFactory.getLogger(VaultClientPki.class);

    protected VaultClientPki(String vaultUrl, VaultAuthentication vaultAuthentication) throws VaultRuntimeException {
        super(vaultUrl, vaultAuthentication);
    }

    public void createRootCa(String pkiName, String commonName, String issuerName) {
        try {
            LOG.info("Enable the pki secrets engine at '{}' path.", pkiName);
            var secretsEngine = new SecretsEngine("pki", "PKI Secrets Engine", null, null);
            this.vaultApiClientSys.createSecretEngine(pkiName, secretsEngine);

            LOG.info("Generate a root CA cert using the /{}/root/generate/internal endpoint", pkiName);
            var generateCaCertRequest = new GenerateCACertRequest(commonName, issuerName);
            this.vaultApiClientPki.generateRootInternalCA(pkiName, generateCaCertRequest);

        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Error adding root CA pki '" + pkiName + "'. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }

    }

    public void createIntermediateCA(String pkiName, String commonName, String issuerName, String rootPkiName) {
        try {
            var pkiNameFull = "pki_int_%s".formatted(pkiName);

            LOG.info("Enable the pki secrets engine at '{}' path.", pkiName);
            var secretsEngine = new SecretsEngine("pki", "PKI Secrets Engine", null, null);
            this.vaultApiClientSys.createSecretEngine(pkiNameFull, secretsEngine);

            LOG.info("Tune the {} secrets engine to issue certificates with a maximum time-to-live (TTL) of 43800h hours.", pkiNameFull);
            var tuneRequest = new TunePkiSecretsEngineRequest("43800h");
            this.vaultApiClientPki.tunePkiSecretsEngine(pkiNameFull, tuneRequest);

            LOG.info("Generate an intermediate cert using the /{}/intermediate/generate/internal endpoint", pkiNameFull);
            var generateIntermediateCertRequest = new GenerateCACertRequest(commonName, issuerName);
            var generateIntermediateCertResponse = this.vaultApiClientPki.generateIntermediate(pkiNameFull, generateIntermediateCertRequest);
            var pkiIntermediateCertCsr = generateIntermediateCertResponse.getData().getCsr();


            LOG.info("Sign the intermediate certificate with the root CA private key, and save the certificate");
            var signIntermediateCertRequest = new SignIntermediateCertRequest(pkiIntermediateCertCsr, "pem_bundle", "43800h");
            var signIntermediateCertResponse = this.vaultApiClientPki.signIntermediateCert(rootPkiName, signIntermediateCertRequest);
            var signedIntermediateCert = signIntermediateCertResponse.getData().getCertificate();

            LOG.info("Import the signed CSR back to Vault ");
            var importSignedIntermediateCertRequest = new ImportSignedIntermediateCertRequest(signedIntermediateCert);
            this.vaultApiClientPki.importSignedIntermediateCert(pkiNameFull, importSignedIntermediateCertRequest);

        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 400) {
                LOG.debug("The intermediate CA pki '{}' (probably) already exists. Status code: {}, message: {}", pkiName, e.getStatusCode(), e.getMessage());
                return;
            }
            throw new VaultRuntimeException("Error adding intermediate CA pki '" + pkiName + "'. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }

    }

    public void deleteIntermediateCA(String pkiName){
        try {
            var pkiNameFull = "pki_int_%s".formatted(pkiName);
            this.vaultApiClientSys.deleteMount(pkiNameFull);

        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Error disabling intermediate CA pki '" + pkiName + "'. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }

    public void createIntermediateRole(String pkiName, List<String> domains, String roleName){
        try {
            var pkiNameFull = "pki_int_%s".formatted(pkiName);

            var issuerRefResponse = this.vaultApiClientPki.getIssuerRef(pkiNameFull);
            var issuerRef = issuerRefResponse.getData().getDefaultValue();

            var roleRequest = new CreateIntermediateRoleRequest(null, true, issuerRef, "43800h");
            this.vaultApiClientPki.createIntermediateRole(pkiNameFull, roleName, roleRequest);
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Error creating intermediate role '" + roleName + "' in pki '" + pkiName + "'. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }

    public void deleteIntermediateRole(String pkiName, String roleName){
        try {
            var pkiNameFull = "pki_int_%s".formatted(pkiName);
            this.vaultApiClientPki.removeRoleFromPki(pkiNameFull, roleName);
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Error removing role '" + roleName + "' from pki '" + pkiName + "'. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }

}
