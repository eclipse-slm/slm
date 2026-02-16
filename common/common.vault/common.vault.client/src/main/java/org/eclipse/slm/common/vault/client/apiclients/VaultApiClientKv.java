package org.eclipse.slm.common.vault.client.apiclients;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.eclipse.slm.common.vault.model.VaultApiResponse;
import org.eclipse.slm.common.vault.model.kv.KvMetadataCreateRequest;
import org.eclipse.slm.common.vault.model.kv.KvSecretsCreateRequest;
import org.eclipse.slm.common.vault.model.kv.KvSecrets;
import org.eclipse.slm.common.vault.model.kv.KvSubkeys;

import java.util.Map;

public interface VaultApiClientKv {

    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#list-secrets
    @RequestLine("GET /{secretsEngineName}/metadata/{kvPath}?list=true")
    @Headers("Content-Type: application/json")
    VaultApiResponse<Map<String, Object>> listSecretKeysOfPath(@Param("secretsEngineName") String secretsEngineName, @Param("kvPath") String kvPath);

    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-secret-subkeys
    @RequestLine("GET /{secretsEngineName}/subkeys/{kvPath}?version={version}&depth={depth}")
    VaultApiResponse<KvSubkeys> getSecretSubkeys(
            @Param("secretsEngineName") String secretsEngineName,
            @Param("kvPath") String kvPath,
            @Param("version") Integer version,
            @Param("depth") Integer depth
    );

    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-secret-version
    @RequestLine("GET /{secretsEngineName}/data/{kvPath}")
    @Headers("Content-Type: application/json")
    VaultApiResponse<KvSecrets> getSecretsOfPath(
            @Param("secretsEngineName") String secretsEngineName,
            @Param("kvPath") String kvPath
    );

    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#create-update-secret
    @RequestLine("POST /{secretsEngineName}/data/{kvPath}")
    @Headers("Content-Type: application/json")
    @Body("{secretsCreateRequest}")
    void addSecretToKvEngine(
            @Param("secretsEngineName") String secretsEngineName,
            @Param("kvPath") String kvPath,
            KvSecretsCreateRequest secretsCreateRequest
    );

    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#create-update-metadata
    @RequestLine("POST /{secretsEngineName}/metadata/{kvPath}")
    @Headers("Content-Type: application/json")
    @Body("{metadata}")
    void addMetadataToKvEngine(
            @Param("secretsEngineName") String secretsEngineName,
            @Param("kvPath") String kvPath,
            KvMetadataCreateRequest metadata);

    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#delete-secret-versions
    @RequestLine("DELETE /{secretsEngineName}/data/{kvPath}")
    @Headers("Content-Type: application/json")
    void deleteSecretData(@Param("secretsEngineName") String secretsEngineName, @Param("kvPath") String kvPath);

    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#delete-metadata-and-all-versions
    @RequestLine("DELETE /{secretsEngineName}/metadata/{kvPath}")
    @Headers("Content-Type: application/json")
    void deleteSecretMetadata(@Param("secretsEngineName") String secretsEngineName, @Param("kvPath") String kvPath);
}
