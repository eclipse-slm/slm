package org.eclipse.slm.resource_management.service.rest.aas;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
public class AasRestController {

    private final AasHandler aasHandler;

    @Autowired
    public AasRestController(AasHandler aasHandler) {
        this.aasHandler = aasHandler;
    }

    @RequestMapping(value = "/{resourceId}/aas-descriptor", method = RequestMethod.GET)
    @Operation(summary = "Get AAS descriptor of resource")
    public ResponseEntity<AssetAdministrationShellDescriptor> getResourceAasDescriptor(
            @PathVariable(name = "resourceId") UUID resourceId
    ) {
        var aasDescriptor = this.aasHandler.getResourceAasDescriptor(resourceId);

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
