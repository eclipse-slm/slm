package org.eclipse.slm.service_management.service.rest;

import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {
        "org.eclipse.slm.service_management",
        "org.eclipse.slm.common",
        "org.eclipse.slm.common.parent.service_rest",
        "org.eclipse.slm.notification_service.messaging",
        "org.eclipse.slm.resource_management.service.client"
})
@EntityScan(basePackages = {
        "org.eclipse.slm.service_management.model"
})
@EnableJpaRepositories(basePackages = {
        "org.eclipse.slm.service_management.persistence"
})
@EnableAsync
@EnableTransactionManagement
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        DTOConfig.configureModelMapper();
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
        };
    }

}
