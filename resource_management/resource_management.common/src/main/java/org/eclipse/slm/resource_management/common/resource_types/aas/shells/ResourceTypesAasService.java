package org.eclipse.slm.resource_management.common.resource_types.aas.shells;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.slm.common.aas.repositories.shells.AbstractAasService;
import org.eclipse.slm.common.aas.repositories.submodels.SubmodelServiceFactory;
import org.eclipse.slm.resource_management.common.resource_types.aas.submodels.ResourceTypeSubmodelRepositoryFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResourceTypesAasService extends AbstractAasService {


    public ResourceTypesAasService(ResourceTypesAasFactory resourceTypesAasFactory,
                                   ResourceTypeSubmodelRepositoryFactory resourceTypeSubmodelRepositoryFactory
    ) {
        super(ResourceTypesAas.RESOURCE_TYPE_AAS_ID, resourceTypesAasFactory,
                new HashMap<String, SubmodelServiceFactory>(),
                Map.of("ResourceType", resourceTypeSubmodelRepositoryFactory)
        );
    }

    @Override
    public AssetInformation getAssetInformation() throws ElementDoesNotExistException {
        return null;
    }
}
