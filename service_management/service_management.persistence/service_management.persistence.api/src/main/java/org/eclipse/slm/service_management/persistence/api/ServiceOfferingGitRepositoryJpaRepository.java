package org.eclipse.slm.service_management.persistence.api;

import org.eclipse.slm.service_management.model.offerings.ServiceOffering;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingGitRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceOfferingGitRepositoryJpaRepository extends JpaRepository<ServiceOfferingGitRepository, UUID>  {

}
