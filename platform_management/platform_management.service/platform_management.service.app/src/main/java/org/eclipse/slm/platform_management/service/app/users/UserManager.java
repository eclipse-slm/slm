package org.eclipse.slm.platform_management.service.app.users;

public interface UserManager {

    void createUser(UserCreateRequest userCreateRequest);

    void deleteUser(String username);

}
