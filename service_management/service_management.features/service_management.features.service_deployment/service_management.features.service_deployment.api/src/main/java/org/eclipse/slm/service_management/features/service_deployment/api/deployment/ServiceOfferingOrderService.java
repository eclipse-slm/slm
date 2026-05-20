package org.eclipse.slm.service_management.features.service_deployment.api.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.model.MatchingResourceDTO;
import org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.UUID;

public interface ServiceOfferingOrderService {

    void orderServiceOfferingById(UUID serviceOfferingId, UUID serviceOfferingVersionId,
                                  ServiceOrder serviceOrder,
                                  JwtAuthenticationToken jwtAuthenticationToken)
            throws SSLException, JsonProcessingException, ServiceOptionNotFoundException,
            ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException,
            InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException,
            ConsulLoginFailedException;

    List<MatchingResourceDTO> getCapabilityServicesMatchingServiceRequirements(UUID serviceOfferingId,
                                                                               UUID serviceOfferingVersionId,
                                                                               JwtAuthenticationToken jwtAuthenticationToken)
            throws SSLException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException;
}

