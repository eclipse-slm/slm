package org.eclipse.slm.platform_management.service.app.users;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public interface UserManagementRestApi {

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add user")
    @PreAuthorize("hasRole('slm-admin') or hasAuthority('API_KEY')")
    ResponseEntity<Void> createUser(@RequestBody UserCreateRequest userCreateRequest);

    @RequestMapping(value = "{username}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete user by username")
    @PreAuthorize("hasRole('slm-admin')")
    ResponseEntity<Void> deleteUser(@PathVariable(name = "username") String username);
}
