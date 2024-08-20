package org.eclipse.slm.common.aas.clients;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.submodelservice.client.ConnectedSubmodelService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubmodelServiceClient {

    private final ConnectedSubmodelService submodelService;

    public SubmodelServiceClient(String submodelServiceUrl) {
        this.submodelService = new ConnectedSubmodelService(submodelServiceUrl);
    }

    public Submodel getSubmodel() {
        var submodel = this.submodelService.getSubmodel();

        return submodel;
    }

    public List<SubmodelElement> getSubmodelElements() {
        var submodelElements = this.getSubmodel().getSubmodelElements();

        return submodelElements;
    }

    public Map<String, Object> getSubmodelValues() {
        var submodelElementValues = new HashMap<String, Object>();
        var submodelElements = this.getSubmodelElements();

        for (var submodelElement : submodelElements) {
            if (submodelElement instanceof Property) {
                submodelElementValues.put(submodelElement.getIdShort(), ((Property)submodelElement).getValue());
            }
        }

        return submodelElementValues;
    }
}
