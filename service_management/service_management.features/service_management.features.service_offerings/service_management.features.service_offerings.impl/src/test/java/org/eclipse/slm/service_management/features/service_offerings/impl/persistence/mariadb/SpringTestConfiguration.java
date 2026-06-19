package org.eclipse.slm.service_management.features.service_offerings.impl.persistence.mariadb;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan( basePackages =  { "org.eclipse.slm.service_management" })
@EntityScan( basePackages = { "org.eclipse.slm.service_management.model" })
@EnableJpaRepositories(basePackages = "org.eclipse.slm.service_management")
public class SpringTestConfiguration {
}
