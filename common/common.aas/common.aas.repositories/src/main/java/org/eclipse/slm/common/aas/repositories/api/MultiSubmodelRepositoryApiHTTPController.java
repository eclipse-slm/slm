package org.eclipse.slm.common.aas.repositories.api;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationRequest;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.digitaltwin.basyx.http.pagination.Base64UrlEncodedCursor;
import org.eclipse.digitaltwin.basyx.http.pagination.PagedResult;
import org.eclipse.digitaltwin.basyx.http.pagination.PagedResultPagingMetadata;
import org.eclipse.digitaltwin.basyx.pagination.GetSubmodelElementsResult;
import org.eclipse.digitaltwin.basyx.submodelrepository.http.pagination.GetSubmodelsResult;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelValueOnly;
import org.eclipse.slm.common.aas.repositories.SubmodelRepositoryFactory;
import org.eclipse.slm.common.aas.repositories.exceptions.MethodNotImplementedException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/{aasId}")
public abstract class MultiSubmodelRepositoryApiHTTPController implements MultiSubmodelRepositoryHTTPApi {

    private SubmodelRepositoryFactory submodelRepositoryFactory;

    public MultiSubmodelRepositoryApiHTTPController(SubmodelRepositoryFactory submodelRepositoryFactory) {
        this.submodelRepositoryFactory = submodelRepositoryFactory;
    }

    @Override
    public ResponseEntity<PagedResult> getAllSubmodels(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier semanticId, String idShort, Integer limit, Base64UrlEncodedCursor cursor, String level, String extent) {
        if (limit == null) {
            limit = 100;
        }

        String decodedCursor = "";
        if (cursor != null) {
            decodedCursor = cursor.getDecodedCursor();
        }

        PaginationInfo pInfo = new PaginationInfo(limit, decodedCursor);

        CursorResult<List<Submodel>> cursorResult = submodelRepositoryFactory.getSubmodelRepository(aasId.getIdentifier()).getAllSubmodels(pInfo);

        GetSubmodelsResult paginatedSubmodel = new GetSubmodelsResult();

        String encodedCursor = getEncodedCursorFromCursorResult(cursorResult);

        paginatedSubmodel.result(new ArrayList<>(cursorResult.getResult()));
        paginatedSubmodel.setPagingMetadata(new PagedResultPagingMetadata().cursor(encodedCursor));

        return new ResponseEntity<PagedResult>(paginatedSubmodel, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Submodel> getSubmodelById(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String level, String extent) {
        return new ResponseEntity<Submodel>(submodelRepositoryFactory.getSubmodelRepository(aasId.getIdentifier()).getSubmodel(submodelIdentifier.getIdentifier()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SubmodelValueOnly> getSubmodelByIdValueOnly(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String level, String extent) {
        return new ResponseEntity<SubmodelValueOnly>(submodelRepositoryFactory.getSubmodelRepository(aasId.getIdentifier()).getSubmodelByIdValueOnly(submodelIdentifier.getIdentifier()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Submodel> getSubmodelByIdMetadata(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String level) {
        return new ResponseEntity<Submodel>(submodelRepositoryFactory.getSubmodelRepository(aasId.getIdentifier()).getSubmodelByIdMetadata(submodelIdentifier.getIdentifier()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Submodel> postSubmodel(Base64UrlEncodedIdentifier aasId, Submodel body) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> putSubmodelById(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, Submodel body, String level) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> deleteSubmodelById(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<PagedResult> getAllSubmodelElements(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, Integer limit, Base64UrlEncodedCursor cursor, String level, String extent) {
        if (limit == null) {
            limit = 100;
        }

        String decodedCursor = "";
        if (cursor != null) {
            decodedCursor = cursor.getDecodedCursor();
        }

        PaginationInfo pInfo = new PaginationInfo(limit, decodedCursor);
        CursorResult<List<SubmodelElement>> cursorResult = submodelRepositoryFactory.getSubmodelRepository(aasId.getIdentifier()).getSubmodelElements(submodelIdentifier.getIdentifier(), pInfo);

        GetSubmodelElementsResult paginatedSubmodelElement = new GetSubmodelElementsResult();
        String encodedCursor = getEncodedCursorFromCursorResult(cursorResult);

        paginatedSubmodelElement.result(new ArrayList<>(cursorResult.getResult()));
        paginatedSubmodelElement.setPagingMetadata(new PagedResultPagingMetadata().cursor(encodedCursor));

        return new ResponseEntity<PagedResult>(paginatedSubmodelElement, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SubmodelElement> getSubmodelElementByPathSubmodelRepo(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, String level, String extent) {
        return handleSubmodelElementValueNormalGetRequest(aasId, submodelIdentifier.getIdentifier(), idShortPath);
    }

    @Override
    public ResponseEntity<SubmodelElementValue> getSubmodelElementByPathValueOnlySubmodelRepo(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, String level, String extent) {
        return handleSubmodelElementValueGetRequest(aasId, submodelIdentifier.getIdentifier(), idShortPath);
    }

    @Override
    public ResponseEntity<Void> patchSubmodelElementByPathValueOnlySubmodelRepo(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, SubmodelElementValue body, String level) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<SubmodelElement> postSubmodelElementByPathSubmodelRepo(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, SubmodelElement body, String level, String extent) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<SubmodelElement> postSubmodelElementSubmodelRepo(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, SubmodelElement body) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> deleteSubmodelElementByPathSubmodelRepo(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Resource> getFileByPath(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> putFileByPath(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, String fileName, MultipartFile file) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> deleteFileByPath(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<OperationResult> invokeOperationSubmodelRepo(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, OperationRequest body, Boolean async) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> putSubmodelElementByPathSubmodelRepo(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, SubmodelElement body, String level) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> patchSubmodelByIdValueOnly(Base64UrlEncodedIdentifier aasId, Base64UrlEncodedIdentifier submodelIdentifier, List<SubmodelElement> body, String level) {
        throw new MethodNotImplementedException();
    }

    private String getEncodedCursorFromCursorResult(CursorResult<?> cursorResult) {
        if (cursorResult == null || cursorResult.getCursor() == null) {
            return null;
        }

        return Base64UrlEncodedCursor.encodeCursor(cursorResult.getCursor());
    }

    private ResponseEntity<SubmodelElement> handleSubmodelElementValueNormalGetRequest(Base64UrlEncodedIdentifier aasId, String submodelIdentifier, String idShortPath) {
        SubmodelElement submodelElement = submodelRepositoryFactory.getSubmodelRepository(aasId.getIdentifier()).getSubmodelElement(submodelIdentifier, idShortPath);
        return new ResponseEntity<SubmodelElement>(submodelElement, HttpStatus.OK);
    }

    private ResponseEntity<SubmodelElementValue> handleSubmodelElementValueGetRequest(Base64UrlEncodedIdentifier aasId, String submodelIdentifier, String idShortPath) {
        SubmodelElementValue value = submodelRepositoryFactory.getSubmodelRepository(aasId.getIdentifier()).getSubmodelElementValue(submodelIdentifier, idShortPath);
        return new ResponseEntity<SubmodelElementValue>(value, HttpStatus.OK);
    }
}
