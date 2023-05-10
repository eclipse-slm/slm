package org.eclipse.slm.catalog.service.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private final static Logger LOG = LoggerFactory.getLogger(OpenApiConfig.class);

    private String version;

    @Autowired
    public OpenApiConfig(@Value("${application.version}") String version) {
        this.version = version;
    }

    @Bean
    public OpenAPI openAPI(
            @Value("${open-api.title}") String title,
            @Value("${open-api.description}") String description,
            @Value("${open-api.version}") String version,
            @Value("${open-api.contact.name}") String contactName,
            @Value("${open-api.contact.url}") String contactUrl,
            @Value("${open-api.contact.email}") String contactMail
            ) {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name(contactName)
                                .url(contactUrl)
                                .email(contactMail)));
    }

}
