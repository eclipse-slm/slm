package org.eclipse.slm.common.awx.model;

import java.util.Map;

public class TokenCreateResponse {
    int id;
    String type;
    Map<String, String> related;
    Map<String, Object> summary_fields;
    String created;
    String modified;
    String description;
    int user;
    String token;
    String refresh_token;
    int application;
    String expires;
    String scope;

    public TokenCreateResponse() {
    }

    public TokenCreateResponse(int id, String type, Map<String, String> related, Map<String, Object> summary_fields, String created, String modified, String description, int user, String token, String refresh_token, int application, String expires, String scope) {
        this.id = id;
        this.type = type;
        this.related = related;
        this.summary_fields = summary_fields;
        this.created = created;
        this.modified = modified;
        this.description = description;
        this.user = user;
        this.token = token;
        this.refresh_token = refresh_token;
        this.application = application;
        this.expires = expires;
        this.scope = scope;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getRelated() {
        return related;
    }

    public void setRelated(Map<String, String> related) {
        this.related = related;
    }

    public Map<String, Object> getSummary_fields() {
        return summary_fields;
    }

    public void setSummary_fields(Map<String, Object> summary_fields) {
        this.summary_fields = summary_fields;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public int getApplication() {
        return application;
    }

    public void setApplication(int application) {
        this.application = application;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
