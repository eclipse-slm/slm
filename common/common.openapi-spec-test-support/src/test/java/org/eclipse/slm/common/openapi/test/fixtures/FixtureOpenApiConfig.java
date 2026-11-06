package org.eclipse.slm.common.openapi.test.fixtures;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FixtureOpenApiConfig {
    @Bean
    OpenAPI fixtureOpenApi(@Value("${open-api.title}") String title) {
        return new OpenAPI().info(new Info().title(title).version("1"));
    }
}
