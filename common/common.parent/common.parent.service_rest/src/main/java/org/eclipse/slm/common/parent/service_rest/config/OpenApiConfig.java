package org.eclipse.slm.common.parent.service_rest.config;

import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    private final static Logger LOG = LoggerFactory.getLogger(OpenApiConfig.class);

    private String version;

    private MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;

    static {
        io.swagger.v3.core.jackson.ModelResolver.enumsAsRef = true;
    }

    @Autowired
    public OpenApiConfig(@Value("${application.version}") String version,
                         @Autowired MultiTenantKeycloakRegistration multiTenantKeycloakRegistration) {
        this.version = version;
        this.multiTenantKeycloakRegistration = multiTenantKeycloakRegistration;
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
        var firstKeycloakOidcConfig = multiTenantKeycloakRegistration.getFirstOidcConfig();
        var authServerUrl = "";
        var tokenServerUrl = "";
        if (firstKeycloakOidcConfig != null) {
            tokenServerUrl = firstKeycloakOidcConfig.getTokenServerUrl();
            authServerUrl = "";
            try {
                authServerUrl = tokenServerUrl.replace("token", "auth");
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("spring_oauth", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("Oauth2 flow")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(authServerUrl)
                                                .refreshUrl(tokenServerUrl )
                                                .tokenUrl(tokenServerUrl)
                                                .scopes(new Scopes()))
                                )
                            )
                        .addSecuritySchemes("bearer_auth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                            )
                        )
                .security(Arrays.asList(
                        new SecurityRequirement().addList("bearer_auth"),
                        new SecurityRequirement().addList("spring_oauth")
                ))
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
