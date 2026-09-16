package org.eclipse.slm.resource_management.common.access;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication(scanBasePackages = {
        "org.eclipse.slm.resource_management.common.access",
        "org.eclipse.slm.common.access"
})
@EntityScan(basePackages = {
        "org.eclipse.slm.resource_management.common.access",
        "org.eclipse.slm.common.access"
})
@EnableJpaRepositories(basePackages = {
        "org.eclipse.slm.resource_management.common.access",
        "org.eclipse.slm.common.access"
})
@ActiveProfiles("test")
public class TestApplication {}
