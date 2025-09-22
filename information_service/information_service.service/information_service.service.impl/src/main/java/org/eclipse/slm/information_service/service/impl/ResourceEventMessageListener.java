package org.eclipse.slm.information_service.service.impl;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.common.messaging.GenericMessageListener;
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
import java.util.HashMap;
import java.util.List;

@Component
public class ResourceEventMessageListener extends GenericMessageListener<ResourceEventMessage> {

    public final static Logger LOG = LoggerFactory.getLogger(ResourceEventMessageListener.class);

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

        this.irsAasRepositoryClient = new AasRepositoryClient(this.irsUrlInternal + "/api/shell_repo");
        this.irsSubmodelRegistryClient = new SubmodelRegistryClient(this.irsUrlInternal + "/api/submodel_registry");
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

            var semanticIdToSubmodelDescriptors = new HashMap<String, List<SubmodelDescriptor>>();

            // Get all submodel descriptors of submodels contained in the AAS
            for (var submodelRef : resourceAas.getSubmodels()) {
                var submodelRefKey = submodelRef.getKeys().get(0);
                if (submodelRefKey.getType().equals(KeyTypes.SUBMODEL)) {
                    var submodelDescriptor = submodelRegistryClient.findSubmodelDescriptor(submodelRefKey.getValue());

                    if (submodelDescriptor.isPresent()) {
                        if (submodelDescriptor.get().getSemanticId() == null) {
                            LOG.info("No semantic ID found for existing submodel '{}', skipping", submodelDescriptor.get().getId());
                            continue;
                        }

                        var semanticIdKey = submodelDescriptor.get().getSemanticId().getKeys().get(0);

                        if (semanticIdToSubmodelDescriptors.containsKey(semanticIdKey.getValue())) {
                            semanticIdToSubmodelDescriptors.get(semanticIdKey.getValue())
                                    .add(submodelDescriptor.get());
                        } else {
                            semanticIdToSubmodelDescriptors.computeIfAbsent(semanticIdKey.getValue(), k -> new ArrayList<>())
                                    .add(submodelDescriptor.get());
                        }
                    }
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
                shellIds = webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/api/shell_discovery/lookup/shells")
                                .queryParam("assetId", uriOfTheProductBase64Encoded)
                                .build())
                        .retrieve()
                        .bodyToMono(String[].class)
                        .block();
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
                        var submodelDescriptorOptional = irsSubmodelRegistryClient.findSubmodelDescriptor(submodelRefKey.getValue());
                        if (submodelDescriptorOptional.isPresent()) {
                            var submodelDescriptor = submodelDescriptorOptional.get();
                            receivedSubmodelDescriptors.add(submodelDescriptor);
                        } else {
                            LOG.info("Submodel descriptor not found for id '{}', skipping", submodelRefKey.getValue());
                        }
                    }
                });
            }

            var duplicateSubmodelIdsToDelete = new ArrayList<String>();
            for (var submodelDescriptor : receivedSubmodelDescriptors) {
                if (submodelDescriptor.getSemanticId() == null) {
                    LOG.info("No semantic ID found for received submodelDescriptor '{}', skipping duplicate check", submodelDescriptor.getId());
                }
                else {
                    var semanticId = submodelDescriptor.getSemanticId().getKeys().get(0).getValue();
                    if ((semanticId.equals(IDTASubmodelTemplates.NAMEPLATE_V2_SUBMODEL_SEMANTIC_ID)
                            || semanticId.equals(IDTASubmodelTemplates.NAMEPLATE_V3_SUBMODEL_SEMANTIC_ID))
                            && (semanticIdToSubmodelDescriptors.containsKey(IDTASubmodelTemplates.NAMEPLATE_V2_SUBMODEL_SEMANTIC_ID)
                            || semanticIdToSubmodelDescriptors.containsKey(IDTASubmodelTemplates.NAMEPLATE_V3_SUBMODEL_SEMANTIC_ID))) {
                        if (semanticIdToSubmodelDescriptors.containsKey(IDTASubmodelTemplates.NAMEPLATE_V2_SUBMODEL_SEMANTIC_ID)) {
                            duplicateSubmodelIdsToDelete.add(semanticIdToSubmodelDescriptors.get(IDTASubmodelTemplates.NAMEPLATE_V2_SUBMODEL_SEMANTIC_ID).get(0).getId());
                        }
                        if (semanticIdToSubmodelDescriptors.containsKey(IDTASubmodelTemplates.NAMEPLATE_V3_SUBMODEL_SEMANTIC_ID)) {
                            duplicateSubmodelIdsToDelete.add(semanticIdToSubmodelDescriptors.get(IDTASubmodelTemplates.NAMEPLATE_V3_SUBMODEL_SEMANTIC_ID).get(0).getId());
                        }
                    }
                }

                // Register submodel of IRS at submodel registry of SLM
                var submodelEndpoint = irsUrlExternal + "/api/submodel_repo/submodels/" + Base64.getEncoder().encodeToString(submodelDescriptor.getId().getBytes());
                String semanticId = null;
                if (submodelDescriptor.getSemanticId() != null) {
                    if (!submodelDescriptor.getSemanticId().getKeys().isEmpty()) {
                        semanticId = submodelDescriptor.getSemanticId().getKeys().get(0).getValue();
                    }
                }
                submodelRegistryClient.registerSubmodel(
                        submodelEndpoint,
                        submodelDescriptor.getId(),
                        submodelDescriptor.getIdShort(),
                        semanticId);

                aasRepositoryClient.addSubmodelReferenceToAas(resourceAas.getId(), submodelDescriptor.getId());
                // Add submodelDescriptor refs to existing resourceCreatedMessage AAS
                aasRepositoryClient.addSubmodelReferenceToAas(resourceAas.getId(), submodelDescriptor.getId());
            }

            for (var submodelIdToDelete : duplicateSubmodelIdsToDelete) {
                aasRepositoryClient.removeSubmodelReferenceFromAas(resourceAasId, submodelIdToDelete);
                submodelRepositoryClient.deleteSubmodel(submodelIdToDelete);
                LOG.info("Deleted duplicate submodel '{}' from AAS '{}'", submodelIdToDelete, resourceAasId);
            }

            LOG.info("Successfully added {} submodels to AAS '{}'", receivedSubmodelDescriptors.size(), resourceAasId);
        }
        catch (Exception e) {
            Writer buffer = new StringWriter();
            PrintWriter pw = new PrintWriter(buffer);
            e.printStackTrace(pw);
            LOG.error("Error while processing resourceCreatedMessage created message: {} | Stack Trace: {}", e.getMessage(),  buffer);
        }
    }


}
