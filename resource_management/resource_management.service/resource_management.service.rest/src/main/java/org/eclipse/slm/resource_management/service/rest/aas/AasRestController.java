package org.eclipse.slm.resource_management.service.rest.aas;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesManager;
import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
public class AasRestController {

    private final AasHandler aasHandler;

    private final SubmodelManager submodelManager;


    @Autowired
    public AasRestController(AasHandler aasHandler, SubmodelManager submodelManager) {
        this.submodelManager = submodelManager;
        this.aasHandler = aasHandler;
    }

    @RequestMapping(value = "/{resourceId}/aas-descriptor", method = RequestMethod.GET)
    @Operation(summary = "Get resource submodels")
    public ResponseEntity<AssetAdministrationShellDescriptor> getResourceAasDescriptor(
            @PathVariable(name = "resourceId") UUID resourceId
    ) throws ResourceNotFoundException, ConsulLoginFailedException, JsonProcessingException {
        var aasDescriptor = this.aasHandler.getResourceAasDescriptor(resourceId);

        return ResponseEntity.ok(aasDescriptor.get());
    }
}
