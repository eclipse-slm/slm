package org.eclipse.slm.common.aas.repositories.submodels;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.exceptions.*;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationSupport;
import org.eclipse.digitaltwin.basyx.submodelservice.SubmodelService;
import org.eclipse.digitaltwin.basyx.submodelservice.pathparsing.HierarchicalSubmodelElementParser;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.digitaltwin.basyx.submodelservice.value.factory.SubmodelElementValueMapperFactory;
import org.eclipse.slm.common.aas.repositories.api.submodels.GetSubmodelsValueOnlyResult;
import org.eclipse.slm.common.aas.repositories.api.submodels.SubmodelValueOnly;
import org.eclipse.slm.common.aas.repositories.exceptions.MethodNotImplementedException;
import org.eclipse.slm.common.aas.repositories.exceptions.SubmodelNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractSubmodelRepository implements SubmodelRepository {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractSubmodelRepository.class);

    protected static final PaginationInfo NO_LIMIT_PAGINATION_INFO = new PaginationInfo(0, null);

    protected String aasId;

    private Map<String, SubmodelServiceFactory> submodelServiceFactories = new HashMap<>();

    private Map<String, SubmodelRepositoryFactory> submodelRepositoryFactories = new HashMap<>();

    public AbstractSubmodelRepository(String aasId) {
        this.aasId = aasId;
    }

    public Map<String, SubmodelServiceFactory> getSubmodelServiceFactories() {
        return submodelServiceFactories;
    }

    public void setSubmodelServiceFactories(Map<String, SubmodelServiceFactory> submodelServiceFactories) {
        this.submodelServiceFactories = submodelServiceFactories;
    }

    public Map<String, SubmodelRepositoryFactory> getSubmodelRepositoryFactories() {
        return submodelRepositoryFactories;
    }

    public void setSubmodelRepositoryFactories(Map<String, SubmodelRepositoryFactory> submodelRepositoryFactories) {
        this.submodelRepositoryFactories = submodelRepositoryFactories;
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
        for (var submodelRepositoryFactory : submodelRepositoryFactories.values()) {
            var submodelsOfRepoResult = submodelRepositoryFactory.getSubmodelRepository(this.aasId).getAllSubmodels(pInfo);
            submodels.addAll(submodelsOfRepoResult.getResult());
        }
        for (var submodelServiceFactory : submodelServiceFactories.values()) {
            var submodel = submodelServiceFactory.getSubmodelService(this.aasId).getSubmodel();
            submodels.add(submodel);
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
    public GetSubmodelsValueOnlyResult getAllSubmodelsValueOnly(PaginationInfo pInfo) {
        var submodelsValueOnly = new GetSubmodelsValueOnlyResult();

        var allSubmodels = this.getAllSubmodels(pInfo);

        for (var submodel : allSubmodels.getResult()) {
            if (submodel != null) {
                if (submodel.getId() != null) {
                    try {
                        var submodelValueOnly = this.getSubmodelByIdValueOnly(submodel.getId());
                        submodelValueOnly.setIdShort(submodel.getIdShort());

                        submodelsValueOnly.put(submodel.getIdShort(), submodelValueOnly);
                    } catch (SubmodelNotFoundException e) {
                        LOG.debug("Submodel with ID {} not found in repository for AAS {}", submodel.getId(), this.aasId);
                    }
                }
            }
        }

        return submodelsValueOnly;
    }

    @Override
    public Submodel getSubmodel(String submodelId) throws ElementDoesNotExistException {
        var optionalSubmodelService = this.getSubmodelServiceBySubmodelId(submodelId);
        if (optionalSubmodelService.isPresent()) {
            var submodel = optionalSubmodelService.get().getSubmodel();
            return submodel;
        }

        var optionalSubmodelRepository = this.getSubmodelRepositoryBySubmodelId(submodelId);
        if (optionalSubmodelRepository.isPresent()) {
            var submodel = optionalSubmodelRepository.get().getSubmodel(submodelId);
            return submodel;
        }

        throw new SubmodelNotFoundException(aasId, submodelId);
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
        var optionalSubmodelService = this.getSubmodelServiceBySubmodelId(submodelId);
        if (optionalSubmodelService.isPresent()) {
            var submodelElementsResult = optionalSubmodelService.get().getSubmodelElements(pInfo);
            return submodelElementsResult;
        }

        var optionalSubmodelRepository = this.getSubmodelRepositoryBySubmodelId(submodelId);
        if (optionalSubmodelRepository.isPresent()) {
            var submodelElementsResult = optionalSubmodelRepository.get().getSubmodelElements(submodelId, pInfo);
            return submodelElementsResult;
        }

        throw new SubmodelNotFoundException(aasId, submodelId);
    }

    @Override
    public SubmodelElement getSubmodelElement(String submodelId, String smeIdShort) throws ElementDoesNotExistException {
        var optionalSubmodelService = this.getSubmodelServiceBySubmodelId(submodelId);
        if (optionalSubmodelService.isPresent()) {
            var submodelElementResult = optionalSubmodelService.get().getSubmodelElement(smeIdShort);
            return submodelElementResult;
        }

        var optionalSubmodelRepository = this.getSubmodelRepositoryBySubmodelId(submodelId);
        if (optionalSubmodelRepository.isPresent()) {
            var submodelElementResult = optionalSubmodelRepository.get().getSubmodelElement(submodelId, smeIdShort);
            return submodelElementResult;
        }

        throw new SubmodelNotFoundException(aasId, submodelId);
    }

    @Override
    public SubmodelElementValue getSubmodelElementValue(String submodelId, String smeIdShort) throws ElementDoesNotExistException {
        var optionalSubmodelService = this.getSubmodelServiceBySubmodelId(submodelId);
        if (optionalSubmodelService.isPresent()) {
            var submodelElementvalueResult = optionalSubmodelService.get().getSubmodelElementValue(smeIdShort);
            return submodelElementvalueResult;
        }

        var optionalSubmodelRepository = this.getSubmodelRepositoryBySubmodelId(submodelId);
        if (optionalSubmodelRepository.isPresent()) {
            var submodelElementvalueResult = optionalSubmodelRepository.get().getSubmodelElementValue(submodelId, smeIdShort);
            return submodelElementvalueResult;
        }

        throw new SubmodelNotFoundException(aasId, submodelId);
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

    private Optional<SubmodelServiceFactory> getSubmodelServiceFactoryBySubmodelId(String submodelId) {
        var prefixSplit = submodelId.split("-");
        if (prefixSplit.length > 1) {
            var submodelPrefix = prefixSplit[0];

            if ( this.submodelServiceFactories.containsKey(submodelPrefix)) {
                return Optional.of(this.submodelServiceFactories.get(submodelPrefix));
            }
            else {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private Optional<SubmodelService> getSubmodelServiceBySubmodelId(String submodelId) {
        var submodelServiceFactoryOptional = this.getSubmodelServiceFactoryBySubmodelId(submodelId);
        if (submodelServiceFactoryOptional.isPresent()) {
            return Optional.of(submodelServiceFactoryOptional.get().getSubmodelService(this.aasId));
        } else {
            return Optional.empty();
        }
    }

    private Optional<SubmodelRepository> getSubmodelRepositoryBySubmodelId(String submodelId) {
        var prefixSplit = submodelId.split("-");
        if (prefixSplit.length > 1) {
            var submodelPrefix = prefixSplit[0];

            if (this.submodelRepositoryFactories.containsKey(submodelPrefix)) {
                return Optional.of(this.submodelRepositoryFactories.get(submodelPrefix).getSubmodelRepository(this.aasId));
            }
            else {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    protected CursorResult<List<SubmodelElement>> generateCursorResult(List<SubmodelElement> submodelElements, PaginationInfo pInfo) {

        TreeMap<String, SubmodelElement> submodelMap = submodelElements.stream().collect(Collectors.toMap(SubmodelElement::getIdShort, aas -> aas, (a, b) -> a, TreeMap::new));

        PaginationSupport<SubmodelElement> paginationSupport = new PaginationSupport<>(submodelMap, SubmodelElement::getIdShort);
        CursorResult<List<SubmodelElement>> paginatedSubmodels = paginationSupport.getPaged(pInfo);
        return paginatedSubmodels;
    }

    protected SubmodelElement getSubmodelElementForSubmodel(Submodel submodel, String idShortPath) {
        var parser = new HierarchicalSubmodelElementParser(submodel);

        return parser.getSubmodelElementFromIdShortPath(idShortPath);
    }

    protected SubmodelElementValue getSubmodelElementValueForSubmodel(SubmodelElement submodelElement) {
        SubmodelElementValueMapperFactory submodelElementValueFactory = new SubmodelElementValueMapperFactory();

        return submodelElementValueFactory.create(submodelElement).getValue();
    }
}
