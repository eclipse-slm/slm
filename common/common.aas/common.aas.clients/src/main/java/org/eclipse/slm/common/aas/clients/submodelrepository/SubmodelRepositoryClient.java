package org.eclipse.slm.common.aas.clients.submodelrepository;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.internal.SubmodelRepositoryApi;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelValueOnly;
import org.eclipse.digitaltwin.basyx.submodelservice.value.exception.ValueMapperNotFoundException;
import org.eclipse.slm.common.aas.clients.auth.AuthRequestInterceptor;
import org.eclipse.slm.common.aas.clients.base.FeignClientFactory;
import org.eclipse.slm.common.aas.model.shellrepository.exceptions.SubmodelRuntimeException;
import org.eclipse.slm.common.aas.clients.utils.ClientUtils;
import org.eclipse.slm.common.aas.model.submodelrepository.responses.SubmodelQueryResult;
import org.eclipse.slm.common.aas.repositories.exceptions.SubmodelNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SubmodelRepositoryClient {

    private static final Logger LOG = LoggerFactory.getLogger(SubmodelRepositoryClient.class);

    private final String submodelRepositoryUrl;

    private final ConnectedSubmodelRepository connectedSubmodelRepository;

    private final SubmodelRepositoryQueryApiClient submodelRepositoryQueryApiClient;

    public SubmodelRepositoryClient(String submodelRepositoryUrl) {
        this(submodelRepositoryUrl, null);
    }

    public SubmodelRepositoryClient(String submodelRepositoryUrl, AuthRequestInterceptor authRequestInterceptor) {
        this.submodelRepositoryUrl = submodelRepositoryUrl;
        var apiClient = ClientUtils.getApiClient(submodelRepositoryUrl, authRequestInterceptor);
        var submodelRepositoryApi = new SubmodelRepositoryApi(apiClient);

        this.connectedSubmodelRepository = new ConnectedSubmodelRepository(submodelRepositoryUrl, submodelRepositoryApi);

        this.submodelRepositoryQueryApiClient = FeignClientFactory.createClient(SubmodelRepositoryQueryApiClient.class, submodelRepositoryUrl, authRequestInterceptor);
    }

    public List<Submodel> getAllSubmodels() throws DeserializationException {
        WebClient webClient = WebClient.create();
        var responseBody = webClient.get()
                .uri(this.submodelRepositoryUrl + "/submodels")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        var jsonDeserializer = new JsonDeserializer();
        var submodels = jsonDeserializer.readList(responseBody.get("result"), Submodel.class);

        return submodels;
    }

    public Optional<Submodel> getSubmodel(String submodelId) {
        try {
            var submodel = this.getSubmodelOrThrow(submodelId);
            return Optional.of(submodel);
        } catch (SubmodelNotFoundException e) {
            // Expected exception, nothing to do
        } catch (Exception e) {
            LOG.debug("Error while fetching submodel with id '" + submodelId + "': " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    public Submodel getSubmodelOrThrow(String submodelId) throws SubmodelNotFoundException, SubmodelRuntimeException {
        try {
            var submodel = this.connectedSubmodelRepository.getSubmodel(submodelId);
            return submodel;
        } catch (ElementDoesNotExistException e) {
            throw new SubmodelNotFoundException("Submodel with id '" + submodelId + "' not found.");
        }
        catch (Exception e) {
            throw new SubmodelRuntimeException("Error while fetching submodel with id '" + submodelId + "': " + e.getMessage());
        }
    }

    public SubmodelValueOnly getSubmodelValueOnly(String submodelId) {
        try {
            var submodelValueOnly = this.connectedSubmodelRepository.getSubmodelByIdValueOnly(submodelId);

            return submodelValueOnly;
        }
        catch (ValueMapperNotFoundException e) {
            LOG.error("Value mapper not found for submodel with id: " + submodelId);
        }

        return null;
    }

    public void createOrUpdateSubmodel(Submodel submodel) {
        try {
            this.connectedSubmodelRepository.createSubmodel(submodel);
        } catch (CollidingIdentifierException e) {
            this.connectedSubmodelRepository.updateSubmodel(submodel.getId(), submodel);
        }
        catch (RuntimeException e) {
            LOG.error(e.getMessage());
        }
    }

    public void deleteSubmodel(String submodelId) {
        this.connectedSubmodelRepository.deleteSubmodel(submodelId);
    }

    public SubmodelElement getSubmodelElement(String submodelId, String smeIdShort) {
        var submodelElement = this.connectedSubmodelRepository.getSubmodelElement(submodelId, smeIdShort);
        return submodelElement;
    }

    public void createSubmodelElement(String submodelId, SubmodelElement submodelElement) {
        this.connectedSubmodelRepository.createSubmodelElement(submodelId, submodelElement);
    }

    public void updateSubmodelElement(String submodelId, String idShortPath, SubmodelElement submodelElement) {
        this.connectedSubmodelRepository.updateSubmodelElement(submodelId, idShortPath, submodelElement);
    }

    public void createOrUpdateSubmodelElement(String submodelId, String idShortPath, SubmodelElement submodelElement) {
        try {
            this.connectedSubmodelRepository.updateSubmodelElement(submodelId, idShortPath, submodelElement);
        } catch (ElementDoesNotExistException e) {
            this.connectedSubmodelRepository.createSubmodelElement(submodelId, idShortPath, submodelElement);
        }
    }

    public SubmodelQueryResult querySubmodel(int limit, String cursor, Map<String, Object> query) {
        return this.submodelRepositoryQueryApiClient.querySubmodel(limit, cursor, query);
    }

    public String getSubmodelRepositoryUrl() {
        return submodelRepositoryUrl;
    }


}
