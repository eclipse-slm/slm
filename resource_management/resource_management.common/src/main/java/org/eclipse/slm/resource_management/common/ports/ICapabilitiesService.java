package org.eclipse.slm.resource_management.common.ports;

import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface ICapabilitiesService {

    List<UUID> getCapabilityServiceIdsOfResource(UUID resourceId) throws ResourceNotFoundException;

    boolean isResourceClusterMember(UUID resourceId) throws ResourceNotFoundException;

}
