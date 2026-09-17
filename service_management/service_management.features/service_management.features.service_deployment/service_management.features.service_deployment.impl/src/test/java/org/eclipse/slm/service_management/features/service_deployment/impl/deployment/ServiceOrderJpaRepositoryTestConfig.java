package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(
        basePackages = {
                "org.eclipse.slm.service_management.features.service_deployment.api.deployment"
        })
@EnableJpaRepositories(
        basePackages = {
                "org.eclipse.slm.service_management.features.service_deployment.impl.deployment"
        })
public class ServiceOrderJpaRepositoryTestConfig {
}
