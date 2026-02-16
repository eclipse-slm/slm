package org.eclipse.slm.common.parent.service_rest.controller;

import org.eclipse.slm.common.model.SystemVariable;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/variables")
public abstract class VariablesRestController {

    private final SystemVariableHandler systemVariableHandler;

    public VariablesRestController(SystemVariableHandler systemVariableHandler) {
        this.systemVariableHandler = systemVariableHandler;
    }

    @RequestMapping(value = "/system", method = RequestMethod.GET)
    @Operation(summary = "Get system variables")
    public ResponseEntity<List<SystemVariable>> getSystemVariables() {
        var systemVariables = this.systemVariableHandler.getSystemVariablesWithValue();

        return ResponseEntity.ok(systemVariables);
    }

}
