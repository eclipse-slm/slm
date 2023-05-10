package org.eclipse.slm.resource_management.service.initializer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication(
        scanBasePackages = {
                "org.eclipse.slm.resource_management.service.initializer",
                "org.eclipse.slm.resource_management.service.client"
        },
        exclude = {
                DataSourceAutoConfiguration.class,
                SecurityAutoConfiguration.class
        }
)
public class Application {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }
}
