package org.eclipse.slm.catalog.persistence.api;

import org.eclipse.slm.catalog.model.AASSubmodelTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AASSubmodelTemplateRepository extends JpaRepository<AASSubmodelTemplate, Long> {
}
