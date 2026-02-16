package org.eclipse.slm.common.consul.client.testutils;

import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.client.auth.ConsulTokenAuthentication;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;

public class ConsulTestClientFactory {

    private static ConsulClient consulClient;

    public static ConsulClient getConsulClient(ConsulTestContainer consulTestContainer) {
        return new ConsulClient(consulTestContainer.getUrl(), new ConsulTokenAuthentication(consulTestContainer.getRootToken()));
    }


}
