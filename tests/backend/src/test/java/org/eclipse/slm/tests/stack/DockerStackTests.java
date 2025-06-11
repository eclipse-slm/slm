package org.eclipse.slm.tests.stack;

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

    private HashMap<String, String> stackContainersExpectedStatesMap = new HashMap<>() {{
        put("aas-broker", "running");
        put("aas-database", "healthy");
        put("aas-gui", "running");
        put("aas-registry", "running");
        put("aas-environment", "running");
        put("awx-jwt-authenticator", "healthy");
        put("awx-postgres", "healthy");
        put("awx-redis-init", "exited");
        put("awx-redis", "healthy");
        put("awx", "running");
        put("consul", "healthy");
//        put("consul-esm", "running");
        put("keycloak", "healthy");
        put("keycloak-database", "healthy");
        put("monitoring-prometheus-aas", "running");
        put("notification-service-database", "healthy");
        put("notification-service", "healthy");
        put("prometheus", "healthy");
        put("resource-management-database", "healthy");
//        put("resource-management-init", "exited");
        put("resource-management", "healthy");
        put("service-management-database", "healthy");
        put("service-management-init", "exited");
        put("service-management", "healthy");
        put("ui", "running");
        put("vault", "healthy");
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
        long timeout = 300000;
        var stackContainersActualStatesMap = new HashMap<>();
        while (System.currentTimeMillis() < start_time + timeout) {

            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .exec();

            this.stackContainersExpectedStatesMap.entrySet().removeIf(
                    entry ->
                    {
                        var expectedContainerState = entry.getValue();

                        for(var container : containers)
                        {
                            var containerName = Arrays.stream(container.getNames())
                                    .filter(cn -> cn.contains(entry.getKey()))
                                    .findAny();
                            if (containerName.isPresent())
                            {
                                if (expectedContainerState.equals("exited") || expectedContainerState.equals("running"))
                                {
                                    stackContainersActualStatesMap.put(entry, container.getState());
                                    if (container.getState().equals(expectedContainerState))
                                    {
                                        return true;
                                    }
                                }
                                else
                                {
                                    var containerHealth = dockerClient.inspectContainerCmd(container.getId()).exec().getState().getHealth();
                                    if (containerHealth != null)
                                    {
                                        stackContainersActualStatesMap.put(entry, containerHealth.getStatus());
                                        if (containerHealth.getStatus().equals(expectedContainerState))
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

            if (this.stackContainersExpectedStatesMap.size() == 0)
            {
                return;
            }
            else
            {
                LOG.info("The following containers are in wrong state: "
                    + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(stackContainersExpectedStatesMap.keySet())
                + "\n ... sleeping");
                Thread.sleep(10000);
            }
        }

        Assertions.fail("The following containers have the wrong state: "
                + objectMapper.writeValueAsString(this.stackContainersExpectedStatesMap) + "\n Actual container states: "
                + objectMapper.writeValueAsString(stackContainersActualStatesMap));
    }

}
