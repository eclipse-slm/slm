package org.eclipse.slm.service_management.features.service_offerings.impl.servicecategories;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "org.eclipse.slm.service_management.features.service_offerings.impl.servicecategories")
@EntityScan(
        basePackages = {"org.eclipse.slm.service_management.features.service_offerings.api.servicecategories"
        })
@EnableJpaRepositories(
        basePackages = {"org.eclipse.slm.service_management.features.service_offerings.impl.servicecategories"
        })
public class ServiceOfferingCategoryTestConfig {
}
