package org.eclipse.slm.resource_management.common.remote_access;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RemoteAccessJpaRepository extends JpaRepository<RemoteAccessEntity, UUID> {

    List<RemoteAccessEntity> findByResourceId(UUID resourceId);
}
