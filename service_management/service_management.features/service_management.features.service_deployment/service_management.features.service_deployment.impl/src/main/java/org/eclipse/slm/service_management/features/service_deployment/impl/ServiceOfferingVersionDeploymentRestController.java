package org.eclipse.slm.service_management.features.service_deployment.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmUser;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmUserOrApiKey;
import org.eclipse.slm.resource_management.common.model.MatchingResourceDTO;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.CapabilityServiceNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOfferingOrderService;
import org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOfferingVersionsRestApiConfig;
import org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ServiceOfferingVersionsRestApiConfig.BASE_PATH)
@Tag(name = ServiceOfferingVersionsRestApiConfig.TAG)
@AuthorizedAsSlmUserOrApiKey
public class ServiceOfferingVersionDeploymentRestController {

    private final ServiceOfferingOrderService serviceOfferingOrderHandler;

    public ServiceOfferingVersionDeploymentRestController(ServiceOfferingOrderService serviceOfferingOrderHandler) {
        this.serviceOfferingOrderHandler = serviceOfferingOrderHandler;
    }

    @RequestMapping(value = "/{serviceOfferingId}/versions/{serviceOfferingVersionId}/order", method = RequestMethod.POST)
    @Operation(summary = "Order service offering version")
    @AuthorizedAsSlmUser
    public ResponseEntity<Void> orderServiceOfferingVersionById(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId,
            @PathVariable(name = "serviceOfferingVersionId") UUID serviceOfferingVersionId,
            @RequestBody ServiceOrder serviceOrder
    ) throws SSLException, JsonProcessingException, ServiceOptionNotFoundException, ServiceOfferingNotFoundException,
            ServiceOfferingVersionNotFoundException, InvalidServiceOfferingDefinitionException, ConsulLoginFailedException {

        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        try {
            this.serviceOfferingOrderHandler.orderServiceOfferingById(
                    serviceOfferingId,
                    serviceOfferingVersionId,
                    serviceOrder,
                    jwtAuthenticationToken);
        } catch (CapabilityServiceNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }

        return ResponseEntity.ok().build();
    }

    @RequestMapping(
            value = "/{serviceOfferingId}/versions/{serviceOfferingVersionId}/matching-resources",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get possible resources matching service requirements")
    @AuthorizedAsSlmUser
    public ResponseEntity<List<MatchingResourceDTO>> getResourcesMatchingServiceRequirements(
            @PathVariable(name = "serviceOfferingId") UUID serviceOfferingId,
            @PathVariable(name = "serviceOfferingVersionId") UUID serviceOfferingVersionId
    ) throws SSLException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var matchingResources = this.serviceOfferingOrderHandler
                .getCapabilityServicesMatchingServiceRequirements(serviceOfferingId, serviceOfferingVersionId, jwtAuthenticationToken);
        return ResponseEntity.ok(matchingResources);
    }
}

