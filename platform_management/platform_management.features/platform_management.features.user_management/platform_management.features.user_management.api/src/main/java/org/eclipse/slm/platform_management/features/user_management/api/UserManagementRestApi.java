package org.eclipse.slm.platform_management.features.user_management.api;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

public interface UserManagementRestApi {

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get users")
    ResponseEntity<List<Map<String, Object>>> getUsers();

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add user")
    ResponseEntity<Void> createUser(@RequestBody UserCreateRequest userCreateRequest);

    @RequestMapping(value = "{username}/admin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Grant admin role to user by username")
    ResponseEntity<Void> makeUserAdmin(@PathVariable(name = "username") String username);

    @RequestMapping(value = "{username}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete user by username")
    ResponseEntity<Void> deleteUser(@PathVariable(name = "username") String username);
}

