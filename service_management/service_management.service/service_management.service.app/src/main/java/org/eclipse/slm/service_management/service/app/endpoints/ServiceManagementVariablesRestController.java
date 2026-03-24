package org.eclipse.slm.service_management.service.app.endpoints;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.model.SystemVariable;
import org.eclipse.slm.common.model.SystemVariableValueSource;
import org.eclipse.slm.common.parent.service.controller.SystemVariableHandler;
import org.eclipse.slm.common.parent.service.controller.VariablesRestController;
import org.eclipse.slm.service_management.model.offerings.options.DeploymentVariableType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ServiceManagementVariablesRestApiConfig.BASE_PATH)
@Tag(name = ServiceManagementVariablesRestApiConfig.TAG)
public class ServiceManagementVariablesRestController extends VariablesRestController implements ServiceManagementVariablesRestApi {

    public ServiceManagementVariablesRestController(SystemVariableHandler systemVariableHandler) {
        super(systemVariableHandler);

        systemVariableHandler.addSystemVariable(new SystemVariable("AAS_REGISTRY", "AAS Registry URL",
                SystemVariableValueSource.APPLICATION_PROPERTIES, "aas.aas-registry.proxy"));
        systemVariableHandler.addSystemVariable(new SystemVariable("SUBMODEL_REGISTRY", "Submodel Registry URL",
                SystemVariableValueSource.APPLICATION_PROPERTIES, "aas.submodel-registry.proxy"));
    }

    @Override
    public ResponseEntity<DeploymentVariableType[]> getDeploymentVariables() {
        var deploymentVariables = DeploymentVariableType.values();

        return ResponseEntity.ok(deploymentVariables);
    }

}
