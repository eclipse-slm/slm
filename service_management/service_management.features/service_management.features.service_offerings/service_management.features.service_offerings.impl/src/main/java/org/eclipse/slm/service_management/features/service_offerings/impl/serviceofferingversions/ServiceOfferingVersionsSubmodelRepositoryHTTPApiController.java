package org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferingversions;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.aas.repositories.api.submodels.MultiSubmodelRepositoryHTTPApiController;
import org.eclipse.slm.service_management.features.service_offerings.api.aas.ServiceOfferingVersionsSubmodelRepositoryHTTPApi;
import org.eclipse.slm.service_management.features.service_offerings.api.aas.ServiceOfferingVersionsSubmodelRepositoryHTTPApiConfig;
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

