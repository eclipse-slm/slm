package org.eclipse.slm.resource_management.features.capabilities.jobs;

import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.keycloak.client.KeycloakServiceClient;
import org.eclipse.slm.resource_management.features.capabilities.CapabilitiesService;
import org.eclipse.slm.resource_management.features.capabilities.CapabilityUtil;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilitiesVaultClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CapabilityJobExecutorFactory {

    private final CapabilitiesService capabilitiesService;

    private final CapabilityUtil capabilityUtil;

    private final AwxJobExecutor awxJobExecutor;

    private final AwxJobObserverInitializer awxJobObserverInitializer;

    private final SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient;

    private final int awxJobTimeoutInMin;

    public CapabilityJobExecutorFactory(CapabilitiesService capabilitiesService,
                                        CapabilityUtil capabilityUtil,
                                        AwxJobExecutor awxJobExecutor,
                                        AwxJobObserverInitializer awxJobObserverInitializer,
                                        SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient,
                                        @Value("${resource-management.capabilities.awx-job-timeout-in-minutes:20}") int awxJobTimeoutInMin) {
        this.capabilitiesService = capabilitiesService;
        this.capabilityUtil = capabilityUtil;
        this.awxJobExecutor = awxJobExecutor;
        this.awxJobObserverInitializer = awxJobObserverInitializer;
        this.singleHostCapabilitiesConsulClient = singleHostCapabilitiesConsulClient;
        this.awxJobTimeoutInMin = awxJobTimeoutInMin;
    }


    public CapabilityJobExecutor create(CapabilityJob capabilityJob, CapabilityService capabilityService, CapabilityJobExecutorListener capabilityJobExecutorListener) {
        var capabilityJobExecutor = new CapabilityJobExecutor(
                this.capabilitiesService,
                this.capabilityUtil,
                this.awxJobExecutor,
                this.awxJobObserverInitializer,
                this.singleHostCapabilitiesConsulClient,
                capabilityJob,
                capabilityService,
                awxJobTimeoutInMin
        );
        capabilityJobExecutor.addCapabilityJobExecutorListener(capabilityJobExecutorListener);

        return capabilityJobExecutor;
    }

}
