package org.eclipse.slm.service_management.service.rest.endpoints;

import org.eclipse.slm.common.model.TemplateVariable;
import org.eclipse.slm.common.model.TemplateVariableValueSource;
import org.eclipse.slm.common.parent.service_rest.controller.TemplateVariableHandler;
import org.eclipse.slm.common.parent.service_rest.controller.TemplatesRestController;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceManagementTemplatesRestController extends TemplatesRestController {

    public ServiceManagementTemplatesRestController(TemplateVariableHandler templateVariableHandler) {
        super(templateVariableHandler);

        templateVariableHandler.addTemplateVariable(new TemplateVariable("AAS_REGISTRY", "AAS Registry",
                TemplateVariableValueSource.APPLICATION_PROPERTIES, "basyx.aas-registry.url"));
        templateVariableHandler.addTemplateVariable(new TemplateVariable("AAS_SERVER", "AAS Server",
                TemplateVariableValueSource.APPLICATION_PROPERTIES, "basyx.aas-server.url"));
    }

}
