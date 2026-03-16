package org.eclipse.slm.resource_management.features.capabilities.providers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(ProvidersRestApiConfig.BASE_PATH)
@Tag(name = ProvidersRestApiConfig.TAG)
public class ProvidersRestController implements ProvidersRestApi {
    private final static Logger LOG = LoggerFactory.getLogger(ProvidersRestController.class);
    private final ServiceHosterHandler serviceHosterHandler;

    public ProvidersRestController(
            ServiceHosterHandler serviceHosterHandler
    ) {
        this.serviceHosterHandler = serviceHosterHandler;
    }

    @Override
    public List<ServiceHoster> getServiceHosters(ServiceHosterFilter filter) {
        try {
            var optionalFilter = Optional.ofNullable(filter);
            return serviceHosterHandler.getServiceHosters(optionalFilter);
        } catch (ConsulLoginFailedException e) {
            LOG.warn("Failed to log into Consul. Return empty list of service hoster.");
            return new ArrayList<>();
        }
    }

}
