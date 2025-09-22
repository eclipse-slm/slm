package org.eclipse.slm.resource_management.features.capabilities.providers;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.features.providers.ServiceHoster;
import org.eclipse.slm.resource_management.features.providers.ServiceHosterFilter;
import org.eclipse.slm.resource_management.features.providers.VirtualResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/resources/providers")
@Tag(name = "Capability Providers")
public class ProvidersRestController {
    private final static Logger LOG = LoggerFactory.getLogger(ProvidersRestController.class);
    private final VirtualResourceProviderHandler virtualResourceProviderHandler;
    private final ServiceHosterHandler serviceHosterHandler;

    public ProvidersRestController(
            VirtualResourceProviderHandler virtualResourceProviderHandler,
            ServiceHosterHandler serviceHosterHandler
    ) {
        this.virtualResourceProviderHandler = virtualResourceProviderHandler;
        this.serviceHosterHandler = serviceHosterHandler;
    }

    @RequestMapping(value = "/virtual-resource-provider", method = RequestMethod.GET)
    @Operation(summary = "Get all virtual resource provider")
    public @ResponseBody
    List<VirtualResourceProvider> getVirtualResourceProviders() {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        try {
            return virtualResourceProviderHandler.getVirtualResourceProviders(
                    new ConsulCredential()
            );
        } catch (ConsulLoginFailedException e) {
            LOG.warn("Failed to log into Consul. Return empty list of virtual resource provider");
            return new ArrayList<>();
        }
    }

    @RequestMapping(value = "/virtual-resource-provider/{virtualResourceProviderId}/vm", method = RequestMethod.POST)
    @Operation(summary = "Create Virtual Machine")
    public @ResponseBody void createVm(@PathVariable(name = "virtualResourceProviderId") UUID virtualResourceProviderId) throws ConsulLoginFailedException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        virtualResourceProviderHandler.createVm(
                new AwxCredential(jwtAuthenticationToken),
                new ConsulCredential(jwtAuthenticationToken),
                virtualResourceProviderId
        );
    }

    @RequestMapping(value = "/service-hoster", method = RequestMethod.GET)
    @Operation(summary = "Get all service hoster")
    public @ResponseBody
    List<ServiceHoster> getServiceHosters(Optional<ServiceHosterFilter> filter) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        try {
            return serviceHosterHandler.getServiceHosters(new ConsulCredential(jwtAuthenticationToken), filter);
        } catch (ConsulLoginFailedException e) {
            LOG.warn("Failed to log into Consul. Return empty list of service hoster.");
            return new ArrayList<>();
        }
    }

}
