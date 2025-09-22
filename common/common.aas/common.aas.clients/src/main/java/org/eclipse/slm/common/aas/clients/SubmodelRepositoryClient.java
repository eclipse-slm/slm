package org.eclipse.slm.common.aas.clients;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.internal.SubmodelRepositoryApi;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelValueOnly;
import org.eclipse.digitaltwin.basyx.submodelservice.value.exception.ValueMapperNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.regex.Pattern;

public class SubmodelRepositoryClient {

    private static final Logger LOG = LoggerFactory.getLogger(SubmodelRepositoryClient.class);

    private final String submodelRepositoryUrl;

    private final ConnectedSubmodelRepository connectedSubmodelRepository;

    public SubmodelRepositoryClient(String submodelRepositoryUrl) {
        this(submodelRepositoryUrl, null);
    }

    public SubmodelRepositoryClient(String submodelRepositoryUrl, JwtAuthenticationToken jwtAuthenticationToken) {
        this.submodelRepositoryUrl = submodelRepositoryUrl;
        var apiClient = ClientUtils.getApiClient(submodelRepositoryUrl, jwtAuthenticationToken);
        var submodelRepositoryApi = new SubmodelRepositoryApi(apiClient);

        this.connectedSubmodelRepository = new ConnectedSubmodelRepository(submodelRepositoryUrl, submodelRepositoryApi);
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

    public Submodel getSubmodel(String submodelId) {
        try {
            var submodel = this.connectedSubmodelRepository.getSubmodel(submodelId);
            return submodel;
        } catch (Exception e) {
            LOG.error("Error while fetching submodel with id '{}': {}", submodelId, e);
            return null;
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

    public String getSubmodelRepositoryUrl() {
        return submodelRepositoryUrl;
    }
}
