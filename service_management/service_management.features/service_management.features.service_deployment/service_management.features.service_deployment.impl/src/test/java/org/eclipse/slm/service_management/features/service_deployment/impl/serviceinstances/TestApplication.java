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
 * <p>{@link ServiceInstanceGroupJpaRepository} lives in this package but maps
 * {@code ServiceInstanceGroup} from the {@code api} package, which this narrow {@code @EntityScan}
 * does not cover. It is excluded from repository scanning so context loading does not fail on an
 * unmanaged type. Widening the scan instead would pull in unrelated entities.
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
