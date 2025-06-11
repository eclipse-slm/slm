package org.eclipse.slm.common.aas.repositories;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementNotAFileException;
import org.eclipse.digitaltwin.basyx.core.exceptions.FileDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.exceptions.MissingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.slm.common.aas.repositories.api.GetSubmodelsValueOnlyResult;
import org.eclipse.slm.common.aas.repositories.api.SubmodelValueOnly;

public interface SubmodelRepository {
    CursorResult<List<Submodel>> getAllSubmodels(PaginationInfo pInfo);

    GetSubmodelsValueOnlyResult getAllSubmodelsValueOnly(PaginationInfo pInfo);

    Submodel getSubmodel(String submodelId) throws ElementDoesNotExistException;

    void updateSubmodel(String submodelId, Submodel submodel) throws ElementDoesNotExistException;

    void createSubmodel(Submodel submodel) throws CollidingIdentifierException, MissingIdentifierException;

    void updateSubmodelElement(String submodelIdentifier, String idShortPath, SubmodelElement submodelElement) throws ElementDoesNotExistException;

    void deleteSubmodel(String submodelId) throws ElementDoesNotExistException;

    CursorResult<List<SubmodelElement>> getSubmodelElements(String submodelId, PaginationInfo pInfo) throws ElementDoesNotExistException;

    SubmodelElement getSubmodelElement(String submodelId, String smeIdShort) throws ElementDoesNotExistException;

    SubmodelElementValue getSubmodelElementValue(String submodelId, String smeIdShort) throws ElementDoesNotExistException;

    void setSubmodelElementValue(String submodelId, String smeIdShort, SubmodelElementValue value) throws ElementDoesNotExistException;

    void createSubmodelElement(String submodelId, SubmodelElement smElement);

    void createSubmodelElement(String submodelId, String idShortPath, SubmodelElement smElement) throws ElementDoesNotExistException;

    void deleteSubmodelElement(String submodelId, String idShortPath) throws ElementDoesNotExistException;

    default String getName() {
        return "sm-repo";
    }

    OperationVariable[] invokeOperation(String submodelId, String idShortPath, OperationVariable[] input) throws ElementDoesNotExistException;

    SubmodelValueOnly getSubmodelByIdValueOnly(String submodelId) throws ElementDoesNotExistException;

    Submodel getSubmodelByIdMetadata(String submodelId) throws ElementDoesNotExistException;

    File getFileByPathSubmodel(String submodelId, String idShortPath) throws ElementDoesNotExistException, ElementNotAFileException, FileDoesNotExistException;

    void setFileValue(String submodelId, String idShortPath, String fileName, InputStream inputStream) throws ElementDoesNotExistException, ElementNotAFileException;

    void deleteFileValue(String submodelId, String idShortPath) throws ElementDoesNotExistException, ElementNotAFileException, FileDoesNotExistException;

    void patchSubmodelElements(String submodelId, List<SubmodelElement> submodelElementList);

    InputStream getFileByFilePath(String submodelId, String filePath);
}
