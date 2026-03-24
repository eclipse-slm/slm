package org.eclipse.slm.common.aas.repositories.api.submodels;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.http.pagination.Base64UrlEncodedCursor;
import org.eclipse.digitaltwin.basyx.http.pagination.PagedResult;
import org.eclipse.digitaltwin.basyx.http.pagination.PagedResultPagingMetadata;
import org.eclipse.digitaltwin.basyx.pagination.GetSubmodelElementsResult;
import org.eclipse.digitaltwin.basyx.submodelservice.SubmodelService;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelValueOnly;
import org.eclipse.slm.common.aas.repositories.exceptions.MethodNotImplementedException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/{aasId}")
public abstract class MultiSubmodelServiceHTTPApiController implements MultiSubmodelServiceHTTPApi {

	private static final PaginationInfo NO_LIMIT_PAGINATION_INFO = new PaginationInfo(0, null);

	private SubmodelService service;

	@Override
	public ResponseEntity<Void> deleteSubmodelElementByPath(String aasId, String idShortPath) {
		throw new MethodNotImplementedException();
	}

	@Override
	public ResponseEntity<PagedResult> getAllSubmodelElements(String aasId, Integer limit, Base64UrlEncodedCursor cursor, String level, String extent) {
		if (limit == null) {
			limit = 100;
		}

		String decodedCursor = "";
		if (cursor != null) {
			decodedCursor = cursor.getDecodedCursor();
		}

		PaginationInfo pInfo = new PaginationInfo(limit, decodedCursor);
		CursorResult<List<SubmodelElement>> submodelElements = service.getSubmodelElements(pInfo);

		GetSubmodelElementsResult paginatedSubmodelElement = new GetSubmodelElementsResult();

		String encodedCursor = getEncodedCursorFromCursorResult(submodelElements);

		paginatedSubmodelElement.setResult(submodelElements.getResult());
		paginatedSubmodelElement.setPagingMetadata(new PagedResultPagingMetadata().cursor(encodedCursor));

		return new ResponseEntity<PagedResult>(paginatedSubmodelElement, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Submodel> getSubmodel(String aasId, String level, String extent) {
		Submodel submodel = service.getSubmodel();

		return new ResponseEntity<Submodel>(submodel, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SubmodelElement> getSubmodelElementByPath(String aasId, String idShortPath, Integer limit, Base64UrlEncodedCursor cursor, String level, String extent) {
		SubmodelElement submodelElement = service.getSubmodelElement(idShortPath);

		return new ResponseEntity<SubmodelElement>(submodelElement, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SubmodelElementValue> getSubmodelElementByPathValueOnly(String aasId, String idShortPath, Integer limit, Base64UrlEncodedCursor cursor, String level, String extent) {
		SubmodelElementValue submodelElementValue = service.getSubmodelElementValue(idShortPath);

		return new ResponseEntity<SubmodelElementValue>(submodelElementValue, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Submodel> getSubmodelMetadata(String aasId, String level) {
		Submodel submodel = service.getSubmodel();
		submodel.setSubmodelElements(null);

		return new ResponseEntity<Submodel>(submodel, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SubmodelValueOnly> getSubmodelValueOnly(String aasId, String level, String extent) {
		SubmodelValueOnly result = new SubmodelValueOnly(service.getSubmodelElements(NO_LIMIT_PAGINATION_INFO).getResult());

		return new ResponseEntity<SubmodelValueOnly>(result, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> patchSubmodelElementByPathValueOnly(String aasId, String idShortPath, SubmodelElementValue body, Integer limit, Base64UrlEncodedCursor cursor, String level) {
		throw new MethodNotImplementedException();
	}

	@Override
	public ResponseEntity<SubmodelElement> postSubmodelElement(String aasId, SubmodelElement body, String level) {
		throw new MethodNotImplementedException();
	}

	@Override
	public ResponseEntity<SubmodelElement> postSubmodelElementByPath(String aasId, String idShortPath, SubmodelElement body) {
		throw new MethodNotImplementedException();
	}

	@Override
	public ResponseEntity<OperationResult> invokeOperation(String aasId, String idShortPath, OperationRequest body) {
		throw new MethodNotImplementedException();
	}

	@Override
	public ResponseEntity<Void> putSubmodelElementByPath(String aasId, String idShortPath, SubmodelElement body, String level) {
		throw new MethodNotImplementedException();
	}

	@Override
	public ResponseEntity<Void> patchSubmodelValueOnly(String aasId, List<SubmodelElement> body, String level) {
		throw new MethodNotImplementedException();
	}

	@Override
	public ResponseEntity<Void> deleteFileByPath(String aasId, String idShortPath) {
		throw new MethodNotImplementedException();
	}

	@Override
	public ResponseEntity<Resource> getFileByPath(String aasId, String idShortPath) {
		throw new MethodNotImplementedException();
	}

	@Override
	public ResponseEntity<Void> putFileByPath(String aasId, String idShortPath, String fileName, MultipartFile file) {
		throw new MethodNotImplementedException();
	}

	private String getEncodedCursorFromCursorResult(CursorResult<?> cursorResult) {
		if (cursorResult == null || cursorResult.getCursor() == null) {
			return null;
		}

		return Base64UrlEncodedCursor.encodeCursor(cursorResult.getCursor());
	}
}
