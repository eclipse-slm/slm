package org.eclipse.slm.common.parent.service_rest.controller;

import org.eclipse.slm.common.model.TemplateVariable;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/templates")
public abstract class TemplatesRestController {

    private final TemplateVariableHandler templateVariableHandler;

    public TemplatesRestController(TemplateVariableHandler templateVariableHandler) {
        this.templateVariableHandler = templateVariableHandler;
    }

    @RequestMapping(value = "/variables", method = RequestMethod.GET)
    @Operation(summary = "Get template variables")
    public ResponseEntity<List<TemplateVariable>> getTemplateVariables() {
        var templateVariables = this.templateVariableHandler.getTemplateVariablesWithValue();

        return ResponseEntity.ok(templateVariables);
    }

}
