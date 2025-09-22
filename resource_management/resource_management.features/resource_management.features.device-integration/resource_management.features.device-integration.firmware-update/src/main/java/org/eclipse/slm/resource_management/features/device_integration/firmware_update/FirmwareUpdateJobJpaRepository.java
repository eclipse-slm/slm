package org.eclipse.slm.resource_management.features.device_integration.firmware_update;

import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FirmwareUpdateJobJpaRepository extends JpaRepository<FirmwareUpdateJob, UUID> {

        List<FirmwareUpdateJob> findByResourceIdOrderByCreatedAtDesc(UUID resourceId);

}
