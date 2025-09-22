package org.eclipse.slm.resource_management.common.resource_types.aas.submodels;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationSupport;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.slm.common.aas.repositories.exceptions.SubmodelNotFoundException;
import org.eclipse.slm.common.aas.repositories.submodels.AbstractSubmodelRepository;
import org.eclipse.slm.resource_management.common.resource_types.ResourceTypesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ResourceTypesSubmodelRepository extends AbstractSubmodelRepository {

    public final static Logger LOG = LoggerFactory.getLogger(ResourceTypesSubmodelRepository.class);

    private final String aasId;

    private final ResourceTypesManager resourceTypesManager;

    public ResourceTypesSubmodelRepository(String aasId, ResourceTypesManager resourceTypesManager) {
        super(aasId);
        this.aasId = aasId;
        this.resourceTypesManager = resourceTypesManager;
    }

    @Override
    public CursorResult<List<Submodel>> getAllSubmodels(PaginationInfo pInfo) {
        var submodels = new ArrayList<Submodel>();
        var resourceTypes = resourceTypesManager.getResourceTypes();
        for (var resourceType : resourceTypes) {
            var resourceTypeSubmodel = new ResourceTypeSubmodel(resourceType);
            submodels.add(resourceTypeSubmodel);
        }

        TreeMap<String, Submodel> submodelMap = submodels.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Submodel::getId,
                        submodel -> submodel,
                        (a, b) -> a,
                        TreeMap::new
                ));
        PaginationSupport<Submodel> paginationSupport = new PaginationSupport<>(submodelMap, Submodel::getId);

        return paginationSupport.getPaged(pInfo);
    }

    @Override
    public Submodel getSubmodel(String submodelId) throws ElementDoesNotExistException {
        var resourceTypes = resourceTypesManager.getResourceTypes();

        var prefixSplit = submodelId.split("-");
        if (prefixSplit.length == 3) {
            var resourceTypeName = prefixSplit[2];

            var resourceTypeOptional = resourceTypes.stream()
                    .filter(resourceType -> resourceType.getTypeName().equals(resourceTypeName))
                    .findAny();
            if (resourceTypeOptional.isPresent()) {
                var resourceTypeSubmodel = new ResourceTypeSubmodel(resourceTypeOptional.get());
                return resourceTypeSubmodel;
            }
        }

        throw new SubmodelNotFoundException(aasId, submodelId);
    }

    @Override
    public CursorResult<List<SubmodelElement>> getSubmodelElements(String submodelId, PaginationInfo pInfo) throws ElementDoesNotExistException {
        var submodel = this.getSubmodel(submodelId);
        var result = this.generateCursorResult(submodel.getSubmodelElements(), pInfo);

        return result;
    }

    @Override
    public SubmodelElement getSubmodelElement(String submodelId, String smeIdShort) throws ElementDoesNotExistException {
        var submodel = this.getSubmodel(submodelId);
        var submodelElement = this.getSubmodelElementForSubmodel(submodel, smeIdShort);

        return submodelElement;
    }

    @Override
    public SubmodelElementValue getSubmodelElementValue(String submodelId, String smeIdShort) throws ElementDoesNotExistException {
        var submodelElement = this.getSubmodelElement(submodelId, smeIdShort);
        var submodelElementValue = this.getSubmodelElementValueForSubmodel(submodelElement);

        return submodelElementValue;
    }
}
