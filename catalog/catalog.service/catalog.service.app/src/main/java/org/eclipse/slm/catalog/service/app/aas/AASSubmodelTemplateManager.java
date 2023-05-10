package org.eclipse.slm.catalog.service.app.aas;

import org.eclipse.slm.catalog.model.AASSubmodelTemplate;
import org.eclipse.slm.catalog.persistence.api.AASSubmodelTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AASSubmodelTemplateManager {

    public final static Logger LOG = LoggerFactory.getLogger(AASSubmodelTemplateManager.class);

    private final AASSubmodelTemplateRepository aasSubmodelTemplateRepository;

    public AASSubmodelTemplateManager(AASSubmodelTemplateRepository aasSubmodelTemplateRepository) {
        this.aasSubmodelTemplateRepository = aasSubmodelTemplateRepository;
    }

    public List<AASSubmodelTemplate> getAASSubmodelTemplates() {
        var aasSubmodelTemplates = this.aasSubmodelTemplateRepository.findAll();

        return aasSubmodelTemplates;
    }

    public void addAASSubmodelTemplate(AASSubmodelTemplate aasSubmodelTemplate) {
        this.aasSubmodelTemplateRepository.save(aasSubmodelTemplate);
    }

    public void removeAASSubmodelTemplate(Long aasSubmodelTemplateId) {
        this.aasSubmodelTemplateRepository.deleteById(aasSubmodelTemplateId);
    }
}
