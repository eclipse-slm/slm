package org.eclipse.slm.common.openapi.test.outside;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ExternalServiceAutoConfiguration {
    @Bean
    String externalServiceConnection() {
        throw new IllegalStateException("External service auto-configuration must not run");
    }
}
