package org.eclipse.slm.common.vault.model;

import java.util.List;

public class SecretEngineConfig {
    String default_lease_ttl;
    String max_lease_ttl;
    boolean force_no_cache = false;
    List<String> audit_non_hmac_request_keys;
    List<String> audit_non_hmac_response_keys;
    String listing_visibility;
    List<String> passthrough_request_headers;
    List<String> allowed_response_headers;

    public SecretEngineConfig() {
    }

    public SecretEngineConfig(String default_lease_ttl, String max_lease_ttl, boolean force_no_cache, List<String> audit_non_hmac_request_keys, List<String> audit_non_hmac_response_keys, String listing_visibility, List<String> passthrough_request_headers, List<String> allowed_response_headers) {
        this.default_lease_ttl = default_lease_ttl;
        this.max_lease_ttl = max_lease_ttl;
        this.force_no_cache = force_no_cache;
        this.audit_non_hmac_request_keys = audit_non_hmac_request_keys;
        this.audit_non_hmac_response_keys = audit_non_hmac_response_keys;
        this.listing_visibility = listing_visibility;
        this.passthrough_request_headers = passthrough_request_headers;
        this.allowed_response_headers = allowed_response_headers;
    }

    public String getDefault_lease_ttl() {
        return default_lease_ttl;
    }

    public void setDefault_lease_ttl(String default_lease_ttl) {
        this.default_lease_ttl = default_lease_ttl;
    }

    public String getMax_lease_ttl() {
        return max_lease_ttl;
    }

    public void setMax_lease_ttl(String max_lease_ttl) {
        this.max_lease_ttl = max_lease_ttl;
    }

    public boolean isForce_no_cache() {
        return force_no_cache;
    }

    public void setForce_no_cache(boolean force_no_cache) {
        this.force_no_cache = force_no_cache;
    }

    public List<String> getAudit_non_hmac_request_keys() {
        return audit_non_hmac_request_keys;
    }

    public void setAudit_non_hmac_request_keys(List<String> audit_non_hmac_request_keys) {
        this.audit_non_hmac_request_keys = audit_non_hmac_request_keys;
    }

    public List<String> getAudit_non_hmac_response_keys() {
        return audit_non_hmac_response_keys;
    }

    public void setAudit_non_hmac_response_keys(List<String> audit_non_hmac_response_keys) {
        this.audit_non_hmac_response_keys = audit_non_hmac_response_keys;
    }

    public String getListing_visibility() {
        return listing_visibility;
    }

    public void setListing_visibility(String listing_visibility) {
        this.listing_visibility = listing_visibility;
    }

    public List<String> getPassthrough_request_headers() {
        return passthrough_request_headers;
    }

    public void setPassthrough_request_headers(List<String> passthrough_request_headers) {
        this.passthrough_request_headers = passthrough_request_headers;
    }

    public List<String> getAllowed_response_headers() {
        return allowed_response_headers;
    }

    public void setAllowed_response_headers(List<String> allowed_response_headers) {
        this.allowed_response_headers = allowed_response_headers;
    }

    @Override
    public String toString() {
        return "SecretEngineConfig{" +
                "default_lease_ttl='" + default_lease_ttl + '\'' +
                ", max_lease_ttl='" + max_lease_ttl + '\'' +
                ", force_no_cache=" + force_no_cache +
                ", audit_non_hmac_request_keys=" + audit_non_hmac_request_keys +
                ", audit_non_hmac_response_keys=" + audit_non_hmac_response_keys +
                ", listing_visibility='" + listing_visibility + '\'' +
                ", passthrough_request_headers=" + passthrough_request_headers +
                ", allowed_response_headers=" + allowed_response_headers +
                '}';
    }
}
