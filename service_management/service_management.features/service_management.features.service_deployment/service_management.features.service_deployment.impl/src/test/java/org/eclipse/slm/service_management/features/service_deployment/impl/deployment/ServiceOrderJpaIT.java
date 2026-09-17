package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrderResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Note: adapted from the plan's literal test code. The plan's original annotations
// (@EnableAutoConfiguration + @ContextConfiguration(classes = {ServiceOrderJpaRepository.class}))
// do not provide the module with a Spring Boot main configuration to bootstrap from (this is a
// library module, not an application), so @DataJpaTest cannot infer which packages to scan for
// entities/repositories and spring-cloud-consul-config on the classpath additionally fails fast
// with "No spring.config.import set" without a profile disabling it. This mirrors the working
// pattern already used by ServiceOfferingJpaRepositoryIT in the sibling service_offerings module:
// an explicit @Configuration class with @EntityScan/@EnableJpaRepositories, plus test/resources
// application.yml + application-test.yml copied from that module to disable Consul config lookup
// and point at a Testcontainers-backed MariaDB instance.
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {ServiceOrderJpaRepositoryTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ServiceOrderJpaIT {

    @Autowired
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Test
    @DisplayName("A service order stores the deployment target and the job it started")
    void serviceOrderStoresTargetAndJob() {
        var order = new ServiceOrder();
        order.setServiceInstanceId(UUID.randomUUID());
        order.setServiceOptionValues(new ArrayList<>());
        order.setDeploymentTargetSubmodelId("Deployment-22222222-0000-0000-0000-000000000002");
        order.setDeploymentJobId("deploy-1-abc");
        order.setServiceOrderResult(ServiceOrderResult.SUCCESSFULL);

        var saved = serviceOrderJpaRepository.save(order);
        var reloaded = serviceOrderJpaRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getDeploymentTargetSubmodelId())
                .isEqualTo("Deployment-22222222-0000-0000-0000-000000000002");
        assertThat(reloaded.getDeploymentJobId()).isEqualTo("deploy-1-abc");
    }
}
