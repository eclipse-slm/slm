package org.eclipse.slm.common.aas.repositories;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementNotAFileException;
import org.eclipse.digitaltwin.basyx.core.exceptions.FileDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationSupport;
import org.eclipse.digitaltwin.basyx.submodelservice.SubmodelService;
import org.eclipse.digitaltwin.basyx.submodelservice.pathparsing.HierarchicalSubmodelElementParser;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.digitaltwin.basyx.submodelservice.value.factory.SubmodelElementValueMapperFactory;
import org.eclipse.slm.common.aas.repositories.exceptions.MethodNotImplementedException;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public abstract class AbstractSubmodelService implements SubmodelService {

    protected AbstractSubmodelService() {
    }

    @Override
    public abstract Submodel getSubmodel();

    @Override
    public CursorResult<List<SubmodelElement>> getSubmodelElements(PaginationInfo pInfo) {
        List<SubmodelElement> allSubmodelElements = this.getSubmodel().getSubmodelElements();

        TreeMap<String, SubmodelElement> submodelMap = allSubmodelElements.stream().collect(Collectors.toMap(SubmodelElement::getIdShort, aas -> aas, (a, b) -> a, TreeMap::new));

        PaginationSupport<SubmodelElement> paginationSupport = new PaginationSupport<>(submodelMap, SubmodelElement::getIdShort);
        CursorResult<List<SubmodelElement>> paginatedSubmodels = paginationSupport.getPaged(pInfo);
        return paginatedSubmodels;
    }

    @Override
    public SubmodelElement getSubmodelElement(String idShortPath) throws ElementDoesNotExistException {
        var parser = new HierarchicalSubmodelElementParser(this.getSubmodel());

        return parser.getSubmodelElementFromIdShortPath(idShortPath);
    }

    @Override
    public SubmodelElementValue getSubmodelElementValue(String idShortPath) throws ElementDoesNotExistException {
        SubmodelElementValueMapperFactory submodelElementValueFactory = new SubmodelElementValueMapperFactory();

        return submodelElementValueFactory.create(getSubmodelElement(idShortPath)).getValue();
    }

    @Override
    public void setSubmodelElementValue(String idShortPath, SubmodelElementValue value) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void createSubmodelElement(SubmodelElement submodelElement) {
        throw new MethodNotImplementedException();
    }

    @Override
    public void createSubmodelElement(String idShortPath, SubmodelElement submodelElement) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void updateSubmodelElement(String idShortPath, SubmodelElement submodelElement) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void deleteSubmodelElement(String idShortPath) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void patchSubmodelElements(List<SubmodelElement> submodelElementList) {
        throw new MethodNotImplementedException();
    }

    @Override
    public OperationVariable[] invokeOperation(String idShortPath, OperationVariable[] input) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public File getFileByPath(String idShortPath) throws ElementDoesNotExistException, ElementNotAFileException, FileDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void setFileValue(String idShortPath, String fileName, InputStream inputStream) throws ElementDoesNotExistException, ElementNotAFileException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void deleteFileValue(String idShortPath) throws ElementDoesNotExistException, ElementNotAFileException, FileDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public InputStream getFileByFilePath(String filePath) {
        throw new MethodNotImplementedException();
    }
}
