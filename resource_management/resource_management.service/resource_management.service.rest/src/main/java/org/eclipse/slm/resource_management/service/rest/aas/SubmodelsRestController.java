package org.eclipse.slm.resource_management.service.rest.aas;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesManager;
import org.eclipse.slm.resource_management.model.resource.BasicResource;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
public class SubmodelsRestController {

    private final ResourcesManager resourcesManager;
    private final SubmodelManager submodelManager;

    @Autowired
    public SubmodelsRestController(
            ResourcesManager resourcesManager,
            SubmodelManager submodelManager
    ) {
        this.resourcesManager = resourcesManager;
        this.submodelManager = submodelManager;
    }

    @RequestMapping(value = "/{resourceId}/submodels", method = RequestMethod.GET)
    @Operation(summary = "Get resource submodels")
    public ResponseEntity getResourceSubmodels(
            @PathVariable(name = "resourceId") UUID resourceId
    ) throws ResourceNotFoundException, ConsulLoginFailedException, JsonProcessingException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

//        var resource = this.resourcesManager.getResource(keycloakPrincipal, resourceId);
        var resource = this.resourcesManager.getResourceWithCredentialsByRemoteAccessService(keycloakPrincipal, resourceId);
        return ResponseEntity.ok(submodelManager.getSubmodels(resource));
    }

    @RequestMapping(value = "/{resourceId}/submodels", method = RequestMethod.POST)
    @Operation(summary = "Add submodels to existing resource")
    public ResponseEntity addSubmodels(
            @PathVariable(name = "resourceId") UUID resourceId,
            @RequestParam("aasx") MultipartFile multipartFile
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IOException, ParserConfigurationException, InvalidFormatException, SAXException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        BasicResource resource = this.resourcesManager.getResourceWithCredentialsByRemoteAccessService(keycloakPrincipal, resourceId);
        InputStream inputStream = new BufferedInputStream(multipartFile.getInputStream());
        return ResponseEntity.ok(submodelManager.addSubmodels(resource, inputStream));
    }

    @RequestMapping(value = "/{resourceId}/submodels/{submodelIdShort}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete resource submodel")
    public ResponseEntity deleteSubmodel(
            @PathVariable(name = "resourceId") UUID resourceId,
            @PathVariable(name = "submodelIdShort") String submodelIdShort
    ) throws ConsulLoginFailedException, ResourceNotFoundException, JsonProcessingException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        BasicResource resource = this.resourcesManager.getResource(keycloakPrincipal, resourceId);
        BasicResource resource = this.resourcesManager.getResourceWithCredentialsByRemoteAccessService(keycloakPrincipal, resourceId);
        try {
            submodelManager.deleteSubmodel(resource, submodelIdShort);
        } catch (NullPointerException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
