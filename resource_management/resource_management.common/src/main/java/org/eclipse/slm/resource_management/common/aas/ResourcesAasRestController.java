package org.eclipse.slm.resource_management.common.aas;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
@Tag(name = "Resources AAS")
public class ResourcesAasRestController {

    private final ResourcesAasHandler resourcesAasHandler;

    @Autowired
    public ResourcesAasRestController(ResourcesAasHandler resourcesAasHandler) {
        this.resourcesAasHandler = resourcesAasHandler;
    }

    @RequestMapping(value = "/{resourceId}/aas-descriptor", method = RequestMethod.GET)
    @Operation(summary = "Get AAS descriptor of resource")
    public ResponseEntity<AssetAdministrationShellDescriptor> getResourceAasDescriptor(
            @PathVariable(name = "resourceId") UUID resourceId
    ) {
        var aasDescriptor = this.resourcesAasHandler.getResourceAasDescriptor(resourceId);

        return ResponseEntity.ok(aasDescriptor.get());
    }

    @RequestMapping(value = "/aas", method = RequestMethod.GET)
    @Operation(summary = "Get all AAS of resources")
    public List<AssetAdministrationShell> getResourceAASDescriptors() {
        // TODO: Fix together with Profiler update
//        var allAASDescriptors = this.aasRegistryClient.getAllShellDescriptors();
//        List<AssetAdministrationShellDescriptor> resourceAASDescriptors = allAASDescriptors.stream().filter(aasDescriptor ->
//                aasDescriptor
//                        .getSubmodelDescriptorFromIdShort(ID_SHORT_PLATFORM_RESOURCES) != null
//        ).collect(Collectors.toList());

        var aasList = new ArrayList<AssetAdministrationShell>();

//        for(var aasDescriptor : resourceAASDescriptors) {
//            Collection<Submodel> submodels = new ArrayList<>();
//
//            aasManager.retrieveSubmodels(aasDescriptor.getIdentifier())
//                    .values()
//                    .stream()
//                    .forEach(e -> {
//                        try {
//                            submodels.add( ((ConnectedSubmodel) e).getLocalCopy() );
//                        } catch(ResourceNotFoundException exception) {
//                            LOG.error("Unable to lookup Submodel.");
//                            LOG.error(exception.getMessage());
//                        }
//                    });
//
//            aasList.add(new ResourceAASInclSubmodels(
//                    aasManager.retrieveAAS(aasDescriptor.getIdentifier()).getLocalCopy(),
//                    submodels
//            ));
//        }

        return aasList;
    }
}
