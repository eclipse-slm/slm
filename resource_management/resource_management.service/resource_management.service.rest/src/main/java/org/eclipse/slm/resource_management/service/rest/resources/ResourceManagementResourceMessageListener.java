package org.eclipse.slm.resource_management.service.rest.resources;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.slm.common.aas.clients.AasRepositoryClient;
import org.eclipse.slm.common.aas.clients.IDTASubmodelTemplates;
import org.eclipse.slm.common.aas.clients.SubmodelRegistryClient;
import org.eclipse.slm.common.aas.clients.SubmodelRepositoryClient;
import org.eclipse.slm.common.messaging.resources.ResourceCreatedMessage;
import org.eclipse.slm.common.messaging.resources.ResourceInformationFoundMessage;
import org.eclipse.slm.common.messaging.resources.ResourceMessageListener;
import org.eclipse.slm.common.messaging.resources.ResourceMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

@Component
public class ResourceManagementResourceMessageListener extends ResourceMessageListener {

    public final static Logger LOG = LoggerFactory.getLogger(ResourceManagementResourceMessageListener.class);

    private final AasRepositoryClient aasRepositoryClient;
    private final SubmodelRepositoryClient submodelRepositoryClient;
    private final SubmodelRegistryClient submodelRegistryClient;
    private final ResourceMessageSender resourceMessageSender;

    public ResourceManagementResourceMessageListener(AasRepositoryClient aasRepositoryClient,
                                                     SubmodelRepositoryClient submodelRepositoryClient,
                                                     SubmodelRegistryClient submodelRegistryClient, ResourceMessageSender resourceMessageSender) {
        this.aasRepositoryClient = aasRepositoryClient;
        this.submodelRepositoryClient = submodelRepositoryClient;
        this.submodelRegistryClient = submodelRegistryClient;
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
