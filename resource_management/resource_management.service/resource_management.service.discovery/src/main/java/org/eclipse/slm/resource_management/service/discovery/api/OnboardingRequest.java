package org.eclipse.slm.resource_management.service.discovery.api;

import java.util.List;

public class OnboardingRequest {

    private List<String> resultIds;

    public List<String> getResultIds() {
        return resultIds;
    }

    public void setResultIds(List<String> resultIds) {
        this.resultIds = resultIds;
    }
}
