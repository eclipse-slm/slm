package org.eclipse.slm.service_management.common.api.variables;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public interface ServiceManagementVariablesRestApi {

    @RequestMapping(value = "/deployment", method = RequestMethod.GET)
    @Operation(summary = "Get deployment variables")
    ResponseEntity<DeploymentVariableType[]> getDeploymentVariables();
}

