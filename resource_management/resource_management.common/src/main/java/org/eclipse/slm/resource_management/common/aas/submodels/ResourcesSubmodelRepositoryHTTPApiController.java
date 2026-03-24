package org.eclipse.slm.resource_management.common.aas.submodels;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.aas.repositories.api.submodels.MultiSubmodelRepositoryHTTPApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resources/{aasId}/aas")
@Tag(name = "Resources AAS")
public class ResourcesSubmodelRepositoryHTTPApiController extends MultiSubmodelRepositoryHTTPApiController {

    @Autowired
    public ResourcesSubmodelRepositoryHTTPApiController(ResourcesSubmodelRepositoryFactory submodelRepositoryFactory) {
        super(submodelRepositoryFactory);
    }

}
