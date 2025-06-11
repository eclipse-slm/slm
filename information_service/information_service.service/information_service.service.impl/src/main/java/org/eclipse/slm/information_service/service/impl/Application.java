package org.eclipse.slm.information_service.service.impl;

import org.eclipse.slm.common.messaging.resources.ResourceMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication( scanBasePackages = {
        "org.eclipse.slm.information_service",
        "org.eclipse.slm.common.aas",
        "org.eclipse.slm.common.messaging"
    },
        exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
    }
)
public class Application {

    @Autowired
    private ResourceMessageListener resourceMessageListener;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}
