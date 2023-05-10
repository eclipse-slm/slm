package org.eclipse.slm.notification_service.service.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = { "org.eclipse.slm.notification_service", "org.eclipse.slm.notification_service.communication.websocket", "org.eclipse.slm.common" })
@EntityScan(basePackages = { "org.eclipse.slm.notification_service.model" })
@EnableJpaRepositories(basePackages = "org.eclipse.slm.notification_service.persistence")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
