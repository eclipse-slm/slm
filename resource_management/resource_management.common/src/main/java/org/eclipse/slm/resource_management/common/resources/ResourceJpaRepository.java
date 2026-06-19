package org.eclipse.slm.resource_management.common.resources;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ResourceJpaRepository extends JpaRepository<BasicResource, UUID> {

    List<BasicResource> findByIdIn(Set<UUID> ids);
}
