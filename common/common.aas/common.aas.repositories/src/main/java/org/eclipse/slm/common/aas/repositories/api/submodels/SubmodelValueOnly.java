package org.eclipse.slm.common.aas.repositories.api.submodels;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.digitaltwin.basyx.submodelservice.value.ValueOnly;
import org.eclipse.digitaltwin.basyx.submodelservice.value.mapper.ValueMapperUtil;

public class SubmodelValueOnly {
    private String id;
    private String idShort;
    private String semanticId;
    private Map<String, SubmodelElementValue> submodelValuesMap;

    public SubmodelValueOnly() {
    }

    public SubmodelValueOnly(Collection<SubmodelElement> submodelElements) {
        this.submodelValuesMap = (Map)submodelElements.stream().filter(SubmodelValueOnly::hasValueOnlyDefined).map(ValueMapperUtil::toValueOnly).collect(Collectors.toMap(ValueOnly::getIdShort, ValueOnly::getSubmodelElementValue));
    }

    private static boolean hasValueOnlyDefined(SubmodelElement element) {
        return !(element instanceof Operation);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdShort() {
        return this.idShort;
    }

    public void setIdShort(String idShort) {
        this.idShort = idShort;
    }

    public String getSemanticId() {
        return semanticId;
    }

    public void setSemanticId(String semanticId) {
        this.semanticId = semanticId;
    }

    public Map<String, SubmodelElementValue> getValuesOnlyMap() {
        return this.submodelValuesMap;
    }

    public void setValuesOnlyMap(Map<String, SubmodelElementValue> submodelValuesMap) {
        this.submodelValuesMap = submodelValuesMap;
    }
}
