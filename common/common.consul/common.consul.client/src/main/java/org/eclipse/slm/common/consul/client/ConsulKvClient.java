package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.auth.ConsulAuthentication;
import org.eclipse.slm.common.consul.model.exceptions.ConsulKvEntryNotFoundException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulRuntimeException;
import org.eclipse.slm.common.consul.model.kv.KeyValueData;
import org.eclipse.slm.common.restclient.feign.FeignResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Consul KV Client which provides functionality to interact with Consul KV store.
 */
public class ConsulKvClient extends AbstractConsulClient {

    private final static Logger LOG = LoggerFactory.getLogger(ConsulKvClient.class);

    public ConsulKvClient(String consulUrl, ConsulAuthentication consulAuthentication) {
        super(consulUrl, consulAuthentication);
    }

    /**
     * Reads a key from the KV store.
     * @param key The key to read
     * @param recurse Specifies if the lookup should be recursive and treat key as a prefix
     * @param raw Specifies the response is just the raw value of the key without encoding or metadata
     * @param keys Specifies to return only keys (no values or metadata). Implies recurse
     * @param separator Specifies the string to use as a separator for recursive key lookups (only with keys)
     * @return The key value data list
     */
    public List<KeyValueData> readKey(String key, boolean recurse, boolean raw, boolean keys, String separator) {
        try {
            Boolean recurseParam = recurse ? Boolean.TRUE : null;
            Boolean rawParam = raw ? Boolean.TRUE : null;
            Boolean keysParam = keys ? Boolean.TRUE : null;
            return this.consulKvStoreApiClient.readKey(
                    key,
                    this.consulDatacenter,
                    recurseParam,
                    rawParam,
                    keysParam,
                    separator,
                    null,
                    null
            );
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new ConsulKvEntryNotFoundException("Key '" + key + "' not found", e);
            }
            else {
                throw new ConsulRuntimeException("Error reading key '" + key + "'", e);
            }
        }
    }

    /**
     * Creates or updates a key in the KV store.
     * @param key The key to put
     * @param payload The value to store
     * @return True if the operation was successful, false otherwise
     */
    public boolean putKey(String key, String payload) {
        try {
            return this.consulKvStoreApiClient.putKey(key, this.consulDatacenter, null, null, null, null, null, null, payload);
        } catch (FeignResponseException e) {
            throw new ConsulRuntimeException("Error putting key '" + key + "'", e);
        }
    }

    /**
     * Deletes a key (or keys if recurse is true) from the KV store.
     * @param key The key to delete
     * @param recurse Specifies if the deletion should be recursive and treat key as a prefix
     * @return True if the deletion was successful, false otherwise
     */
    public boolean deleteKey(String key, Boolean recurse) {
        try {
            return this.consulKvStoreApiClient.deleteKey(key, this.consulDatacenter, recurse, null, null, null);
        } catch (FeignResponseException e) {
            throw new ConsulRuntimeException("Error deleting key '" + key + "'", e);
        }
    }


}
