package org.eclipse.slm.resource_management.common.aas;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.common.aas.clients.exceptions.ShellNotFoundException;
import org.eclipse.slm.resource_management.common.resources.BasicResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ResourcesSubmodelManager {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcesSubmodelManager.class);

    private final AasRegistryClient aasRegistryClient;

    private final AasRepositoryClient aasRepositoryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    private final SubmodelRepositoryClient submodelRepositoryClient;

    private final ConceptDescriptionRepositoryClient conceptDescriptionRepositoryClient;

    ResourcesSubmodelManager(AasRegistryClientFactory aasRegistryClientFactory,
                             AasRepositoryClientFactory aasRepositoryClientFactory,
                             SubmodelRegistryClientFactory submodelRegistryClientFactory,
                             SubmodelRepositoryClientFactory submodelRepositoryClientFactory,
                             ConceptDescriptionRepositoryClient conceptDescriptionRepositoryClient) {
        this.aasRegistryClient = aasRegistryClientFactory.getClient();
        this.aasRepositoryClient = aasRepositoryClientFactory.getClient();
        this.submodelRegistryClient = submodelRegistryClientFactory.getClient();
        this.submodelRepositoryClient = submodelRepositoryClientFactory.getClient();
        this.conceptDescriptionRepositoryClient = conceptDescriptionRepositoryClient;
    }

    public List<SubmodelDescriptor> getSubmodels(BasicResource resource) {
        var submodelDescriptors = new ArrayList<SubmodelDescriptor>();

        var aasId = ResourceAas.createAasIdFromResourceId(resource.getId());
        var aasOptional = aasRepositoryClient.getAas(aasId);
        if (aasOptional.isEmpty()) {
            LOG.error("AAS with ID {} not found", aasId);
            throw new ShellNotFoundException(aasId);
        }
        var aas = aasOptional.get();
        var submodelRefs = aas.getSubmodels();
        for (var submodelRef : submodelRefs) {
            var submodelId = submodelRef.getKeys().get(0).getValue();
            var submodelDescriptorOptional = this.submodelRegistryClient.findSubmodelDescriptor(submodelId);

            if (submodelDescriptorOptional.isPresent()) {
                submodelDescriptors.add(submodelDescriptorOptional.get());
            }
        }

        return submodelDescriptors;
    }

    public void addSubmodelsFromAASX(String aasId, InputStream aasxFileInputStream)
            throws IOException, InvalidFormatException, DeserializationException {
        var aasxDeserializer = new AASXDeserializer(aasxFileInputStream);
        var environment = aasxDeserializer.read();

        var submodels = environment.getSubmodels();
        for (var submodel : submodels) {
            var resourceId = ResourceAas.getResourceIdFromAasId(aasId);
            var submodelId = submodel.getId() + "-" + resourceId;
            submodel.setId(submodelId);
            try {
                if (this.submodelRepositoryClient.getSubmodel(submodelId) != null) {
                    try {
                        LOG.debug("Submodel with id '" + submodelId + "' already exists, deleting it");
                        this.submodelRepositoryClient.deleteSubmodel(submodelId);
                    } catch (Exception e) {
                        LOG.error("Failed to delete submodel with id '" + submodelId + "': {}", e.getMessage());
                    }
                }
                this.submodelRepositoryClient.createOrUpdateSubmodel(submodel);
                this.aasRepositoryClient.addSubmodelReferenceToAas(aasId, submodel);
            } catch (Exception e) {
                LOG.error("Failed to create submodel with id '" + submodelId + "': {}", e.getMessage());
            }
        }

        var conceptDescriptions = environment.getConceptDescriptions();
        for (var conceptDescription : conceptDescriptions) {
            this.conceptDescriptionRepositoryClient.createOrUpdateConceptDescription(conceptDescription);
        }
    }

    public void deleteSubmodel(UUID resourceId, String submodelId) {
        var aasId = ResourceAas.createAasIdFromResourceId(resourceId);

        this.submodelRepositoryClient.deleteSubmodel(submodelId);
        this.aasRepositoryClient.removeSubmodelReferenceFromAas(aasId, submodelId);
    }
}
