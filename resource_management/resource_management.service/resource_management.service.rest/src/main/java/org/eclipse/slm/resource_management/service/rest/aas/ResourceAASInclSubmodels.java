package org.eclipse.slm.resource_management.service.rest.aas;

import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ResourceAASInclSubmodels extends AssetAdministrationShell {

    public ResourceAASInclSubmodels(
            AssetAdministrationShell aasLocalCopy,
            Collection<Submodel> submodels
    ) {
        this.putAll(aasLocalCopy);
        this.setSubmodels(submodels);
    }

    @Override
    public void setSubmodels(Collection<Submodel> submodels) {
        this.put(SUBMODELS, submodels);
    }

    @Override
    public Map<String, ISubmodel> getSubmodels() {
        Map<String, ISubmodel> submodelMap = new HashMap<>();

        for(Submodel submodel: (Collection<Submodel>) this.get(SUBMODELS)) {
            submodelMap.put(submodel.getIdShort(), submodel);
        }

        return submodelMap;
    }
}
