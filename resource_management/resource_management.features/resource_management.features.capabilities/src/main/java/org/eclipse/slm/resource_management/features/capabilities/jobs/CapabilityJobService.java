package org.eclipse.slm.resource_management.features.capabilities.jobs;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CapabilityJobService {

    List<CapabilityJob> getCapabilityJobsOfResource(UUID resourceId);

    void initCapabilityJob(JwtAuthenticationToken jwtAuthenticationToken, UUID resourceId, UUID capabilityId,
                           boolean skipInstall, Map<String, String> configParameters, boolean force) throws Exception;

    void uninstallCapability(JwtAuthenticationToken jwtAuthenticationToken, UUID resourceId, UUID capabilityId) throws Exception;

}
