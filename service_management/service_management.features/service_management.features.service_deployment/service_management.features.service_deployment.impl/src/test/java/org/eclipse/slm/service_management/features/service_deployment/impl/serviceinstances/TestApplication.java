package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

/**
 * Minimal Spring Boot config for {@code @DataJpaTest}s in this package.
 *
 * <p>The {@code serviceinstances} package also contains {@link ServiceInstanceGroupJpaRepository},
 * which is a pre-existing repository over {@code ServiceInstanceGroup} (not yet a JPA
 * {@code @Entity} - that is Consul-backed and out of scope here). It is excluded from repository
 * scanning so it does not break context loading for these tests.
 */
@SpringBootApplication(scanBasePackages = {
        "org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances",
        "org.eclipse.slm.common.access"
})
@EntityScan(basePackages = {
        "org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances",
        "org.eclipse.slm.common.access"
})
@EnableJpaRepositories(basePackages = {
        "org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances",
        "org.eclipse.slm.common.access"
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ServiceInstanceGroupJpaRepository.class))
@ActiveProfiles("test")
public class TestApplication {}
