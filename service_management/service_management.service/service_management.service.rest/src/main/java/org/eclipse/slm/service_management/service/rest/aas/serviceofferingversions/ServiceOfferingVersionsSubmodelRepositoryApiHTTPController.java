package org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.aas.repositories.api.MultiSubmodelRepositoryApiHTTPController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/services/offerings/{serviceOfferingId}/versions/{aasId}/aas")
@Tag(name = "Service Offering Version API")
public class ServiceOfferingVersionsSubmodelRepositoryApiHTTPController extends MultiSubmodelRepositoryApiHTTPController {

    @Autowired
    public ServiceOfferingVersionsSubmodelRepositoryApiHTTPController(ServiceOfferingVersionssSubmodelRepositoryFactory submodelRepositoryFactory) {
        super(submodelRepositoryFactory);
    }

}
