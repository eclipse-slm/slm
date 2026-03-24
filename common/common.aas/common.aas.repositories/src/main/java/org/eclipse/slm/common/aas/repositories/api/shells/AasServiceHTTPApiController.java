package org.eclipse.slm.common.aas.repositories.api.shells;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.digitaltwin.basyx.http.pagination.Base64UrlEncodedCursor;
import org.eclipse.digitaltwin.basyx.http.pagination.PagedResult;
import org.eclipse.digitaltwin.basyx.http.pagination.PagedResultPagingMetadata;
import org.eclipse.digitaltwin.basyx.pagination.GetSubmodelElementsResult;
import org.eclipse.digitaltwin.basyx.submodelrepository.http.pagination.GetSubmodelsResult;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.slm.common.aas.repositories.api.submodels.GetSubmodelsValueOnlyResult;
import org.eclipse.slm.common.aas.repositories.api.submodels.SubmodelValueOnly;
import org.eclipse.slm.common.aas.repositories.exceptions.MethodNotImplementedException;
import org.eclipse.slm.common.aas.repositories.shells.AbstractAasService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public abstract class AasServiceHTTPApiController implements AasServiceHTTPApi {

    private final AbstractAasService aasService;

    protected AasServiceHTTPApiController(AbstractAasService aasService) {
        this.aasService = aasService;
    }

    @Override
    public ResponseEntity<AssetAdministrationShell> getAssetAdministrationShell() {
        var aas = aasService.getAas();

        return ResponseEntity.ok(aas);
    }

    @Override
    public ResponseEntity<AssetInformation> getAssetInformation() {
        var assetInformation = aasService.getAssetInformation();

        return ResponseEntity.ok(assetInformation);
    }

    @Override
    public ResponseEntity<PagedResult> getSubmodelReferences(Integer limit, Base64UrlEncodedCursor cursor) {
        if (limit == null) {
            limit = 100;
        }

        String decodedCursor = "";
        if (cursor != null) {
            decodedCursor = cursor.getDecodedCursor();
        }

        PaginationInfo paginationInfo = new PaginationInfo(limit, decodedCursor);
        var submodelReferences = aasService.getSubmodelReferences(paginationInfo);

        GetReferencesResult result = new GetReferencesResult();

        String encodedCursor = getEncodedCursorFromCursorResult(submodelReferences);

        result.setResult(submodelReferences.getResult());
        result.setPagingMetadata(new PagedResultPagingMetadata().cursor(encodedCursor));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Reference> postSubmodelReferenceAas(Reference body) {
        var reference = this.aasService.postSubmodelReferenceAas(body);

        return ResponseEntity.ok(reference);
    }

    @Override
    public ResponseEntity<Void> deleteSubmodelReferenceByIdAas(Base64UrlEncodedIdentifier submodelIdentifier) {
        this.aasService.deleteSubmodelReferenceByIdAas(submodelIdentifier);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PagedResult> getAllSubmodels(Base64UrlEncodedIdentifier semanticId, String idShort, Integer limit, Base64UrlEncodedCursor cursor, String level, String extent) {
        if (limit == null) {
            limit = 100;
        }

        String decodedCursor = "";
        if (cursor != null) {
            decodedCursor = cursor.getDecodedCursor();
        }

        PaginationInfo pInfo = new PaginationInfo(limit, decodedCursor);

        CursorResult<List<Submodel>> cursorResult = this.aasService.getAllSubmodels(pInfo);

        GetSubmodelsResult paginatedSubmodel = new GetSubmodelsResult();

        String encodedCursor = getEncodedCursorFromCursorResult(cursorResult);

        paginatedSubmodel.result(new ArrayList<>(cursorResult.getResult()));
        paginatedSubmodel.setPagingMetadata(new PagedResultPagingMetadata().cursor(encodedCursor));

        return new ResponseEntity<>(paginatedSubmodel, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetSubmodelsValueOnlyResult> getAllSubmodelsValueOnly(Base64UrlEncodedIdentifier semanticId, String idShort, Integer limit, Base64UrlEncodedCursor cursor, String level, String extent) {
        if (limit == null) {
            limit = 100;
        }

        String decodedCursor = "";
        if (cursor != null) {
            decodedCursor = cursor.getDecodedCursor();
        }

        PaginationInfo pInfo = new PaginationInfo(limit, decodedCursor);

        var submodelValuesOnlyMap = this.aasService.getAllSubmodelsValueOnly(pInfo);

        return new ResponseEntity<>(submodelValuesOnlyMap, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Submodel> getSubmodelById(Base64UrlEncodedIdentifier submodelIdentifier, String level, String extent) {
        return new ResponseEntity<Submodel>(this.aasService.getSubmodel(submodelIdentifier.getIdentifier()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SubmodelValueOnly> getSubmodelByIdValueOnly(Base64UrlEncodedIdentifier submodelIdentifier, String level, String extent) {
        return new ResponseEntity<SubmodelValueOnly>(this.aasService.getSubmodelByIdValueOnly(submodelIdentifier.getIdentifier()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Submodel> getSubmodelByIdMetadata(Base64UrlEncodedIdentifier submodelIdentifier, String level) {
        return new ResponseEntity<Submodel>(this.aasService.getSubmodelByIdMetadata(submodelIdentifier.getIdentifier()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PagedResult> getAllSubmodelElements(Base64UrlEncodedIdentifier submodelIdentifier, Integer limit, Base64UrlEncodedCursor cursor, String level, String extent) {
        if (limit == null) {
            limit = 100;
        }

        String decodedCursor = "";
        if (cursor != null) {
            decodedCursor = cursor.getDecodedCursor();
        }

        PaginationInfo pInfo = new PaginationInfo(limit, decodedCursor);
        CursorResult<List<SubmodelElement>> cursorResult = this.aasService.getSubmodelElements(submodelIdentifier.getIdentifier(), pInfo);

        GetSubmodelElementsResult paginatedSubmodelElement = new GetSubmodelElementsResult();
        String encodedCursor = getEncodedCursorFromCursorResult(cursorResult);

        paginatedSubmodelElement.result(new ArrayList<>(cursorResult.getResult()));
        paginatedSubmodelElement.setPagingMetadata(new PagedResultPagingMetadata().cursor(encodedCursor));

        return new ResponseEntity<PagedResult>(paginatedSubmodelElement, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SubmodelElement> getSubmodelElementByPathSubmodelRepo(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, String level, String extent) {
        return handleSubmodelElementValueNormalGetRequest(submodelIdentifier.getIdentifier(), idShortPath);
    }

    @Override
    public ResponseEntity<SubmodelElementValue> getSubmodelElementByPathValueOnlySubmodelRepo(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, String level, String extent) {
        return handleSubmodelElementValueGetRequest(submodelIdentifier.getIdentifier(), idShortPath);
    }

    @Override
    public ResponseEntity<Resource> getFileByPath(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath) {
        throw new MethodNotImplementedException();
    }

    protected String getEncodedCursorFromCursorResult(CursorResult<?> cursorResult) {
        if (cursorResult == null || cursorResult.getCursor() == null) {
            return null;
        }

        return Base64UrlEncodedCursor.encodeCursor(cursorResult.getCursor());
    }

    protected ResponseEntity<SubmodelElement> handleSubmodelElementValueNormalGetRequest(String submodelIdentifier, String idShortPath) {
        SubmodelElement submodelElement = this.aasService.getSubmodelElement(submodelIdentifier, idShortPath);
        return new ResponseEntity<SubmodelElement>(submodelElement, HttpStatus.OK);
    }

    protected ResponseEntity<SubmodelElementValue> handleSubmodelElementValueGetRequest(String submodelIdentifier, String idShortPath) {
        SubmodelElementValue value = this.aasService.getSubmodelElementValue(submodelIdentifier, idShortPath);
        return new ResponseEntity<SubmodelElementValue>(value, HttpStatus.OK);
    }
}
