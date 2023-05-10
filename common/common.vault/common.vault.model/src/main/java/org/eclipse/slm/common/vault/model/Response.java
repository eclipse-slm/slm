package org.eclipse.slm.common.vault.model;

import java.util.Map;

public class Response {
    public String request_id;
    public String lease_id;
    public Boolean renewable;
    public int lease_duration;
    public Map<String,Object> data;
    public String wrap_info;
    public String warnings;
    public Auth auth;

    public Response() {
    }

    public Response(String request_id, String lease_id, Boolean renewable, int lease_duration, Map<String, Object> data, String wrap_info, String warnings, Auth auth) {
        this.request_id = request_id;
        this.lease_id = lease_id;
        this.renewable = renewable;
        this.lease_duration = lease_duration;
        this.data = data;
        this.wrap_info = wrap_info;
        this.warnings = warnings;
        this.auth = auth;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getLease_id() {
        return lease_id;
    }

    public void setLease_id(String lease_id) {
        this.lease_id = lease_id;
    }

    public Boolean getRenewable() {
        return renewable;
    }

    public void setRenewable(Boolean renewable) {
        this.renewable = renewable;
    }

    public int getLease_duration() {
        return lease_duration;
    }

    public void setLease_duration(int lease_duration) {
        this.lease_duration = lease_duration;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getWrap_info() {
        return wrap_info;
    }

    public void setWrap_info(String wrap_info) {
        this.wrap_info = wrap_info;
    }

    public String getWarnings() {
        return warnings;
    }

    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }
}
