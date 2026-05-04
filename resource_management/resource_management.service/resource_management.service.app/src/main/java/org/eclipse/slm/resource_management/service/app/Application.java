package org.eclipse.slm.resource_management.service.app;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinFeature;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.eclipse.digitaltwin.basyx.http.SerializationExtension;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.concurrent.Executor;

@SpringBootApplication(scanBasePackages = {
        "org.eclipse.slm.resource_management.service",
        "org.eclipse.slm.resource_management.common",
        "org.eclipse.slm.resource_management.features.capabilities",
        "org.eclipse.slm.resource_management.features.device_integration",
        "org.eclipse.slm.resource_management.features.device_integration.firmware_update",
        "org.eclipse.slm.resource_management.features.metrics",
        "org.eclipse.slm.resource_management.features.profiler",
        "org.eclipse.slm.resource_management.features.importer",
        "org.eclipse.slm.platform_management.service.client",
        "org.eclipse.slm.notification_service.messaging",
        "org.eclipse.slm.aas",
        "org.eclipse.slm.awx",
        "org.eclipse.slm.common.consul",
        "org.eclipse.slm.common.credentials",
        "org.eclipse.slm.common.keycloak",
        "org.eclipse.slm.common.minio",
        "org.eclipse.slm.common.model",
        "org.eclipse.slm.common.parent",
        "org.eclipse.slm.common.utils",
        "org.eclipse.slm.common.vault",
        "org.eclipse.slm.common.messaging"
}
)
@EntityScan(basePackages = {
        "org.eclipse.slm.common.credentials",
        "org.eclipse.slm.resource_management.common",
        "org.eclipse.slm.resource_management.features.capabilities",
        "org.eclipse.slm.resource_management.features.device_integration.discovery",
        "org.eclipse.slm.resource_management.features.device_integration.firmware_update",
        "org.eclipse.slm.resource_management.features.metrics",
        "org.eclipse.slm.resource_management.features.profiler",
        "org.eclipse.slm.resource_management.features.importer",
})
@EnableJpaRepositories(basePackages = {
        "org.eclipse.slm.common.credentials",
        "org.eclipse.slm.resource_management.common",
        "org.eclipse.slm.resource_management.features.capabilities",
        "org.eclipse.slm.resource_management.features.device_integration.discovery",
        "org.eclipse.slm.resource_management.features.device_integration.firmware_update",
        "org.eclipse.slm.resource_management.features.metrics",
        "org.eclipse.slm.resource_management.features.profiler",
        "org.eclipse.slm.resource_management.features.importer",
})
@EnableAsync
@EnableDiscoveryClient
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    @EventListener(ApplicationReadyEvent.class)
    public void init()
    {
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args ->{
        };
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("SpringAsync-");
        executor.initialize();
        return executor;
    }

    @Bean
    public WebClient.Builder webClientBuilder() throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        TcpClient tcpClient = TcpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        HttpClient httpClient = HttpClient.from(tcpClient);
        var webClientBuilder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));

        return webClientBuilder;
    }

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder(List<SerializationExtension> serializationExtensions) {
        KotlinModule kotlinModule = new KotlinModule.Builder()
                .configure(KotlinFeature.NullIsSameAsDefault, true)
                .build();

        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modules(List.of(new JavaTimeModule(), kotlinModule));

        for (SerializationExtension serializationExtension : serializationExtensions) {
            serializationExtension.extend(builder);
        }

        return builder;
    }

}
