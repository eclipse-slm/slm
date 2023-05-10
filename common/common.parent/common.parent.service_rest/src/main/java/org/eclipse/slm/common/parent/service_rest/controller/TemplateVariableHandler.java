package org.eclipse.slm.common.parent.service_rest.controller;

import org.eclipse.slm.common.model.TemplateVariable;
import org.eclipse.slm.common.model.TemplateVariableValueSource;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TemplateVariableHandler {

    protected Map<String, TemplateVariable> templateVariables = new HashMap<>();

    private Environment environment;

    public TemplateVariableHandler(Environment environment) {
        this.environment = environment;
    }

    public void addTemplateVariable(TemplateVariable templateVariable) {
        this.templateVariables.put(templateVariable.getKey(), templateVariable);
    }

    public List<TemplateVariable> getTemplateVariablesWithValue() {
        var templateVariables = new ArrayList<TemplateVariable>();

        for (var templateVariable : this.templateVariables.values()) {
            var value = this.getValueForTemplateVariable(templateVariable);
            templateVariable.setValue(value);
            templateVariables.add(templateVariable);
        }

        return templateVariables;
    }

    public Object getValueForTemplateVariable(String templateVariableKey) {
        var templateVariable = this.templateVariables.get(templateVariableKey);
        return this.getValueForTemplateVariable(templateVariable);
    }

    public Object getValueForTemplateVariable(TemplateVariable templateVariable) {
        if (templateVariable.getValueSource().equals(TemplateVariableValueSource.APPLICATION_PROPERTIES)) {
            var value = this.environment.getProperty(templateVariable.getValuePath());
            return value;
        }
        else {
            throw new NotImplementedException("Template variable values for source '"  + templateVariable.getValueSource() + "' not implemented");
        }
    }

}
