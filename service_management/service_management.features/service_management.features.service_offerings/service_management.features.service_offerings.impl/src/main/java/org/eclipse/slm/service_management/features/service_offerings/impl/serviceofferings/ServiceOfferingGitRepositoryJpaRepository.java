package org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferings;

import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.ServiceOfferingGitRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceOfferingGitRepositoryJpaRepository extends JpaRepository<ServiceOfferingGitRepository, UUID>  {

}


