package org.eclipse.slm.service_management.persistence.mariadb.test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@EnableAutoConfiguration
@ComponentScan( basePackages =  { "org.eclipse.slm.service_management.persistence" })
@EntityScan( basePackages = { "org.eclipse.slm.service_management.model" })
@EnableJpaRepositories(basePackages = "org.eclipse.slm.service_management.persistence")
public class SpringTestConfiguration {
}
