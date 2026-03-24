package org.eclipse.slm.resource_management.common.resources;

import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.common.exceptions.ResourceDefinitionException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceRuntimeException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourcesManager {

    List<BasicResource> getResources(String jwtAccessToken) throws ResourceNotFoundException, ResourceRuntimeException;

    Optional<BasicResource> getResourceById(UUID resourceId, String jwtAccessToken) throws ResourceRuntimeException;

    BasicResource getResourceByIdOrThrow(UUID resourceId, String jwtAccessToken) throws ResourceRuntimeException, ResourceNotFoundException;

    BasicResource createResource(
            UUID resourceId,
            String assetId,
            String resourceHostname,
            String resourceIp,
            String firmwareVersion,
            String driverId,
            DigitalNameplateV3 digitalNameplateV3,
            String fullPathOwnerGroupId
    ) throws ResourceNotFoundException, ResourceRuntimeException, ResourceDefinitionException;

    void deleteResource(UUID resourceId, String jwtAccessToken) throws ResourceNotFoundException, ResourceRuntimeException;

    void setLocationOfResource(UUID resourceId, UUID locationId, String jwtAccessToken) throws ConsulLoginFailedException;

    String getConnectionParametersOfResource(UUID resourceId);

    void setConnectionParametersOfResource(UUID resourceId, String connectionParameters);

    void setFirmwareVersionOfResource(UUID resourceId, String firmwareVersion);

    void updateResource(UUID resourceId, ResourceUpdateRequest updateResourceRequest, String jwtAccessToken)
            throws ResourceNotFoundException, ResourceRuntimeException;
}
