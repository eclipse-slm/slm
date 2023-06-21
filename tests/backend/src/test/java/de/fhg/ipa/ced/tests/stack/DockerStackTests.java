package de.fhg.ipa.ced.tests.stack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@DisplayName("Docker Stack")
public class DockerStackTests {

    private static final Logger LOG = LoggerFactory.getLogger(DockerStackTests.class);

    private HashMap<String, String> stackContainerStateMap = new HashMap<>() {{
        put("awx-init", "exited");
        put("awx-web", "healthy");
        put("awx-task", "running");
        put("awx-redis", "healthy");
        put("awx-redis-init", "exited");
        put("awx-postgres", "healthy");
        put("awx-jwt-authenticator", "healthy");
        put("consul", "healthy");
        put("consul-esm", "running");
        put("keycloak", "healthy");
        put("keycloak-database", "healthy");
        put("keycloak-init", "exited");
        put("vault", "healthy");
        put("ui", "unhealthy");
        put("notification-service", "healthy");
        put("notification-service-database", "healthy");
        put("resource-registry", "healthy");
        put("service-registry", "healthy");
        put("service-registry-database", "healthy");
        put("service-registry-init", "exited");
    }};

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Stack Running")
    public void stackRunning() throws InterruptedException, JsonProcessingException {
        var dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(TestConfig.DOCKER_HOST)
                .build();
        var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(dockerClientConfig, httpClient);

        long start_time = System.currentTimeMillis();
        long timeout = 600000;
        while (System.currentTimeMillis() < start_time + timeout) {

            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .exec();

            this.stackContainerStateMap.entrySet().removeIf(
                    entry ->
                    {
                        var targetContainerState = entry.getValue();

                        for(var container : containers)
                        {
                            var containerName = Arrays.stream(container.getNames())
                                    .filter(cn -> cn.contains(entry.getKey()))
                                    .findAny();
                            if (containerName.isPresent())
                            {
                                if (targetContainerState.equals("exited") || targetContainerState.equals("running"))
                                {
                                    if (container.getState().equals(targetContainerState))
                                    {
                                        return true;
                                    }
                                }
                                else
                                {
                                    var containerHealth = dockerClient.inspectContainerCmd(container.getId()).exec().getState().getHealth();
                                    if (containerHealth != null)
                                    {
                                        if (containerHealth.getStatus().equals(targetContainerState))
                                        {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }

                        return false;
                    }
            );

            if (this.stackContainerStateMap.size() == 0)
            {
                return;
            }
            else
            {
                LOG.info("The following containers are in wrong state: "
                    + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(stackContainerStateMap.keySet())
                + "\n ... sleeping");
                Thread.sleep(10000);
            }
        }

        Assertions.fail("The following containers have the wrong state: "
                + objectMapper.writeValueAsString(this.stackContainerStateMap));
    }

}
