package org.eclipse.slm.common.messaging;

public class ResourceInformationEntry {

    public String semanticId;

    public String submodelId;

    public ResourceInformationEntry(String semanticId, String submodelId) {
        this.semanticId = semanticId;
        this.submodelId = submodelId;
    }

    public String getSemanticId() {
        return semanticId;
    }

    public void setSemanticId(String semanticId) {
        this.semanticId = semanticId;
    }

    public String getSubmodelId() {
        return submodelId;
    }

    public void setSubmodelId(String submodelId) {
        this.submodelId = submodelId;
    }
}
