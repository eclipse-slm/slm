package org.eclipse.slm.common.aas.repositories.shells;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.exceptions.MissingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationSupport;
import org.eclipse.slm.common.aas.repositories.exceptions.MethodNotImplementedException;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractAasRepository implements AasRepository {

    protected final AasFactory aasFactory;

    protected AbstractAasRepository(AasFactory aasFactory) {
        this.aasFactory = aasFactory;
    }

    protected TreeMap<String, Reference> convertToTreeMap(List<Reference> submodelReferences,
                                                        Function<Reference, String> idResolver) {
        return submodelReferences.stream().collect(Collectors
                .toMap(reference -> idResolver.apply(reference), ref -> ref, (ref1, ref2) -> ref1, TreeMap::new));
    }

    protected Function<Reference, String> extractSubmodelID() {
        return reference -> {
            List<Key> keys = reference.getKeys();
            for (Key key : keys) {
                if (key.getType() == KeyTypes.SUBMODEL) {
                    return key.getValue();
                }
            }
            return ""; // Return an empty string if no ID is found
        };
    }

    @Override
    public abstract CursorResult<List<AssetAdministrationShell>> getAllAas(List<SpecificAssetId> assetIds, String idShort, PaginationInfo pInfo);

    @Override
    public abstract AssetAdministrationShell getAas(String aasId) throws ElementDoesNotExistException;

    @Override
    public void createAas(AssetAdministrationShell aas) throws CollidingIdentifierException, MissingIdentifierException {
        throw new MethodNotImplementedException();
    }

    @Override
    public void deleteAas(String aasId) {
        throw new MethodNotImplementedException();
    }

    @Override
    public void updateAas(String aasId, AssetAdministrationShell aas) {
        throw new MethodNotImplementedException();
    }

    @Override
    public CursorResult<List<Reference>> getSubmodelReferences(String aasId, PaginationInfo pInfo) {
        var aas = this.getAas(aasId);
        var submodelReferences = aas.getSubmodels();

        Function<Reference, String> idResolver = extractSubmodelID();
        TreeMap<String, Reference> submodelRefMap = convertToTreeMap(submodelReferences, idResolver);

        PaginationSupport<Reference> paginationSupport = new PaginationSupport<>(submodelRefMap, idResolver);
        CursorResult<List<Reference>> paginatedSubmodelReference = paginationSupport.getPaged(pInfo);

        return paginatedSubmodelReference;
    }

    @Override
    public void addSubmodelReference(String aasId, Reference submodelReference) {
        throw new MethodNotImplementedException();
    }

    @Override
    public void removeSubmodelReference(String aasId, String submodelId) {
        throw new MethodNotImplementedException();
    }

    @Override
    public void setAssetInformation(String aasId, AssetInformation aasInfo) throws ElementDoesNotExistException {
        throw new MethodNotImplementedException();
    }

    @Override
    public abstract AssetInformation getAssetInformation(String aasId) throws ElementDoesNotExistException;

    @Override
    public File getThumbnail(String aasId) {
        throw new MethodNotImplementedException();
    }

    @Override
    public void setThumbnail(String aasId, String fileName, String contentType, InputStream inputStream) {
        throw new MethodNotImplementedException();
    }

    @Override
    public void deleteThumbnail(String aasId) {
        throw new MethodNotImplementedException();
    }

    @Override
    public String getName() {
        return AasRepository.super.getName();
    }
}
