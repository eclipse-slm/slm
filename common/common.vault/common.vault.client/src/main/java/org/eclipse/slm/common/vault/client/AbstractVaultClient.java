package org.eclipse.slm.common.vault.client;

import org.eclipse.slm.common.restclient.feign.FeignClientFactory;
import org.eclipse.slm.common.vault.client.apiclients.*;
import org.eclipse.slm.common.vault.client.auth.VaultAuthentication;
import org.eclipse.slm.common.vault.client.exceptions.VaultRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractVaultClient {
    private final Logger LOG = LoggerFactory.getLogger(AbstractVaultClient.class);

    protected final String vaultUrl;
    protected final VaultAuthentication vaultAuthentication;

    protected VaultApiClientSys vaultApiClientSys;
    protected VaultApiClientAuth vaultApiClientAuth;
    protected VaultApiClientIdentity vaultApiClientIdentity;
    protected VaultApiClientKv vaultApiClientKv;
    protected VaultApiClientPki vaultApiClientPki;

    protected AbstractVaultClient(String vaultUrl, VaultAuthentication vaultAuthentication) throws VaultRuntimeException {
        try {
            if (!vaultUrl.endsWith("/v1")) {
                vaultUrl += "/v1";
            }
            this.vaultUrl = vaultUrl;
            this.vaultAuthentication = vaultAuthentication;

            var vaultAuthRequestInterceptor = vaultAuthentication.getAuthRequestInterceptor();
            var vaultResponseErrorDecoder = new VaultResponseErrorDecoder();
            this.vaultApiClientSys = FeignClientFactory.createClient(VaultApiClientSys.class, this.vaultUrl, vaultAuthRequestInterceptor, vaultResponseErrorDecoder);
            this.vaultApiClientAuth = FeignClientFactory.createClient(VaultApiClientAuth.class, this.vaultUrl, vaultAuthRequestInterceptor, vaultResponseErrorDecoder);
            this.vaultApiClientIdentity = FeignClientFactory.createClient(VaultApiClientIdentity.class, this.vaultUrl, vaultAuthRequestInterceptor, vaultResponseErrorDecoder);
            this.vaultApiClientKv = FeignClientFactory.createClient(VaultApiClientKv.class, this.vaultUrl, vaultAuthRequestInterceptor, vaultResponseErrorDecoder);
            this.vaultApiClientPki = FeignClientFactory.createClient(VaultApiClientPki.class, this.vaultUrl, vaultAuthRequestInterceptor, vaultResponseErrorDecoder);
        } catch (Exception e) {
            throw new VaultRuntimeException("Could not create VaultClient: " + e.getMessage(), e);
        }
    }
}
