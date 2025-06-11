package org.eclipse.slm.information_service.service.impl;

import jakarta.annotation.PostConstruct;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException;
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
public class InformationServiceResourceMessageListener extends ResourceMessageListener {

    public final static Logger LOG = LoggerFactory.getLogger(InformationServiceResourceMessageListener.class);

    private final AasRepositoryClient aasRepositoryClient;
    private final SubmodelRepositoryClient submodelRepositoryClient;
    private final SubmodelRegistryClient submodelRegistryClient;
    private final ResourceMessageSender resourceMessageSender;

    private final AasRepositoryClient irsAasRepositoryClient;
    private final SubmodelRegistryClient irsSubmodelRegistryClient;
    private final String irsUrl;

    public InformationServiceResourceMessageListener(@Value("${irs.url}") String irsUrl,
                                                     AasRepositoryClient aasRepositoryClient,
                                                     SubmodelRepositoryClient submodelRepositoryClient,
                                                     SubmodelRegistryClient submodelRegistryClient,
                                                     ResourceMessageSender resourceMessageSender) {
        this.irsUrl = irsUrl;
        this.aasRepositoryClient = aasRepositoryClient;
        this.submodelRepositoryClient = submodelRepositoryClient;
        this.submodelRegistryClient = submodelRegistryClient;
        this.resourceMessageSender = resourceMessageSender;

        this.irsAasRepositoryClient = new AasRepositoryClient(this.irsUrl + "/api/shell_repo");
        this.irsSubmodelRegistryClient = new SubmodelRegistryClient(
                this.irsUrl + "/api/submodel_registry",
                this.irsUrl + "/api/submodel_repo");
    }

    @Override
    public void onResourceCreated(ResourceCreatedMessage resourceCreatedMessage) {
        LOG.info("Received resource created message: {}", resourceCreatedMessage);

        try {
            var resourceAasId = "Resource_" + resourceCreatedMessage.resourceId();
            var resourceAas = aasRepositoryClient.getAas(resourceAasId);
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

            var assetId = resourceCreatedMessage.assetId();
            if (assetId == null) {
                LOG.info("Asset id for created resource '{}' not available, skipping information retrieval", resourceCreatedMessage.resourceId());
                return;
            }

            // Get submodels of device via Information Receiving Service using ID Link
            var webClientBuilder = WebClient.builder();
            var webClient = webClientBuilder.baseUrl(irsUrl)
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
                var shell = irsAasRepositoryClient.getAas(shellId);
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
                String submodelEndpoint;
                if (!submodelDescriptor.getEndpoints().isEmpty()) {
                    submodelEndpoint = submodelDescriptor.getEndpoints().get(0).getProtocolInformation().getHref();
                }
                else {
                    LOG.info("No endpoint found for submodel descriptor '{}', skipping", submodelDescriptor.getId());
                    return;
                }
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

            resourceMessageSender.sendResourceInformationMessage(resourceCreatedMessage.resourceId());
        }
        catch (Exception e) {
            Writer buffer = new StringWriter();
            PrintWriter pw = new PrintWriter(buffer);
            e.printStackTrace(pw);
            LOG.error("Error while processing resourceCreatedMessage created message: {} | Stack Trace: {}", e.getMessage(),  buffer);
        }
    }

    @Override
    public void onResourceInformationFound(ResourceInformationFoundMessage resourceInformationFoundMessage) {
        // Nothing to do
    }

}
