package org.eclipse.slm.common.aas.repositories;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementNotAFileException;
import org.eclipse.digitaltwin.basyx.core.exceptions.FileDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;

import java.io.InputStream;
import java.util.List;


public interface SubmodelService {

	public Submodel getSubmodel();

	public CursorResult<List<SubmodelElement>> getSubmodelElements(PaginationInfo pInfo);

	public SubmodelElement getSubmodelElement(String idShortPath) throws ElementDoesNotExistException;

	public SubmodelElementValue getSubmodelElementValue(String idShortPath) throws ElementDoesNotExistException;


	public void setSubmodelElementValue(String idShortPath, SubmodelElementValue value) throws ElementDoesNotExistException;

	public void createSubmodelElement(SubmodelElement submodelElement);

	public void createSubmodelElement(String idShortPath, SubmodelElement submodelElement) throws ElementDoesNotExistException;

	public void updateSubmodelElement(String idShortPath, SubmodelElement submodelElement) throws ElementDoesNotExistException;

	public void deleteSubmodelElement(String idShortPath) throws ElementDoesNotExistException;

	public void patchSubmodelElements(List<SubmodelElement> submodelElementList);

	public OperationVariable[] invokeOperation(String idShortPath, OperationVariable[] input) throws ElementDoesNotExistException;

	public java.io.File getFileByPath(String idShortPath) throws ElementDoesNotExistException, ElementNotAFileException, FileDoesNotExistException;

	public void setFileValue(String idShortPath, String fileName, InputStream inputStream) throws ElementDoesNotExistException, ElementNotAFileException;

	public void deleteFileValue(String idShortPath) throws ElementDoesNotExistException, ElementNotAFileException, FileDoesNotExistException;

}
