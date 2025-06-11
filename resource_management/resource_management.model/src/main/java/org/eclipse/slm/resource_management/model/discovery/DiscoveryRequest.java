package org.eclipse.slm.resource_management.model.discovery;

import java.util.HashMap;
import java.util.Map;

public class DiscoveryRequest {

    private Map<String, String> filterValues = new HashMap<>();

    private Map<String, String> optionValues = new HashMap<>();

    public Map<String, String> getFilterValues() {
        return filterValues;
    }

    public void setFilterValues(Map<String, String> filterValues) {
        this.filterValues = filterValues;
    }

    public Map<String, String> getOptionValues() {
        return optionValues;
    }

    public void setOptionValues(Map<String, String> optionValues) {
        this.optionValues = optionValues;
    }
}
