package org.eclipse.slm.platform_management.features.user_management.impl;

import org.eclipse.slm.platform_management.features.user_management.api.UserCreateRequest;

import java.util.List;
import java.util.Map;

public interface UserManager {

    void createUser(UserCreateRequest userCreateRequest);

    List<Map<String, Object>> getUsers();

    void makeUserAdmin(String username);

    void deleteUser(String username);
}

