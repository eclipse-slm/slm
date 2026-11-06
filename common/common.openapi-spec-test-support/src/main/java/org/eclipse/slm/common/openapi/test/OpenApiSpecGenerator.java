package org.eclipse.slm.common.openapi.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;

/** Support for opt-in OpenAPI generation tests; never starts the real application. */
public final class OpenApiSpecGenerator {
    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private OpenApiSpecGenerator() { }

    @SpringBootConfiguration
    @ImportAutoConfiguration({ServletWebServerFactoryAutoConfiguration.class, DispatcherServletAutoConfiguration.class,
            WebMvcAutoConfiguration.class, JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
            org.springdoc.core.configuration.SpringDocConfiguration.class,
            org.springdoc.core.properties.SpringDocConfigProperties.class,
            org.springdoc.core.configuration.SpringDocSecurityConfiguration.class,
            org.springdoc.core.configuration.SpringDocPageableConfiguration.class,
            org.springdoc.core.configuration.SpringDocSortConfiguration.class,
            org.springdoc.core.configuration.SpringDocSpecPropertiesConfiguration.class,
            org.springdoc.core.configuration.SpringDocKotlinConfiguration.class,
            org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration.class,
            org.springdoc.webmvc.core.configuration.MultipleOpenApiSupportConfiguration.class})
    public static class GeneratorConfiguration { }

    public static void generate(Class<?> applicationClass, Path destination) throws Exception {
        generate(applicationClass, destination, api -> { });
    }

