package org.eclipse.slm.common.awx.model;

public class UserCreateRequest {
    String email;
    String first_name;
    String last_name;
    String username;
    String password;
    boolean is_superuser;
    boolean is_system_auditor;

    public UserCreateRequest() {
    }

    public UserCreateRequest(String email, String first_name, String last_name, String username, String password, boolean is_superuser, boolean is_system_auditor) {
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.username = username;
        this.password = password;
        this.is_superuser = is_superuser;
        this.is_system_auditor = is_system_auditor;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
