package org.eclipse.slm.resource_management.service.rest.aas;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.resource_management.model.resource.BasicResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SubmodelManager {

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

        var aasId = ResourceAAS.createAasIdFromResourceId(resource.getId());
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
            submodel.setId(submodel.getId() + "-" + aasId);
            this.submodelRepositoryClient.createOrUpdateSubmodel(submodel);
            this.aasRepositoryClient.addSubmodelReferenceToAas(aasId, submodel);
        }

        var conceptDescriptions = environment.getConceptDescriptions();
        for (var conceptDescription : conceptDescriptions) {
            this.conceptDescriptionRepositoryClient.createOrUpdateConceptDescription(conceptDescription);
        }
    }

//
//    // TODO: find a way to update concept descriptions without overwriting the whole aas
//    private void updateAASConceptDescriptions(AssetAdministrationShell aas, AasEnv aasEnv) {
//        Collection<IConceptDictionary> dictionaries = aas.getConceptDictionary();
//        if (dictionaries.isEmpty()) {
//            for (IConceptDescription conceptDescription: aasEnv.getConceptDescriptions()) {
//                aas.addConceptDescription(conceptDescription);
//            }
//        } else {
//            ConceptDictionary dictionary = (ConceptDictionary)dictionaries.iterator().next();
//            Collection<IConceptDescription> conceptDescriptions = dictionary.getConceptDescriptions();
//            for (IConceptDescription conceptDescription: aasEnv.getConceptDescriptions()) {
//                if (!conceptDescriptions.contains(conceptDescription)) {
//                    aas.addConceptDescription(conceptDescription);
//                }
//            }
//        }
//        aasManager.createAAS(aas, aasServerUrl);
//    }
//
    public void deleteSubmodel(UUID resourceId, String submodelId) {
        var aasId = ResourceAAS.createAasIdFromResourceId(resourceId);

        this.submodelRepositoryClient.deleteSubmodel(submodelId);
        this.aasRepositoryClient.removeSubmodelReferenceFromAas(aasId, submodelId);
    }
}
