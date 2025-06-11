package org.eclipse.slm.resource_management.service.rest.aas;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.model.resource.ResourceAas;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
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
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var resource = this.resourcesManager.getResourceWithCredentialsByRemoteAccessService(jwtAuthenticationToken, resourceId);
        return ResponseEntity.ok(submodelManager.getSubmodels(resource));
    }

    @RequestMapping(value = "/{resourceId}/submodels", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, method = RequestMethod.POST)
    @Operation(summary = "Add submodels to existing resource")
    public ResponseEntity addSubmodels(
            @PathVariable(name = "resourceId") UUID resourceId,
            @RequestParam(name = "aasx") MultipartFile aasxFile
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IOException, InvalidFormatException, DeserializationException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var resource = this.resourcesManager.getResourceWithCredentialsByRemoteAccessService(jwtAuthenticationToken, resourceId);
        var aasxFileInputStream = new BufferedInputStream(aasxFile.getInputStream());

        this.submodelManager.addSubmodelsFromAASX(ResourceAas.createAasIdFromResourceId(resource.getId()), aasxFileInputStream);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{resourceId}/submodels/{submodelIdBase64Encoded}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete resource submodel")
    public ResponseEntity deleteSubmodel(
            @PathVariable(name = "resourceId") UUID resourceId,
            @PathVariable(name = "submodelIdBase64Encoded") String submodelIdBase64Encoded
    ) throws ConsulLoginFailedException, ResourceNotFoundException, JsonProcessingException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var resource = this.resourcesManager.getResourceWithCredentialsByRemoteAccessService(jwtAuthenticationToken, resourceId);
        try {
            var submodelId = new String(Base64.decodeBase64(submodelIdBase64Encoded));
            submodelManager.deleteSubmodel(resource.getId(), submodelId);
        } catch (NullPointerException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
