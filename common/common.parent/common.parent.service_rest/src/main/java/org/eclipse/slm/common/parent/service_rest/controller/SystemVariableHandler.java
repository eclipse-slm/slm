package org.eclipse.slm.common.parent.service_rest.controller;

import org.eclipse.slm.common.model.SystemVariable;
import org.eclipse.slm.common.model.SystemVariableValueSource;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SystemVariableHandler {

    protected Map<String, SystemVariable> systemVariables = new HashMap<>();

    private Environment environment;

    public SystemVariableHandler(Environment environment) {
        this.environment = environment;
    }

    public void addSystemVariable(SystemVariable systemVariable) {
        this.systemVariables.put(systemVariable.getKey(), systemVariable);
    }

    public List<SystemVariable> getSystemVariablesWithValue() {
        var systemVariables = new ArrayList<SystemVariable>();

        for (var systemVariable : this.systemVariables.values()) {
            var value = this.getValueForSystemVariable(systemVariable);
            systemVariable.setValue(value);
            systemVariables.add(systemVariable);
        }

        return systemVariables;
    }

    public Object getValueForSystemVariable(String systemVariableKey) {
        var systemVariable = this.systemVariables.get(systemVariableKey);
        return this.getValueForSystemVariable(systemVariable);
    }

    public Object getValueForSystemVariable(SystemVariable systemVariable) {
        if (systemVariable.getValueSource().equals(SystemVariableValueSource.APPLICATION_PROPERTIES)) {
            var value = this.environment.getProperty(systemVariable.getValuePath());
            return value;
        }
        else {
            throw new NotImplementedException("System variable values for source '"  + systemVariable.getValueSource() + "' not implemented");
        }
    }

}
