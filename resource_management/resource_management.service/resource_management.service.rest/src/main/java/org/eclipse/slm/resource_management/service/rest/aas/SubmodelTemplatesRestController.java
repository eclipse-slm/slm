package org.eclipse.slm.resource_management.service.rest.aas;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.common.aas.clients.exceptions.ShellNotFoundException;
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

    private final AasRegistryClient aasRegistryClient;

    private final AasRepositoryClient aasRepositoryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    public SubmodelTemplatesRestController(AasRegistryClientFactory aasRegistryClientFactory,
                                           AasRepositoryClientFactory aasRepositoryClientFactory,
                                           SubmodelRegistryClientFactory submodelRegistryClientFactory) {
        this.aasRegistryClient = aasRegistryClientFactory.getClient();
        this.aasRepositoryClient = aasRepositoryClientFactory.getClient();
        this.submodelRegistryClient = submodelRegistryClientFactory.getClient();
    }

    @RequestMapping(value = "/submodels/templates/{smTemplateSemanticId}/instances", method = RequestMethod.GET)
    @Operation(summary = "Get instances of submodel templates using semantic id")
    public ResponseEntity getSubmodelTemplateInstancesBySemanticId(
            @Parameter(in = ParameterIn.PATH, description = "The semantic id to search for submodel instances (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema(implementation = String.class))
            @PathVariable(name = "smTemplateSemanticId") Base64UrlEncodedIdentifier smTemplateSemanticIdBase64Encoded,
            @RequestParam(name = "filterByAasId", required = false) String filterByAasId
    ) {
        var semanticId = smTemplateSemanticIdBase64Encoded.getIdentifier();
        List<Map<String, String>> submodelTemplateInstances = new ArrayList<>();

        var allAASDescriptors = this.aasRegistryClient.getAllShellDescriptors();
        var submodelIdToAasDescriptor = new HashMap<String, AssetAdministrationShellDescriptor>();
        for (var aasDescriptor : allAASDescriptors) {
            var aasOptional = aasRepositoryClient.getAas(aasDescriptor.getId());
            if (aasOptional.isEmpty()) {
                LOG.error("AAS with ID {} not found", aasDescriptor.getId());
                throw new ShellNotFoundException(aasDescriptor.getId());
            }
            var aas = aasOptional.get();

            for (var submodelRef : aas.getSubmodels()) {
                var submodelId = submodelRef.getKeys().get(0).getValue();
                submodelIdToAasDescriptor.put(submodelId, aasDescriptor);
            }
        }

        var allSubmodelDescriptors = this.submodelRegistryClient.getAllSubmodelDescriptors();
        for(var submodelDescriptor : allSubmodelDescriptors) {
            if (submodelDescriptor.getSemanticId() != null) {
                for (var semanticIdKey : submodelDescriptor.getSemanticId().getKeys()) {
                    if (semanticIdKey.getValue().equals(semanticId)) {
                        var aasDescriptor = submodelIdToAasDescriptor.get(submodelDescriptor.getId());
                        if (aasDescriptor != null) {
                            var submodelTemplateInstance = new HashMap<String, String>();
                            submodelTemplateInstance.put("id", aasDescriptor.getId());
                            submodelTemplateInstance.put("name", aasDescriptor.getIdShort());
                            submodelTemplateInstance.put("aasEndpoint", aasDescriptor.getEndpoints().get(0).getProtocolInformation().getHref());
                            submodelTemplateInstance.put("smEndpoint", submodelDescriptor.getEndpoints().get(0).getProtocolInformation().getHref());
                            submodelTemplateInstances.add(submodelTemplateInstance);
                        }
                    }
                }
            }
        }

        if (filterByAasId != null) {
            submodelTemplateInstances = submodelTemplateInstances.stream()
                    .filter(smti -> smti.get("id").equals(filterByAasId))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(submodelTemplateInstances);
    }
}
