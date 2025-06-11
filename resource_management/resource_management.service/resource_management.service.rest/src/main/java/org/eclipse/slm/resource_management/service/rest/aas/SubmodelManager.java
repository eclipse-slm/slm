package org.eclipse.slm.resource_management.service.rest.aas;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.resource_management.model.resource.BasicResource;
import org.eclipse.slm.resource_management.model.resource.ResourceAas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SubmodelManager {

    private static final Logger LOG = LoggerFactory.getLogger(SubmodelManager.class);

    private final AasRegistryClient aasRegistryClient;

    private final AasRepositoryClient aasRepositoryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    private final SubmodelRepositoryClient submodelRepositoryClient;

    private final ConceptDescriptionRepositoryClient conceptDescriptionRepositoryClient;

    SubmodelManager(AasRegistryClient aasRegistryClient, AasRepositoryClient aasRepositoryClient, SubmodelRegistryClient submodelRegistryClient, SubmodelRepositoryClient submodelRepositoryClient, ConceptDescriptionRepositoryClient conceptDescriptionRepositoryClient) {
        this.aasRegistryClient = aasRegistryClient;
        this.aasRepositoryClient = aasRepositoryClient;
        this.submodelRegistryClient = submodelRegistryClient;
        this.submodelRepositoryClient = submodelRepositoryClient;
        this.conceptDescriptionRepositoryClient = conceptDescriptionRepositoryClient;
    }

    public List<SubmodelDescriptor> getSubmodels(BasicResource resource) {
        var submodelDescriptors = new ArrayList<SubmodelDescriptor>();

        var aasId = ResourceAas.createAasIdFromResourceId(resource.getId());
        var aas = this.aasRepositoryClient.getAas(aasId);
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
