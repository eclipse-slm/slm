package org.eclipse.slm.common.consul.testing.containers;

import org.testcontainers.containers.GenericContainer;

public class ConsulTestContainer extends GenericContainer<ConsulTestContainer> {

    private final static String CONSUL_IMAGE = "hashicorp/consul:1.22";
    private final static String CONSUL_ROOT_TOKEN = "root";
    private final static String CONSUL_DATACENTER = "dc1";
    private final static String CONSUL_SCHEME = "http";
    private final static String CONSUL_HOST = "localhost";
    private final static int CONSUL_PORT = 8500;
    private final static String CONSUL_LOCAL_CONFIG = "{\"datacenter\": \"" + ConsulTestContainer.CONSUL_DATACENTER + "\", \"bind_addr\": \"0.0.0.0\", \"retry_join\": [\"0.0.0.0\"], \"acl\":{\"enabled\": true, \"default_policy\": \"deny\", \"tokens\":{\"master\": \"" + ConsulTestContainer.CONSUL_ROOT_TOKEN + "\"}}}";

    public ConsulTestContainer() {
        super(ConsulTestContainer.CONSUL_IMAGE);
        this.withEnv("CONSUL_LOCAL_CONFIG", ConsulTestContainer.CONSUL_LOCAL_CONFIG);
        this.withExposedPorts(ConsulTestContainer.CONSUL_PORT);
        this.withExtraHost("localhost", "host-gateway");
    }

    public int getPort() {
        return this.getMappedPort(ConsulTestContainer.CONSUL_PORT);
    }

    public String getUrl() {
        return ConsulTestContainer.CONSUL_SCHEME + "://" + ConsulTestContainer.CONSUL_HOST + ":" + this.getPort();
    }

    public String getDatacenter() {
        return ConsulTestContainer.CONSUL_DATACENTER;
    }

    public String getRootToken() {
        return ConsulTestContainer.CONSUL_ROOT_TOKEN;
    }
}
