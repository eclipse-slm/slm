package org.eclipse.slm.service_management.persistence.api;

import org.eclipse.slm.service_management.model.offerings.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceCategoryJpaRepository extends JpaRepository<ServiceCategory, Long>  {

    List<ServiceCategory> findByName(String name);

}
