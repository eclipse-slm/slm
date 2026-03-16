package org.eclipse.slm.service_management.service.app.aas.serviceofferingversions;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.aas.repositories.api.submodels.MultiSubmodelRepositoryHTTPApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ServiceOfferingVersionsSubmodelRepositoryHTTPApiConfig.BASE_PATH)
@Tag(name = ServiceOfferingVersionsSubmodelRepositoryHTTPApiConfig.TAG)
public class ServiceOfferingVersionsSubmodelRepositoryHTTPApiController
        extends MultiSubmodelRepositoryHTTPApiController
        implements ServiceOfferingVersionsSubmodelRepositoryHTTPApi {

    @Autowired
    public ServiceOfferingVersionsSubmodelRepositoryHTTPApiController(
            ServiceOfferingVersionsSubmodelRepositoryFactory submodelRepositoryFactory
    ) {
        super(submodelRepositoryFactory);
    }
}
