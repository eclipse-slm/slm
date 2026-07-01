package org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferings;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(
        basePackages = {
                "org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings",
                "org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions",
                "org.eclipse.slm.service_management.features.service_offerings.api.servicecategories",
                "org.eclipse.slm.service_management.features.service_offerings.api.servicevendors"
        })
@EnableJpaRepositories(
        basePackages = {
                "org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferings"
        })
public class ServiceOfferingJpaRepositoryTestConfig {
}
