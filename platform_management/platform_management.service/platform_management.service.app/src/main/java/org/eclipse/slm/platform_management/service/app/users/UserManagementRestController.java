package org.eclipse.slm.platform_management.service.app.users;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmAdminOrApiKey;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Void> createUser(UserCreateRequest userCreateRequest) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        this.userManager.createUser(userCreateRequest);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteUser(String username) {
        this.userManager.deleteUser(username);
        return ResponseEntity.ok().build();
    }
}
