package org.eclipse.slm.service_management.features.service_offerings.impl.servicevendors;

import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "org.eclipse.slm.service_management.features.service_offerings.impl.servicevendors")
@EntityScan(
        basePackages = {"org.eclipse.slm.service_management.features.service_offerings.api.servicevendors"
        })
@EnableJpaRepositories(
        basePackages = {"org.eclipse.slm.service_management.features.service_offerings.impl.servicevendors"
        })
public class ServiceVendorJpaRepositoryTestConfig {

        @MockBean
        private KeycloakAdminClient keycloakAdminClient;

}
