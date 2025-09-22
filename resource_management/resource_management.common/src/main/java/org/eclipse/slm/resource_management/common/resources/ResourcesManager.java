package org.eclipse.slm.resource_management.common.resources;

import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceRuntimeException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.UUID;

public interface ResourcesManager {

    List<BasicResource> getResources(JwtAuthenticationToken jwtAuthenticationToken) throws ResourceNotFoundException, ResourceRuntimeException;

    BasicResource getResourceByIdOrThrow(JwtAuthenticationToken jwtAuthenticationToken, UUID resourceId) throws ResourceRuntimeException, ResourceNotFoundException;

    BasicResource getResourceByIdOrThrow(UUID resourceId) throws ResourceRuntimeException, ResourceNotFoundException;

    BasicResource addResource(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID resourceId,
            String assetId,
            String resourceHostname,
            String resourceIp,
            String firmwareVersion,
            String driverId,
            DigitalNameplateV3 digitalNameplateV3
    ) throws ResourceNotFoundException, ResourceRuntimeException;

    void deleteResource(JwtAuthenticationToken jwtAuthenticationToken, UUID resourceId) throws ResourceNotFoundException, ResourceRuntimeException;

    void setLocationOfResource(UUID resourceId, UUID locationId) throws ConsulLoginFailedException;

    String getConnectionParametersOfResource(UUID resourceId);

    void setConnectionParametersOfResource(UUID resourceId, String connectionParameters);

    void setFirmwareVersionOfResource(UUID resourceId, String firmwareVersion);
}
