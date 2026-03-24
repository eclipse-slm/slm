package org.eclipse.slm.resource_management.common.aas;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ResourcesAasRestApiConfig.BASE_PATH)
@Tag(name = ResourcesAasRestApiConfig.TAG)
public class ResourcesAasRestController implements ResourcesAasRestApi {

    private final ResourcesAasHandler resourcesAasHandler;

    @Autowired
    public ResourcesAasRestController(ResourcesAasHandler resourcesAasHandler) {
        this.resourcesAasHandler = resourcesAasHandler;
    }

    @Override
    public ResponseEntity<AssetAdministrationShellDescriptor> getResourceAasDescriptor(
            UUID resourceId
    ) {
        var aasDescriptor = this.resourcesAasHandler.getResourceAasDescriptor(resourceId);

        return ResponseEntity.ok(aasDescriptor.get());
    }

    @Override
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
