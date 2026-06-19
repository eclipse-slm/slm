package org.eclipse.slm.resource_management.common.resources;

import org.eclipse.slm.resource_management.common.access.UserContext;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.common.exceptions.ResourceDefinitionException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceRuntimeException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourcesManager {

    List<BasicResource> getResources(UserContext userContext) throws ResourceNotFoundException, ResourceRuntimeException;

    Optional<BasicResource> getResourceById(UUID resourceId, UserContext userContext) throws ResourceRuntimeException;

    BasicResource getResourceByIdOrThrow(UUID resourceId, UserContext userContext) throws ResourceRuntimeException, ResourceNotFoundException;

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

    void deleteResource(UUID resourceId, UserContext userContext) throws ResourceNotFoundException, ResourceRuntimeException;

    void setLocationOfResource(UUID resourceId, UUID locationId, UserContext userContext);

    String getConnectionParametersOfResource(UUID resourceId);

    void setConnectionParametersOfResource(UUID resourceId, String connectionParameters);

    void setFirmwareVersionOfResource(UUID resourceId, String firmwareVersion);

    void updateResource(UUID resourceId, ResourceUpdateRequest updateResourceRequest, UserContext userContext)
            throws ResourceNotFoundException, ResourceRuntimeException;
}
