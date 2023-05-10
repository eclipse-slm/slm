package org.eclipse.slm.catalog.service.app;

import org.eclipse.slm.catalog.model.AASSubmodelTemplate;
import org.eclipse.slm.catalog.persistence.api.AASSubmodelTemplateRepository;
import org.eclipse.slm.catalog.service.app.aas.AASSubmodelTemplatesRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PostConstruct;

@SpringBootApplication(scanBasePackages = {
        "org.eclipse.slm.catalog"
}, exclude = SecurityAutoConfiguration.class)
@EntityScan(basePackages = { "org.eclipse.slm.catalog.model" })
@EnableJpaRepositories(basePackages = "org.eclipse.slm.catalog.persistence")
public class Application {

    @Autowired
    public AASSubmodelTemplateRepository aasSubmodelTemplateRepository;

    @Autowired
    public AASSubmodelTemplatesRestController assSubmodelTemplatesRestController;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void init() {
        var assSubmodelTemplate1 = new AASSubmodelTemplate("fabos_3dscanner", "3D Scanner");
        var assSubmodelTemplate2 = new AASSubmodelTemplate("fabos_camera", "Camera");
        var assSubmodelTemplate3 = new AASSubmodelTemplate("fabos_robot", "Robot");

        this.aasSubmodelTemplateRepository.save(assSubmodelTemplate1);
        this.aasSubmodelTemplateRepository.save(assSubmodelTemplate2);
        this.aasSubmodelTemplateRepository.save(assSubmodelTemplate3);

    }

}
