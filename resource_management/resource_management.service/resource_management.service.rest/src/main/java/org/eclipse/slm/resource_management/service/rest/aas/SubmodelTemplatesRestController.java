package org.eclipse.slm.resource_management.service.rest.aas;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/aas")
public class SubmodelTemplatesRestController {

    public final static Logger LOG = LoggerFactory.getLogger(SubmodelTemplatesRestController.class);

    private final IAASRegistry aasRegistry;

    public SubmodelTemplatesRestController(@Value("${basyx.aas-registry.url}") String aasRegistryUrl) {
        this.aasRegistry = new AASRegistryProxy(aasRegistryUrl);
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
