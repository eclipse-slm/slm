package org.eclipse.slm.information_service.service.app;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.slm.aas.clients.*;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClient;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClientFactory;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClient;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClientFactory;
import org.eclipse.slm.aas.clients.submodelrepository.SubmodelRepositoryClient;
import org.eclipse.slm.aas.clients.submodelrepository.SubmodelRepositoryClientFactory;
import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.common.messaging.GenericMessageListener;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3Submodel;
import org.eclipse.slm.resource_management.common.resources.ResourceEventMessage;
import org.eclipse.slm.resource_management.common.resources.ResourceEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;

@Component
public class ResourceEventMessageListener extends GenericMessageListener<ResourceEventMessage> {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceEventMessageListener.class);

    private final AasRepositoryClient aasRepositoryClient;
    private final SubmodelRepositoryClient submodelRepositoryClient;
    private final SubmodelRegistryClient submodelRegistryClient;

    private final AasRepositoryClient irsAasRepositoryClient;
    private final SubmodelRegistryClient irsSubmodelRegistryClient;
    private final String irsUrlInternal;
    private final String irsUrlExternal;

    public ResourceEventMessageListener(@Value("${irs.url.internal}") String irsUrlInternal,
                                        @Value("${irs.url.external}") String irsUrlExternal,
                                        AasRepositoryClientFactory aasRepositoryClientFactory,
                                        SubmodelRepositoryClientFactory submodelRepositoryClientFactory,
                                        SubmodelRegistryClientFactory submodelRegistryClientFactory,
                                        ConnectionFactory connectionFactory,
                                        RabbitTemplate rabbitTemplate) {
        super(ResourceEventMessage.EXCHANGE_NAME, AbstractEventMessage.getRoutingKeyAllEvents(ResourceEventMessage.ROUTING_KEY_PREFIX),
                connectionFactory, rabbitTemplate);
        this.irsUrlInternal = irsUrlInternal;
        this.irsUrlExternal = irsUrlExternal;
        this.aasRepositoryClient = aasRepositoryClientFactory.getClient();
        this.submodelRepositoryClient = submodelRepositoryClientFactory.getClient();
        this.submodelRegistryClient = submodelRegistryClientFactory.getClient();

        this.irsAasRepositoryClient = new AasRepositoryClient(this.irsUrlInternal);
        this.irsSubmodelRegistryClient = new SubmodelRegistryClient(this.irsUrlInternal, null);
    }

    @Override
    public void onMessageReceived(ResourceEventMessage resourceEventMessage) {
        // Only handle resource created events
        if (!resourceEventMessage.getEventType().equals(ResourceEventType.CREATED)) {
            return;
        }

        LOG.info("Received resource created message: {}", resourceEventMessage);
        try {
            var resourceAasId = "Resource_" + resourceEventMessage.getResource().getId();
            var resourceAasOptional = aasRepositoryClient.getAas(resourceAasId);
            if (resourceAasOptional.isEmpty()) {
                LOG.info("No AAS found for resource '{}', skipping information retrieval", resourceEventMessage.getResource().getId());
                return;
            }
            var resourceAas = resourceAasOptional.get();

            // Collect the ids of all submodels currently referenced by the AAS. This is used further down to
            // keep processing idempotent (skip already imported submodels) and to check whether the Resource
            // Management Nameplate still needs to be replaced.
            var existingSubmodelIds = new HashSet<String>();
            for (var submodelRef : resourceAas.getSubmodels()) {
                var submodelRefKey = submodelRef.getKeys().get(0);
                if (submodelRefKey.getType().equals(KeyTypes.SUBMODEL)) {
                    existingSubmodelIds.add(submodelRefKey.getValue());
                }
            }

            var assetId = resourceEventMessage.getResource().getAssetId();
            if (assetId == null) {
                LOG.info("Asset id for created resource '{}' not available, skipping information retrieval", resourceEventMessage.getResource().getId());
                return;
            }

            // Get submodels of device via Information Receiving Service using ID Link
            var webClientBuilder = WebClient.builder();
            var webClient = webClientBuilder.baseUrl(irsUrlInternal)
                    .codecs(codecs -> codecs
                            .defaultCodecs()
                            .maxInMemorySize(10000 * 1024))
                    .build();

            var uriOfTheProductBase64Encoded = Base64.getEncoder().encodeToString(assetId.getBytes());
            String[] shellIds = new String[0];
            try {
                var lookupResponse = webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/lookup/shells")
                                .queryParam("assetIds", uriOfTheProductBase64Encoded)
                                .build())
                        .retrieve()
                        .bodyToMono(ShellLookupResponse.class)
                        .block();

                if (lookupResponse == null || lookupResponse.getResult() == null) {
                    LOG.info("No shells found for asset id '{}', skipping information retrieval", assetId);
                    return;
                }

                shellIds = lookupResponse.getResult().toArray(String[]::new);
            } catch (WebClientResponseException.NotFound e) {
                LOG.info("No shells found for asset id '{}', skipping information retrieval", assetId);
                return;
            }

            var receivedSubmodelDescriptors = new ArrayList<SubmodelDescriptor>();
            for (var shellId : shellIds) {
                var shellOptional = irsAasRepositoryClient.getAas(shellId);
                var shell = shellOptional.get();
                shell.getSubmodels().forEach(submodelRef -> {
                    var submodelRefKey = submodelRef.getKeys().get(0);
                    if (submodelRefKey.getType().equals(KeyTypes.SUBMODEL)) {
                        var submodelDescriptorOptional = irsSubmodelRegistryClient.getSubmodelDescriptor(submodelRefKey.getValue());
                        if (submodelDescriptorOptional.isPresent()) {
                            var submodelDescriptor = submodelDescriptorOptional.get();
                            receivedSubmodelDescriptors.add(submodelDescriptor);
                        } else {
                            LOG.info("Submodel descriptor not found for id '{}', skipping", submodelRefKey.getValue());
                        }
                    }
                });
            }

            // The Digital Nameplate that Resource Management created during onboarding. It is meant to be
            // replaced by the (richer) Nameplate imported from the IRS - but ONLY this specific submodel may
            // ever be deleted, never an already imported one.
            var rmNameplateSubmodelId = DigitalNameplateV3Submodel.SUBMODEL_IDSHORT + "-" + resourceEventMessage.getResource().getId();

            // Collect the ids of the received submodels and detect whether a Nameplate is among them. The id
            // set is used as a guard so we never delete a submodel that we are importing/keeping.
            var receivedSubmodelIds = new HashSet<String>();
            var receivedNameplatePresent = false;
            for (var submodelDescriptor : receivedSubmodelDescriptors) {
                receivedSubmodelIds.add(submodelDescriptor.getId());
                if (submodelDescriptor.getSemanticId() != null && !submodelDescriptor.getSemanticId().getKeys().isEmpty()) {
                    var semanticId = submodelDescriptor.getSemanticId().getKeys().get(0).getValue();
                    if (semanticId.equals(IDTASubmodelTemplates.NAMEPLATE_V2_SUBMODEL_SEMANTIC_ID)
                            || semanticId.equals(IDTASubmodelTemplates.NAMEPLATE_V3_SUBMODEL_SEMANTIC_ID)) {
                        receivedNameplatePresent = true;
                    }
                }
            }

            // Register the received submodels at the SLM registry and add references to the resource AAS.
            // Submodels that are already referenced are skipped so that a re-delivery / re-processing of the
            // same event does not create duplicate references (idempotency).
            var addedSubmodelCount = 0;
            for (var submodelDescriptor : receivedSubmodelDescriptors) {
                if (existingSubmodelIds.contains(submodelDescriptor.getId())) {
                    LOG.info("Submodel '{}' already referenced by AAS '{}', skipping", submodelDescriptor.getId(), resourceAasId);
                    continue;
                }

                // Register submodel of IRS at submodel registry of SLM
                var submodelEndpoint = irsUrlExternal + "/submodels/" + Base64.getEncoder().encodeToString(submodelDescriptor.getId().getBytes());
                String semanticId = null;
                if (submodelDescriptor.getSemanticId() != null && !submodelDescriptor.getSemanticId().getKeys().isEmpty()) {
                    semanticId = submodelDescriptor.getSemanticId().getKeys().get(0).getValue();
                }
                submodelRegistryClient.registerSubmodel(
                        submodelEndpoint,
                        submodelDescriptor.getId(),
                        submodelDescriptor.getIdShort(),
                        semanticId);

                aasRepositoryClient.addSubmodelReferenceToAas(resourceAas.getId(), submodelDescriptor.getId());
                existingSubmodelIds.add(submodelDescriptor.getId());
                addedSubmodelCount++;
            }

            // Replace the Resource Management Nameplate with the one imported from the IRS. Only the RM-created
            // Nameplate is deleted, and only if it still exists and is not itself one of the imported submodels.
            // This keeps the step idempotent: on re-processing the RM Nameplate is already gone, so nothing happens.
            if (receivedNameplatePresent
                    && existingSubmodelIds.contains(rmNameplateSubmodelId)
                    && !receivedSubmodelIds.contains(rmNameplateSubmodelId)) {
                aasRepositoryClient.removeSubmodelReferenceFromAas(resourceAasId, rmNameplateSubmodelId);
                submodelRepositoryClient.deleteSubmodel(rmNameplateSubmodelId);
                LOG.info("Replaced Resource Management Nameplate '{}' on AAS '{}' with Nameplate imported from IRS", rmNameplateSubmodelId, resourceAasId);
            }

            LOG.info("Successfully added {} submodels to AAS '{}'", addedSubmodelCount, resourceAasId);
        }
        catch (Exception e) {
            Writer buffer = new StringWriter();
            PrintWriter pw = new PrintWriter(buffer);
            e.printStackTrace(pw);
            LOG.error("Error while processing resourceCreatedMessage created message: {} | Stack Trace: {}", e.getMessage(),  buffer);
        }
    }
    private static class ShellLookupResponse {
        private List<String> result;

        public List<String> getResult() {
            return result;
        }

        public void setResult(List<String> result) {
            this.result = result;
        }
    }
}
