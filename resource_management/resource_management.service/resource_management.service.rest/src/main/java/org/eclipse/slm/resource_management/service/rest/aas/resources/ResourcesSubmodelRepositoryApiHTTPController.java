package org.eclipse.slm.resource_management.service.rest.aas.resources;

import org.eclipse.slm.common.aas.repositories.api.MultiSubmodelRepositoryApiHTTPController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resources/{aasId}/aas")
public class ResourcesSubmodelRepositoryApiHTTPController extends MultiSubmodelRepositoryApiHTTPController {

    @Autowired
    public ResourcesSubmodelRepositoryApiHTTPController(ResourcesSubmodelRepositoryFactory submodelRepositoryFactory) {
        super(submodelRepositoryFactory);
    }

}
