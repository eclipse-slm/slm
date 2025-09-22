package org.eclipse.slm.common.aas.repositories.shells;

import jakarta.validation.Valid;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationSupport;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.slm.common.aas.repositories.exceptions.MethodNotImplementedException;
import org.eclipse.slm.common.aas.repositories.submodels.AbstractSubmodelRepository;
import org.eclipse.slm.common.aas.repositories.submodels.SubmodelRepositoryFactory;
import org.eclipse.slm.common.aas.repositories.submodels.SubmodelServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractAasService extends AbstractSubmodelRepository {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractAasService.class);

    protected final AasFactory aasFactory;

    protected final String aasId;

    protected AbstractAasService(String aasId,
                                 AasFactory aasFactory,
                                 Map<String, SubmodelServiceFactory> submodelServiceFactories,
                                 Map<String, SubmodelRepositoryFactory> submodelRepositoryFactories) {
        super(aasId);
        this.aasId = aasId;
        this.aasFactory = aasFactory;
        this.setSubmodelServiceFactories(submodelServiceFactories);
        this.setSubmodelRepositoryFactories(submodelRepositoryFactories);
    }

    public AssetAdministrationShell getAas() {
        var aas = this.aasFactory.createAas(this.aasId);
        return aas;
    }

    public AssetInformation getAssetInformation() {
        var assetInformation = this.getAas().getAssetInformation();
        return assetInformation;
    }

    public CursorResult<List<Reference>> getSubmodelReferences(PaginationInfo pInfo) {
        var aas = this.getAas();
        var submodelReferences = aas.getSubmodels();

        Function<Reference, String> idResolver = extractSubmodelID();
        TreeMap<String, Reference> submodelRefMap = convertToTreeMap(submodelReferences, idResolver);

        PaginationSupport<Reference> paginationSupport = new PaginationSupport<>(submodelRefMap, idResolver);
        CursorResult<List<Reference>> paginatedSubmodelReference = paginationSupport.getPaged(pInfo);

        return paginatedSubmodelReference;
    }

    public Reference postSubmodelReferenceAas(@Valid Reference body) {
        throw new MethodNotImplementedException();
    }

    public void deleteSubmodelReferenceByIdAas(Base64UrlEncodedIdentifier submodelIdentifier) {
        throw new MethodNotImplementedException();
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
}
