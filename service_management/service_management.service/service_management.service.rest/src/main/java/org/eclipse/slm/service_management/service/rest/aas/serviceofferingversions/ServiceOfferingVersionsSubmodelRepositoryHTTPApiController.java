package org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.aas.repositories.api.submodels.MultiSubmodelRepositoryHTTPApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/services/offerings/{serviceOfferingId}/versions/{aasId}/aas")
@Tag(name = "Service Offering Versions")
public class ServiceOfferingVersionsSubmodelRepositoryHTTPApiController extends MultiSubmodelRepositoryHTTPApiController {

    @Autowired
    public ServiceOfferingVersionsSubmodelRepositoryHTTPApiController(ServiceOfferingVersionsSubmodelRepositoryFactory submodelRepositoryFactory) {
        super(submodelRepositoryFactory);
    }

}
