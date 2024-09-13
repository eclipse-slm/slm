package org.eclipse.slm.common.aas.repositories;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.exceptions.*;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationSupport;
import org.eclipse.digitaltwin.basyx.submodelrepository.SubmodelRepository;
import org.eclipse.digitaltwin.basyx.submodelservice.SubmodelService;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelValueOnly;
import org.eclipse.slm.common.aas.repositories.exceptions.MethodNotImplementedException;
import org.eclipse.slm.common.aas.repositories.exceptions.SubmodelNotFoundException;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractSubmodelRepository implements SubmodelRepository {

    private static final PaginationInfo NO_LIMIT_PAGINATION_INFO = new PaginationInfo(0, null);

    private String aasId;

    private Map<String, SubmodelServiceFactory> submodelServiceFactories = new HashMap<>();

    public AbstractSubmodelRepository(String aasId) {
        this.aasId = aasId;
    }

    public void addSubmodelServiceFactory(String submodelId, SubmodelServiceFactory factory) {
        this.submodelServiceFactories.put(submodelId, factory);
    }

    public void removeSubmodelServiceFactory(String submodelId) {
        this.submodelServiceFactories.remove(submodelId);
    }

    @Override
    public CursorResult<List<Submodel>> getAllSubmodels(PaginationInfo pInfo) {
        var submodels = new ArrayList<Submodel>();
        for (var submodelFactory : submodelServiceFactories.values()) {
            var submodel = submodelFactory.getSubmodelService(this.aasId).getSubmodel();
            submodels.add(submodel);
        }

        TreeMap<String, Submodel> submodelMap = submodels.stream().collect(Collectors.toMap(Submodel::getId, submodel -> submodel, (a, b) -> a, TreeMap::new));
        PaginationSupport<Submodel> paginationSupport = new PaginationSupport<>(submodelMap, Submodel::getId);

        return paginationSupport.getPaged(pInfo);
    }

    @Override
    public Submodel getSubmodel(String submodelId) throws ElementDoesNotExistException {
        var submodel = this.getSubmodelServiceBySubmodelId(submodelId).getSubmodel();
        return submodel;
    }

    @Override
    public void updateSubmodel(String submodelId, Submodel submodel) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void createSubmodel(Submodel submodel) throws CollidingIdentifierException, MissingIdentifierException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void updateSubmodelElement(String submodelIdentifier, String idShortPath, SubmodelElement submodelElement) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void deleteSubmodel(String submodelId) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public CursorResult<List<SubmodelElement>> getSubmodelElements(String submodelId, PaginationInfo pInfo) throws ElementDoesNotExistException {
        var submodelElementsResult = this.getSubmodelServiceBySubmodelId(submodelId).getSubmodelElements(pInfo);

        return submodelElementsResult;
    }

    @Override
    public SubmodelElement getSubmodelElement(String submodelId, String smeIdShort) throws ElementDoesNotExistException {
        var submodelElement = this.getSubmodelServiceBySubmodelId(submodelId).getSubmodelElement(smeIdShort);

        return submodelElement;
    }

    @Override
    public SubmodelElementValue getSubmodelElementValue(String submodelId, String smeIdShort) throws ElementDoesNotExistException {
        var submodelElementValue = this.getSubmodelServiceBySubmodelId(submodelId).getSubmodelElementValue(smeIdShort);

        return submodelElementValue;
    }

    @Override
    public void setSubmodelElementValue(String submodelId, String smeIdShort, SubmodelElementValue value) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void createSubmodelElement(String submodelId, SubmodelElement smElement) {
        throw new MethodNotImplementedException();
    }

    @Override
    public void createSubmodelElement(String submodelId, String idShortPath, SubmodelElement smElement) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void deleteSubmodelElement(String submodelId, String idShortPath) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public OperationVariable[] invokeOperation(String submodelId, String idShortPath, OperationVariable[] input) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public SubmodelValueOnly getSubmodelByIdValueOnly(String submodelId) throws ElementDoesNotExistException {
        var submodelValueOnly = new SubmodelValueOnly(this.getSubmodelElements(submodelId, NO_LIMIT_PAGINATION_INFO).getResult());

        return submodelValueOnly;
    }

    @Override
    public Submodel getSubmodelByIdMetadata(String submodelId) throws ElementDoesNotExistException {
        var submodel = getSubmodel(submodelId);

        return submodel;
    }

    @Override
    public File getFileByPathSubmodel(String submodelId, String idShortPath) throws ElementDoesNotExistException, ElementNotAFileException, FileDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void setFileValue(String submodelId, String idShortPath, String fileName, InputStream inputStream) throws ElementDoesNotExistException, ElementNotAFileException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void deleteFileValue(String submodelId, String idShortPath) throws ElementDoesNotExistException, ElementNotAFileException, FileDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void patchSubmodelElements(String submodelId, List<SubmodelElement> submodelElementList) {
        throw new MethodNotImplementedException();
    }

    @Override
    public InputStream getFileByFilePath(String submodelId, String filePath) {
        throw new MethodNotImplementedException();
    }

    private SubmodelServiceFactory getSubmodelFactoryBySubmodelId(String submodelId) {
        var prefixSplit = submodelId.split("-");
        if (prefixSplit.length > 1) {
            var submodelPrefix = prefixSplit[0];
            return this.submodelServiceFactories.get(submodelPrefix);
        }

        throw new SubmodelNotFoundException(this.aasId, submodelId);
    }

    private SubmodelService getSubmodelServiceBySubmodelId(String submodelId) {
        return this.getSubmodelFactoryBySubmodelId(submodelId).getSubmodelService(this.aasId);
    }
}
