package org.eclipse.slm.resource_management.common.aas;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.UUID;

public interface ResourcesAasRestApi {

    @RequestMapping(value = "/{resourceId}/aas-descriptor", method = RequestMethod.GET)
    @Operation(summary = "Get AAS descriptor of resource")
    ResponseEntity<AssetAdministrationShellDescriptor> getResourceAasDescriptor(
            @PathVariable(name = "resourceId") UUID resourceId
    );

    @RequestMapping(value = "/aas", method = RequestMethod.GET)
    @Operation(summary = "Get all AAS of resources")
    List<AssetAdministrationShell> getResourceAASDescriptors();
}

