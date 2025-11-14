package org.eclipse.slm.resource_management.common.resource_types;

import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.common.aas.clients.shellregistry.AasRegistryClient;
import org.eclipse.slm.common.aas.clients.shellregistry.AasRegistryClientFactory;
import org.eclipse.slm.common.aas.clients.shellrepository.AasRepositoryClientFactory;
import org.eclipse.slm.common.aas.clients.submodelregistry.SubmodelRegistryClient;
import org.eclipse.slm.common.aas.clients.submodelregistry.SubmodelRegistryClientFactory;
import org.eclipse.slm.common.aas.clients.submodelrepository.SubmodelRepositoryClientFactory;
import org.eclipse.slm.common.aas.clients.utils.SubmodelUtils;
import org.eclipse.slm.common.aas.model.shellregistry.requests.GetAllShellDescriptorsFilter;
import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.eclipse.slm.resource_management.common.resources.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ResourceTypesManager {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceTypesManager.class);

    private final AasRegistryClient aasRegistryClient;

    private final SubmodelRegistryClient submodelRegistryClient;


    public ResourceTypesManager(AasRegistryClientFactory aasRegistryClientFactory,
                                SubmodelRegistryClientFactory submodelRegistryClientFactory
    ) {
        this.aasRegistryClient = aasRegistryClientFactory.getClient();
        this.submodelRegistryClient = submodelRegistryClientFactory.getClient();
    }

    public Collection<ResourceType> getResourceTypes() {
        var nameplateSemanticIds = List.of(
                IDTASubmodelTemplates.NAMEPLATE_V2_SUBMODEL_SEMANTIC_ID,
                IDTASubmodelTemplates.NAMEPLATE_V3_SUBMODEL_SEMANTIC_ID
        );
        //TODO: Implement paging when large number of shells exist
        var getShellDescriptorsResult = aasRegistryClient.getAllShellDescriptors(GetAllShellDescriptorsFilter.builder().build());
        var submodelIdToShellId = new HashMap<String, String>();
        var shellIdToSubmodelIds = new HashMap<String, List<String>>();
        for (var shellDescriptor : getShellDescriptorsResult.getResult()) {
            if (!shellDescriptor.getId().startsWith("Resource_")) {
                continue;
            }

            var aasRepositoryClient = AasRepositoryClientFactory.FromShellDescriptor(shellDescriptor);
            var shell = aasRepositoryClient.getAas(shellDescriptor.getId());
            if (shell.isPresent()) {
                for (var submodelRef : shell.get().getSubmodels()) {
                    submodelIdToShellId.put(submodelRef.getKeys().get(0).getValue(), shell.get().getId());
                }

                var submodelIds = shell.get().getSubmodels().stream()
                        .map(submodelRef -> submodelRef.getKeys().get(0).getValue())
                        .toList();
                shellIdToSubmodelIds.put(shell.get().getId(), submodelIds);
            }
        }

        var nameplateSubmodelDescriptors = submodelRegistryClient.findSubmodelDescriptorsWithSemanticIds(nameplateSemanticIds);

        var nameplateSubmodels = new ArrayList<Submodel>();
        for (var nameplateSubmodelDescriptor : nameplateSubmodelDescriptors) {
            var submodelRepositoryClient = SubmodelRepositoryClientFactory.FromSubmodelDescriptor(nameplateSubmodelDescriptor);
            try {
                var nameplateSubmodel = submodelRepositoryClient.getSubmodelOrThrow(nameplateSubmodelDescriptor.getId());
                if (nameplateSubmodel != null) {
                    nameplateSubmodels.add(nameplateSubmodel);
                }
            }
            catch (Exception e) {
                LOG.error("Error fetching nameplate submodel with id '" + nameplateSubmodelDescriptor.getId() + "': " + e.getMessage());
                LOG.debug("Stacktrace: ", e);
            }
        }

        var resourceTypes = new HashMap<String, ResourceType>();
        for (var nameplateSubmodel : nameplateSubmodels) {
            try {
                var manufacturerName = SubmodelUtils.findSubmodelElement(nameplateSubmodel.getSubmodelElements(), "ManufacturerName");
                var manufacturerProductType = SubmodelUtils.findSubmodelElement(nameplateSubmodel.getSubmodelElements(), "ManufacturerProductType");

                if (manufacturerName.isPresent() && manufacturerProductType.isPresent()) {
                    var manufacturerNameProp = (MultiLanguageProperty) manufacturerName.get();
                    var manufacturerNameValue = manufacturerNameProp.getValue().get(0).getText();

                    var manufacturerProductTypeValue = "";
                    if (nameplateSubmodel.getSemanticId().getKeys().get(0).getValue().equals(IDTASubmodelTemplates.NAMEPLATE_V2_SUBMODEL_SEMANTIC_ID)) {
                        var manufacturerProductTypeProp = (MultiLanguageProperty) manufacturerProductType.get();
                        manufacturerProductTypeValue = manufacturerProductTypeProp.getValue().get(0).getText();
                    } else {
                        var manufacturerProductTypeProp = (Property) manufacturerProductType.get();
                        manufacturerProductTypeValue = manufacturerProductTypeProp.getValue();
                    }

                    ResourceType resourceType;
                    if (resourceTypes.containsKey(manufacturerProductTypeValue)) {
                        resourceType = resourceTypes.get(manufacturerProductTypeValue);
                    } else {
                        resourceType = new ResourceType(manufacturerProductTypeValue, manufacturerNameValue);
                        resourceTypes.put(resourceType.getTypeName(), resourceType);
                    }

                    if (submodelIdToShellId.containsKey(nameplateSubmodel.getId())) {
                        var shellId = submodelIdToShellId.get(nameplateSubmodel.getId());
                        if (shellId.contains(ResourceAas.AAS_ID_PREFIX)) {
                            var resourceId = UUID.fromString(shellId.replace(ResourceAas.AAS_ID_PREFIX, ""));
                            resourceType.addResourceInstanceId(resourceId);

                            var submodelIds = shellIdToSubmodelIds.get(shellId);
                            var softwareNameplateIds = new ArrayList<String>();
                            for (var submodelId : submodelIds) {
                                var optionalSubmodelDescriptor = this.submodelRegistryClient.findSubmodelDescriptor(submodelId);
                                if (optionalSubmodelDescriptor.isPresent()) {
                                    var semanticId = optionalSubmodelDescriptor.get().getSemanticId();
                                    if (semanticId != null) {
                                        if (semanticId.getKeys() != null && !semanticId.getKeys().isEmpty()) {
                                            var semanticIdValue = semanticId.getKeys().get(0).getValue();
                                            if (semanticIdValue.equals(IDTASubmodelTemplates.SOFTWARE_NAMEPLATE_SUBMODEL_SEMANTIC_ID)) {
                                                softwareNameplateIds.add(submodelId);
                                            }
                                        }
                                    }
                                }
                            }
                            resourceType.setSoftwareNameplateIds(softwareNameplateIds);
                        }
                    }

                } else {
                    LOG.debug("Skipping submodel with id '" + nameplateSubmodel.getId() + "', because of missing ManufacturerName or ManufacturerProductType");
                }
            } catch (Exception e) {
                LOG.debug("Error processing nameplate submodel: " + e.getMessage(), e);
            }
        }

        return resourceTypes.values();
    }
}
