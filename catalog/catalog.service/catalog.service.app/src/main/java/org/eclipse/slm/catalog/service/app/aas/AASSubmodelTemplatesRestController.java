package org.eclipse.slm.catalog.service.app.aas;

import org.eclipse.slm.catalog.model.AASSubmodelTemplate;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog/aas")
public class AASSubmodelTemplatesRestController {

    public final static Logger LOG = LoggerFactory.getLogger(AASSubmodelTemplatesRestController.class);

    private final AASSubmodelTemplateManager aasSubmodelTemplateManager;

    public AASSubmodelTemplatesRestController(AASSubmodelTemplateManager aasSubmodelTemplateManager) {
        this.aasSubmodelTemplateManager = aasSubmodelTemplateManager;
    }

    @RequestMapping(value = "/submodels/templates", method = RequestMethod.GET)
    @Operation(summary = "Get AAS submodel templates")
    public ResponseEntity<List<AASSubmodelTemplate>> getAASSubmodelTemplates() {
        var aasSubmodelTemplates = this.aasSubmodelTemplateManager.getAASSubmodelTemplates();

        return ResponseEntity.ok(aasSubmodelTemplates);
    }

    @RequestMapping(value = "/submodels/templates", method = RequestMethod.POST)
    @Operation(summary = "Add AAS submodel template")
    public ResponseEntity addAASSubmodelTemplate(@RequestBody AASSubmodelTemplate aasSubmodelTemplate) {
        this.aasSubmodelTemplateManager.addAASSubmodelTemplate(aasSubmodelTemplate);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/submodels/templates/{aasSubmodelTemplateId}", method = RequestMethod.DELETE)
    @Operation(summary = "Remove AAS submodel template by id")
    public ResponseEntity removeAASSubmodelTemplate(@PathVariable(name = "aasSubmodelTemplateId") Long aasSubmodelTemplateId) {
        this.aasSubmodelTemplateManager.removeAASSubmodelTemplate(aasSubmodelTemplateId);

        return ResponseEntity.ok().build();
    }

}
