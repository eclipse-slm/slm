package org.eclipse.slm.service_management.service.rest.endpoints;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.common.model.SystemVariable;
import org.eclipse.slm.common.model.SystemVariableValueSource;
import org.eclipse.slm.common.parent.service_rest.controller.SystemVariableHandler;
import org.eclipse.slm.common.parent.service_rest.controller.VariablesRestController;
import org.eclipse.slm.service_management.model.offerings.options.DeploymentVariableType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ServiceManagementVariablesRestController extends VariablesRestController {

    public ServiceManagementVariablesRestController(SystemVariableHandler systemVariableHandler) {
        super(systemVariableHandler);

        systemVariableHandler.addSystemVariable(new SystemVariable("AAS_REGISTRY", "AAS Registry URL",
                SystemVariableValueSource.APPLICATION_PROPERTIES, "aas.aas-registry.proxy"));
        systemVariableHandler.addSystemVariable(new SystemVariable("SUBMODEL_REGISTRY", "Submodel Registry URL",
                SystemVariableValueSource.APPLICATION_PROPERTIES, "aas.submodel-registry.proxy"));
    }

    @RequestMapping(value = "/deployment", method = RequestMethod.GET)
    @Operation(summary = "Get deployment variables")
    public ResponseEntity<DeploymentVariableType[]> getDeploymentVariables() {
        var deploymentVariables = DeploymentVariableType.values();

        return ResponseEntity.ok(deploymentVariables);
    }

}