    public static void generate(Class<?> applicationClass, Path destination, OpenApiCustomizer customizer) throws Exception {
        SpringBootApplication application = AnnotatedElementUtils.findMergedAnnotation(applicationClass, SpringBootApplication.class);
        if (application == null) {
            throw new IllegalArgumentException("Missing @SpringBootApplication on " + applicationClass.getName());
        }
        Set<String> packages = new LinkedHashSet<>(Arrays.asList(application.scanBasePackages()));
        Arrays.stream(application.scanBasePackageClasses()).map(Class::getPackageName).forEach(packages::add);
        if (packages.isEmpty()) {
            packages.add(applicationClass.getPackageName());
        }
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        Set<Class<?>> controllers = new LinkedHashSet<>();
        Set<Class<?>> apiConfigurations = new LinkedHashSet<>();
        for (String basePackage : packages) {
            for (var definition : scanner.findCandidateComponents(basePackage)) {
                controllers.add(Class.forName(definition.getBeanClassName(), false, applicationClass.getClassLoader()));
            }
        }
        ClassPathScanningCandidateComponentProvider configurationScanner = new ClassPathScanningCandidateComponentProvider(false);
        configurationScanner.addIncludeFilter(new AnnotationTypeFilter(Configuration.class));
        for (String basePackage : packages) {
            for (var definition : configurationScanner.findCandidateComponents(basePackage)) {
                Class<?> candidate = Class.forName(definition.getBeanClassName(), false, applicationClass.getClassLoader());
                if (Arrays.stream(candidate.getDeclaredMethods()).anyMatch(method -> method.isAnnotationPresent(Bean.class)
                        && io.swagger.v3.oas.models.OpenAPI.class.isAssignableFrom(method.getReturnType()))) {
                    apiConfigurations.add(candidate);
                }
            }
        }

        var builder = new SpringApplicationBuilder(GeneratorConfiguration.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.config.location=optional:classpath:/openapi-generator/",
                        "spring.cloud.consul.enabled=false", "spring.cloud.discovery.enabled=false",
                        "spring.cloud.config.enabled=false", "spring.rabbitmq.listener.simple.auto-startup=false");
        builder.initializers(context -> {
            var registry = (org.springframework.beans.factory.support.BeanDefinitionRegistry) context.getBeanFactory();
            registry.registerBeanDefinition("serviceOpenApiCustomizer", BeanDefinitionBuilder.genericBeanDefinition(OpenApiCustomizer.class,
                    () -> customizer).getBeanDefinition());
            registry.registerBeanDefinition("stableOpenApiServer", BeanDefinitionBuilder.genericBeanDefinition(OpenApiCustomizer.class,
                    () -> api -> {
                        String url = context.getEnvironment().getProperty("deployment.url", "/");
                        if (api.getServers() == null || api.getServers().isEmpty()
                                || (api.getServers().size() == 1 && "Generated server url".equals(api.getServers().getFirst().getDescription()))) {
                            api.setServers(java.util.List.of(new Server().url(url)));
                        }
                        if (apiConfigurations.isEmpty() && context.getEnvironment().containsProperty("open-api.title")) {
                            var env = context.getEnvironment();
                            api.setInfo(new Info().title(env.getProperty("open-api.title"))
                                    .description(env.getProperty("open-api.description"))
                                    .version(env.getProperty("open-api.version", "1"))
                                    .contact(new Contact().name(env.getProperty("open-api.contact.name"))
                                            .url(env.getProperty("open-api.contact.url"))
                                            .email(env.getProperty("open-api.contact.email"))));
                        }
                    }).getBeanDefinition());
            for (Class<?> controller : controllers) {
                registerMocks(registry, controller);
                registry.registerBeanDefinition(controller.getName(), BeanDefinitionBuilder.genericBeanDefinition(controller).getBeanDefinition());
            }
            for (Class<?> configuration : apiConfigurations) {
                registerMocks(registry, configuration);
                registry.registerBeanDefinition(configuration.getName(), BeanDefinitionBuilder.genericBeanDefinition(configuration).getBeanDefinition());
            }
            try {
                Path resources = Path.of(applicationClass.getProtectionDomain().getCodeSource().getLocation().toURI()).resolve("application.yml");
                if (Files.isRegularFile(resources)) {
                    var sources = new YamlPropertySourceLoader().load("service-application", new FileSystemResource(resources));
                    for (var source : sources) {
                        context.getEnvironment().getPropertySources().addAfter("commandLineArgs", source);
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException("Cannot load service OpenAPI configuration", e);
            }
        });
        try (ConfigurableApplicationContext context = builder.run("--server.port=0", "--server.address=127.0.0.1", "--server.servlet.context-path=",
                "--spring.config.location=optional:classpath:/openapi-generator/", "--spring.cloud.consul.enabled=false",
                "--spring.cloud.discovery.enabled=false", "--spring.cloud.config.enabled=false")) {
            for (Class<?> controller : controllers) {
                if (!context.containsBean(controller.getName())) {
                    throw new IllegalStateException("Discovered controller was not registered: " + controller.getName());
                }
            }
            int port = ((WebServerApplicationContext) context).getWebServer().getPort();
            HttpResponse<String> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs.yaml")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("OpenAPI endpoint returned HTTP " + response.statusCode());
            }
            writeYaml(destination, response.body());
        }
    }

    private static void registerMocks(org.springframework.beans.factory.support.BeanDefinitionRegistry registry, Class<?> type) {
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            for (Parameter parameter : constructor.getParameters()) {
                if (!parameter.isAnnotationPresent(Value.class)) {
                    registerMock(registry, parameter.getType());
                }
            }
        }
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class) && !Modifier.isStatic(field.getModifiers())) {
                    registerMock(registry, field.getType());
                }
            }
        }
    }

    private static <T> void registerMock(org.springframework.beans.factory.support.BeanDefinitionRegistry registry, Class<T> type) {
        String name = "openapiMock." + type.getName();
        if (!registry.containsBeanDefinition(name)) {
            registry.registerBeanDefinition(name, BeanDefinitionBuilder.genericBeanDefinition(type, () -> mock(type)).getBeanDefinition());
        }
    }

    /** Orders unordered OpenAPI maps so regenerating identical APIs yields identical files. */
    private static String canonicalYaml(JsonNode document) throws Exception {
        canonicalise(document);
        return YAML.writeValueAsString(document);
    }

    /** OpenAPI objects are unordered by specification; sorting their keys removes map-order churn. */
    private static JsonNode canonicalise(JsonNode node) {
        if (node instanceof ArrayNode array) {
            for (int index = 0; index < array.size(); index++) {
                array.set(index, canonicalise(array.get(index)));
            }
            return array;
        }
        if (node instanceof ObjectNode object) {
            var sorted = new java.util.TreeMap<String, JsonNode>();
            object.fields().forEachRemaining(entry -> sorted.put(entry.getKey(), canonicalise(entry.getValue())));
            object.removeAll();
            sorted.forEach(object::set);
            return object;
        }
        return node;
    }

    public static void writeYaml(Path destination, String yaml) throws Exception {
        JsonNode document = YAML.readTree(yaml);
        if (document == null || !document.path("openapi").isTextual()
                || !document.path("openapi").asText().matches("3\\.\\d+\\.\\d+")
                || !document.path("info").isObject() || !document.path("info").path("title").isTextual()
                || document.path("info").path("title").asText().isBlank()
                || !document.path("info").path("version").isTextual()
                || document.path("info").path("version").asText().isBlank()
                || !document.path("paths").isObject()) {
            throw new IllegalArgumentException("Invalid OpenAPI document");
        }
        Path target = destination.toAbsolutePath();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), ".openapi-", ".yaml");
        try {
            Files.writeString(temporary, canonicalYaml(document));
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
