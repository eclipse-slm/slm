package org.eclipse.slm.resource_management.features.capabilities.providers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(ProvidersRestApiConfig.BASE_PATH)
@Tag(name = ProvidersRestApiConfig.TAG)
public class ProvidersRestController implements ProvidersRestApi {
    private final ServiceHosterHandler serviceHosterHandler;

    public ProvidersRestController(
            ServiceHosterHandler serviceHosterHandler
    ) {
        this.serviceHosterHandler = serviceHosterHandler;
    }

    @Override
    public List<ServiceHoster> getServiceHosters(ServiceHosterFilter filter) {
        var optionalFilter = Optional.ofNullable(filter);
        return serviceHosterHandler.getServiceHosters(optionalFilter);
    }

}
