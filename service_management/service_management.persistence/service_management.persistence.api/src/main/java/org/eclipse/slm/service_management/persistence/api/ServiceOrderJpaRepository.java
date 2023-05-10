package org.eclipse.slm.service_management.persistence.api;

import org.eclipse.slm.service_management.model.offerings.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrder, UUID>  {

    List<ServiceOrder> findByServiceInstanceId(UUID serviceInstanceId);

}
