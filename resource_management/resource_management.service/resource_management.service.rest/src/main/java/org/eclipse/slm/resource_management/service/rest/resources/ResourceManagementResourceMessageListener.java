package org.eclipse.slm.resource_management.service.rest.resources;

import org.eclipse.slm.common.messaging.resources.ResourceCreatedMessage;
import org.eclipse.slm.common.messaging.resources.ResourceInformationFoundMessage;
import org.eclipse.slm.common.messaging.resources.ResourceMessageListener;
import org.eclipse.slm.common.messaging.resources.ResourceMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourceManagementResourceMessageListener extends ResourceMessageListener {

    public final static Logger LOG = LoggerFactory.getLogger(ResourceManagementResourceMessageListener.class);

    private final ResourceMessageSender resourceMessageSender;

    public ResourceManagementResourceMessageListener(ResourceMessageSender resourceMessageSender) {
        this.resourceMessageSender = resourceMessageSender;
    }

    @Override
    public void onResourceCreated(ResourceCreatedMessage resource) {
        // Nothing to do
    }

    @Override
    public void onResourceInformationFound(ResourceInformationFoundMessage resourceInformationFoundMessage) {
        // Nothing to do
    }

}
