package org.eclipse.slm.resource_management.features.device_integration.discovery.persistence;

import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveryJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface DiscoveryJobRepository extends JpaRepository<DiscoveryJob, UUID> {
}
