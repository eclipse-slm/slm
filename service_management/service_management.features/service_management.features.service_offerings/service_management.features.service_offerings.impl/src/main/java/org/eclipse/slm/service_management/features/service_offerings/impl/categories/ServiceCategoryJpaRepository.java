package org.eclipse.slm.service_management.features.service_offerings.impl.categories;

import org.eclipse.slm.service_management.features.service_offerings.api.categories.ServiceOfferingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceCategoryJpaRepository extends JpaRepository<ServiceOfferingCategory, Long>  {

    List<ServiceOfferingCategory> findByName(String name);

}


