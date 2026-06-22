package org.eclipse.slm.resource_management.features.capabilities.jobs;

import org.eclipse.slm.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilityServicePersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CapabilityJobExecutorFactory {

    private final AwxJobExecutor awxJobExecutor;

    private final AwxJobObserverInitializer awxJobObserverInitializer;

    private final SingleHostCapabilityServicePersistence singleHostCapabilityServicePersistence;

    private final int awxJobTimeoutInMin;

    public CapabilityJobExecutorFactory(AwxJobExecutor awxJobExecutor,
                                        AwxJobObserverInitializer awxJobObserverInitializer,
                                        SingleHostCapabilityServicePersistence singleHostCapabilityServicePersistence,
                                        @Value("${resource-management.capabilities.awx-job-timeout-in-minutes:20}") int awxJobTimeoutInMin) {
        this.awxJobExecutor = awxJobExecutor;
        this.awxJobObserverInitializer = awxJobObserverInitializer;
        this.singleHostCapabilityServicePersistence = singleHostCapabilityServicePersistence;
        this.awxJobTimeoutInMin = awxJobTimeoutInMin;
    }


    public CapabilityJobExecutor create(CapabilityJob capabilityJob, CapabilityService capabilityService, CapabilityJobExecutorListener capabilityJobExecutorListener) {
        var capabilityJobExecutor = new CapabilityJobExecutor(
                this.awxJobExecutor,
                this.awxJobObserverInitializer,
                this.singleHostCapabilityServicePersistence,
                capabilityJob,
                capabilityService,
                awxJobTimeoutInMin
        );
        capabilityJobExecutor.addCapabilityJobExecutorListener(capabilityJobExecutorListener);

        return capabilityJobExecutor;
    }

}
