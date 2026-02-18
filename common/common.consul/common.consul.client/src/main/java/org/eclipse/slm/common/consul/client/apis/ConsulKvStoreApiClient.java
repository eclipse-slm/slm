package org.eclipse.slm.common.consul.client.apis;

import feign.Body;
import feign.Param;
import feign.RequestLine;
import org.eclipse.slm.common.consul.model.kv.KeyValueData;

import java.util.List;

/**
 * Feign-based Consul HTTP API client for Consul KV store operations.
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/kv">Consul API docs</a>.
 */
public interface ConsulKvStoreApiClient {

    /**
     * Read a key from the KV store
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/kv#read-key">Consul API docs</a>.
     * @param key The key to read
     * @param dc Specifies the datacenter to query
     * @param recurse Specifies if the lookup should be recursive and treat key as a prefix
     * @param raw Specifies the response is just the raw value of the key without encoding or metadata
     * @param keys Specifies to return only keys (no values or metadata). Implies recurse
     * @param separator Specifies the string to use as a separator for recursive key lookups (only with keys)
     * @param ns <ENTERPRISE FEATURE> Specifies the namespace to query
     * @param partition <ENTERPRISE FEATURE> The admin partition to use
     * @return The key value data list
     */
    @RequestLine("GET /kv/{key}?dc={dc}&recurse={recurse}&raw={raw}&keys={keys}&separator={separator}&ns={ns}&partition={partition}")
    List<KeyValueData> readKey(@Param("key") String key,
                               @Param("dc") String dc,
                               @Param("recurse") Boolean recurse,
                               @Param("raw") Boolean raw,
                               @Param("keys") Boolean keys,
                               @Param("separator") String separator,
                               @Param("ns") String ns,
                               @Param("partition") String partition);

    /**
     * Put a key into the KV store
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/kv#create-update-key">Consul API docs</a>.
     * @param key The key to put
     * @param payload The value to store
     * @param dc Specifies the datacenter to query
     * @param flags Specifies an unsigned value between 0 and (2^64)-1 to store with the key
     * @param cas Specifies to use a Check-And-Set operation
     * @param acquire Supply a session ID to use in a lock acquisition operation
     * @param release Supply a session ID to use in a release operation
     * @param ns <ENTERPRISE FEATURE> Specifies the namespace to query
     * @param partition <ENTERPRISE FEATURE> The admin partition to use
     * @return True if the operation was successful, false otherwise
     */
    @RequestLine("PUT /kv/{key}?dc={dc}&flags={flags}&cas={cas}&acquire={acquire}&release={release}&ns={ns}&partition={partition}")
    @Body("{payload}")
    boolean putKey(@Param("key") String key,
                   @Param("dc") String dc,
                   @Param("flags") Long flags,
                   @Param("cas") Long cas,
                   @Param("acquire") String acquire,
                   @Param("release") String release,
                   @Param("ns") String ns,
                   @Param("partition") String partition,
                   String payload);

    /**
     * Delete a key from the KV store
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/kv#delete-key">Consul API docs</a>.
     * @param key The key to delete
     * @param dc Specifies the datacenter to query
     * @param recurse Specifies to delete all keys which have the specified prefix
     * @param cas Specifies to use a Check-And-Set operation
     * @param ns <ENTERPRISE FEATURE> Specifies the namespace to query
     * @param partition <ENTERPRISE FEATURE> The admin partition to use
     * @return True if the operation was successful, false otherwise
     */
    @RequestLine("DELETE /kv/{key}?dc={dc}&recurse={recurse}&cas={cas}&ns={ns}&partition={partition}")
    boolean deleteKey(@Param("key") String key,
                      @Param("dc") String dc,
                      @Param("recurse") Boolean recurse,
                      @Param("cas") Integer cas,
                      @Param("ns") String ns,
                      @Param("partition") String partition);


}
