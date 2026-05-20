package org.eclipse.slm.service_management.common.api.users;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;

public interface UsersRestApi {

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get users")
    @ResponseBody
    ResponseEntity<List<User>> getUsers();

    @RequestMapping(value = "/id", method = RequestMethod.GET)
    @Operation(summary = "Get user id of authenticated user")
    @ResponseBody
    UUID getUserIdOfAuthenticatedUser();

    @RequestMapping(value = "/vendors", method = RequestMethod.GET)
    @Operation(summary = "Get service vendors of authenticated user")
    @ResponseBody
    ResponseEntity<List<UUID>> getServiceVendorsOfUser();
}

