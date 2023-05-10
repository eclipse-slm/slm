package org.eclipse.slm.common.vault.model;

import java.util.Map;

public class Auth {
    public String client_token;
    public String accessor;
    public String[] policies;
    public String[] token_policies;
    public String[] identity_policies;
    public Map<String, String> metadata;
    public int lease_duration;
    public boolean renewable;
    public String entity_id;
    public String token_type;
    public boolean orphan;

    public Auth() {
    }

    public Auth(String client_token, String accessor, String[] policies, String[] token_policies, String[] identity_policies, Map<String, String> metadata, int lease_duration, boolean renewable, String entity_id, String token_type, boolean orphan) {
        this.client_token = client_token;
        this.accessor = accessor;
        this.policies = policies;
        this.token_policies = token_policies;
        this.identity_policies = identity_policies;
        this.metadata = metadata;
        this.lease_duration = lease_duration;
        this.renewable = renewable;
        this.entity_id = entity_id;
        this.token_type = token_type;
        this.orphan = orphan;
    }

    public String getClient_token() {
        return client_token;
    }

    public void setClient_token(String client_token) {
        this.client_token = client_token;
    }

    public String getAccessor() {
        return accessor;
    }

    public void setAccessor(String accessor) {
        this.accessor = accessor;
    }

    public String[] getPolicies() {
        return policies;
    }

    public void setPolicies(String[] policies) {
        this.policies = policies;
    }

    public String[] getToken_policies() {
        return token_policies;
    }

    public void setToken_policies(String[] token_policies) {
        this.token_policies = token_policies;
    }

    public String[] getIdentity_policies() {
        return identity_policies;
    }

    public void setIdentity_policies(String[] identity_policies) {
        this.identity_policies = identity_policies;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public int getLease_duration() {
        return lease_duration;
    }

    public void setLease_duration(int lease_duration) {
        this.lease_duration = lease_duration;
    }

    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
    }

    public String getEntity_id() {
        return entity_id;
    }

    public void setEntity_id(String entity_id) {
        this.entity_id = entity_id;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public boolean isOrphan() {
        return orphan;
    }

    public void setOrphan(boolean orphan) {
        this.orphan = orphan;
    }
}
