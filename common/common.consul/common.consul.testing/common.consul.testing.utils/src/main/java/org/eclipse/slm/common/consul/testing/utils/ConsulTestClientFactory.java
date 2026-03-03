package org.eclipse.slm.common.consul.testing.utils;

import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.client.auth.ConsulTokenAuthentication;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;

public class ConsulTestClientFactory {

    public static ConsulClient getConsulClient(ConsulTestContainer consulTestContainer) {
        return new ConsulClient(consulTestContainer.getUrl(), new ConsulTokenAuthentication(consulTestContainer.getRootToken()));
    }

    public static ConsulClientFactory getConsulClientFactory(ConsulTestContainer consulTestContainer) {
        return new ConsulClientFactory(consulTestContainer.getUrl(), consulTestContainer.getRootToken(), consulTestContainer.getDatacenter());
    }

}
