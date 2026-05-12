package org.eclipse.slm.platform_management.features.user_management.impl;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmAdminOrApiKey;
import org.eclipse.slm.platform_management.features.user_management.api.UserCreateRequest;
import org.eclipse.slm.platform_management.features.user_management.api.UserManagementRestApi;
import org.eclipse.slm.platform_management.features.user_management.api.UserManagementRestApiConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(UserManagementRestApiConfig.BASE_PATH)
@Tag(name = UserManagementRestApiConfig.TAG)
public class UserManagementRestController implements UserManagementRestApi {

    private final UserManager userManager;

    public UserManagementRestController(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    @AuthorizedAsSlmAdminOrApiKey
    public ResponseEntity<List<Map<String, Object>>> getUsers() {
        return ResponseEntity.ok(this.userManager.getUsers());
    }

    @Override
    @AuthorizedAsSlmAdminOrApiKey
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        this.userManager.createUser(userCreateRequest);
        return ResponseEntity.ok().build();
    }

    @Override
    @AuthorizedAsSlmAdminOrApiKey
    public ResponseEntity<Void> makeUserAdmin(String username) {
        this.userManager.makeUserAdmin(username);
        return ResponseEntity.ok().build();
    }

    @Override
    @AuthorizedAsSlmAdminOrApiKey
    public ResponseEntity<Void> deleteUser(String username) {
        this.userManager.deleteUser(username);
        return ResponseEntity.ok().build();
    }
}

