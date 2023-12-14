package org.eclipse.slm.resource_management.service.rest.aas;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.slm.resource_management.service.rest.aas.ResourceAASController.ID_SHORT_PLATFORM_RESOURCES;

@RestController
@RequestMapping("/aas")
public class SubmodelTemplatesRestController {

    public final static Logger LOG = LoggerFactory.getLogger(SubmodelTemplatesRestController.class);

    private final IAASRegistry aasRegistry;
    private ConnectedAssetAdministrationShellManager aasManager;

    public SubmodelTemplatesRestController(@Value("${basyx.aas-registry.url}") String aasRegistryUrl) {
        this.aasRegistry = new AASRegistryProxy(aasRegistryUrl);
        this.aasManager = new ConnectedAssetAdministrationShellManager(aasRegistry);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @Operation(summary = "Get all AAS containing PlatformResources Submodel")
    public List<AssetAdministrationShell> getResourceAASDescriptors() {
        var allAASDescriptors = aasRegistry.lookupAll();
        List<AASDescriptor> resourceAASDescriptors = allAASDescriptors.stream().filter(aasDescriptor ->
                aasDescriptor
                        .getSubmodelDescriptorFromIdShort(ID_SHORT_PLATFORM_RESOURCES) != null
        ).collect(Collectors.toList());

        List<AssetAdministrationShell> aasList = new ArrayList<>();

        for(AASDescriptor aasd : resourceAASDescriptors) {
            Collection<Submodel> submodels = new ArrayList<>();

            aasManager.retrieveSubmodels(aasd.getIdentifier())
                    .values()
                    .stream()
                    .forEach(e -> {
                        try {
                            submodels.add( ((ConnectedSubmodel) e).getLocalCopy() );
                        } catch(ResourceNotFoundException exception) {
                            LOG.error("Unable to lookup Submodel.");
                            LOG.error(exception.getMessage());
                        }
                    });

            aasList.add(new ResourceAASInclSubmodels(
                    aasManager.retrieveAAS(aasd.getIdentifier()).getLocalCopy(),
                    submodels
            ));
        }
        return aasList;
    }

    @RequestMapping(value = "/submodels/templates/{smTemplateSemanticId}/instances", method = RequestMethod.GET)
    @Operation(summary = "Get instances of submodel templates using semantic id")
    public ResponseEntity getSubmodelTemplateInstancesBySemanticId(
            @PathVariable(name = "smTemplateSemanticId") String smTemplateSemanticId,
            @RequestParam(required = false) String filterByAasId
    ) {
        List<Map<String, String>> submodelTemplateInstances = new ArrayList<>();

        var allAASDescriptors = aasRegistry.lookupAll();

        try {
            for (var aasDescriptor : allAASDescriptors) {
                for (var submodelDescriptor : aasDescriptor.getSubmodelDescriptors()) {
                    if (submodelDescriptor != null) {
                        if (submodelDescriptor.getSemanticId() != null) {
                            for (var semanticIdKey : submodelDescriptor.getSemanticId().getKeys()) {
                                if (semanticIdKey.getValue().equals(smTemplateSemanticId)) {
                                    var submodelTemplateInstance = new HashMap<String, String>();
                                    submodelTemplateInstance.put("id", aasDescriptor.getIdentifier().getId());
                                    submodelTemplateInstance.put("name", aasDescriptor.getAsset().getIdShort());
                                    submodelTemplateInstance.put("aasEndpoint", aasDescriptor.getFirstEndpoint());
                                    submodelTemplateInstance.put("smEndpoint", submodelDescriptor.getFirstEndpoint());
                                    submodelTemplateInstance.put("smPath", submodelDescriptor.getFirstEndpoint().replace(aasDescriptor.getFirstEndpoint(), ""));
                                    submodelTemplateInstances.add(submodelTemplateInstance);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (NullPointerException e) {
            LOG.error(e.getMessage());
        }

        if (filterByAasId != null) {
            submodelTemplateInstances = submodelTemplateInstances.stream()
                    .filter(smti -> smti.get("id").equals(filterByAasId))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(submodelTemplateInstances);
    }
}
