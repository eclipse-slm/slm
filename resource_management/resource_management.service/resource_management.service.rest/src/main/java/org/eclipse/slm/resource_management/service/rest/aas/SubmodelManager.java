package org.eclipse.slm.resource_management.service.rest.aas;

import org.eclipse.slm.resource_management.model.resource.BasicResource;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.basyx.aas.bundle.AASBundle;
import org.eclipse.basyx.aas.factory.aasx.AASXToMetamodelConverter;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.api.parts.IConceptDictionary;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.metamodel.map.parts.ConceptDictionary;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.parts.IConceptDescription;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.identifier.Identifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
public class SubmodelManager {
    private final String aasServerUrl;
    private final IAASRegistry registry;
    private final ConnectedAssetAdministrationShellManager aasManager;

    SubmodelManager(
            @Value("${basyx.aas-registry.url}") String aasRegistryUrl,
            @Value("${basyx.aas-server.url}") String aasServerUrl) {
        this.aasServerUrl = aasServerUrl;
        this.registry = new AASRegistryProxy(aasRegistryUrl);
        this.aasManager = new ConnectedAssetAdministrationShellManager(registry);
    }

    public List<SubmodelDescriptor> getSubmodels(BasicResource resource) {
        IIdentifier aasIdentifier = ResourceAAS.createIdentification(resource.getId());
        return registry.lookupSubmodels(aasIdentifier);
    }

    public List<Object> addSubmodels(BasicResource resource, InputStream inputStream) throws IOException, ParserConfigurationException, InvalidFormatException, SAXException {
        IIdentifier aasIdentifier = ResourceAAS.createIdentification(resource.getId());
        // AssetAdministrationShell aas = aasManager.retrieveAAS(aasIdentifier).getLocalCopy();
        AASXToMetamodelConverter packageManager = new AASXToMetamodelConverter(inputStream);
        // updateAASConceptDescriptions(aas, packageManager.retrieveAasEnv());
        Set<AASBundle> bundles = packageManager.retrieveAASBundles();
        List<Object> submodels = new ArrayList<>();
        for (AASBundle bundle : bundles) {
            for (ISubmodel iSubmodel : bundle.getSubmodels()) {
                Submodel submodel = (Submodel) iSubmodel;
                aasManager.createSubmodel(aasIdentifier, submodel);
                submodels.add(submodel);
            }
        }
        return submodels;
    }

    // TODO: find a way to update concept descriptions without overwriting the whole aas
    private void updateAASConceptDescriptions(AssetAdministrationShell aas, AasEnv aasEnv) {
        Collection<IConceptDictionary> dictionaries = aas.getConceptDictionary();
        if (dictionaries.isEmpty()) {
            for (IConceptDescription conceptDescription: aasEnv.getConceptDescriptions()) {
                aas.addConceptDescription(conceptDescription);
            }
        } else {
            ConceptDictionary dictionary = (ConceptDictionary)dictionaries.iterator().next();
            Collection<IConceptDescription> conceptDescriptions = dictionary.getConceptDescriptions();
            for (IConceptDescription conceptDescription: aasEnv.getConceptDescriptions()) {
                if (!conceptDescriptions.contains(conceptDescription)) {
                    aas.addConceptDescription(conceptDescription);
                }
            }
        }
        aasManager.createAAS(aas, aasServerUrl);
    }

    public void deleteSubmodel(BasicResource resource, String submodelIdShort) {
        IIdentifier aasIdentifier = ResourceAAS.createIdentification(resource.getId());
        AASDescriptor aasDescriptor = registry.lookupAAS(aasIdentifier);
        SubmodelDescriptor smDescriptor = aasDescriptor.getSubmodelDescriptorFromIdShort(submodelIdShort);
        if (smDescriptor == null) {
            throw new NullPointerException("Submodel for aas '" + aasIdentifier + "' with idShort '" + submodelIdShort + "' not found.");
        }
        var smIdentifier = smDescriptor.getIdentifier();
        aasManager.deleteSubmodel(aasIdentifier, smIdentifier);
    }
}
