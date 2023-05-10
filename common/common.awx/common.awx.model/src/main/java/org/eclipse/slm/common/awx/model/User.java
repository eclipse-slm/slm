package org.eclipse.slm.common.awx.model;

import java.util.List;
import java.util.Map;

public class User {
    int id;
    String type;
    Map<String, String> related;
    Map<String,Object> summery_fields;
    String created;
    String username;
    String first_name;
    String last_name;
    String email;
    boolean is_superuser;
    boolean is_system_auditor;
    String ldap_dn;
    String last_login;
    String external_account;
    List<Map<String, String>> auth;

    public User() {
    }

    public User(int id, String type, Map<String, String> related, Map<String, Object> summery_fields, String created, String username, String first_name, String last_name, String email, boolean is_superuser, boolean is_system_auditor, String ldap_dn, String last_login, String external_account, List<Map<String, String>> auth) {
        this.id = id;
        this.type = type;
        this.related = related;
        this.summery_fields = summery_fields;
        this.created = created;
        this.username = username;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.is_superuser = is_superuser;
        this.is_system_auditor = is_system_auditor;
        this.ldap_dn = ldap_dn;
        this.last_login = last_login;
        this.external_account = external_account;
        this.auth = auth;
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

    public Map<String, Object> getSummery_fields() {
        return summery_fields;
    }

    public void setSummery_fields(Map<String, Object> summery_fields) {
        this.summery_fields = summery_fields;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isIs_superuser() {
        return is_superuser;
    }

    public void setIs_superuser(boolean is_superuser) {
        this.is_superuser = is_superuser;
    }

    public boolean isIs_system_auditor() {
        return is_system_auditor;
    }

    public void setIs_system_auditor(boolean is_system_auditor) {
        this.is_system_auditor = is_system_auditor;
    }

    public String getLdap_dn() {
        return ldap_dn;
    }

    public void setLdap_dn(String ldap_dn) {
        this.ldap_dn = ldap_dn;
    }

    public String getLast_login() {
        return last_login;
    }

    public void setLast_login(String last_login) {
        this.last_login = last_login;
    }

    public String getExternal_account() {
        return external_account;
    }

    public void setExternal_account(String external_account) {
        this.external_account = external_account;
    }

    public List<Map<String, String>> getAuth() {
        return auth;
    }

    public void setAuth(List<Map<String, String>> auth) {
        this.auth = auth;
    }
}
