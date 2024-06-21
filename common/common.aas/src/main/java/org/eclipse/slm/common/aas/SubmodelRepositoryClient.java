package org.eclipse.slm.common.aas;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class SubmodelRepositoryClient {

    private static final Logger LOG = LoggerFactory.getLogger(SubmodelRepositoryClient.class);

    private final String submodelRepositoryUrl;

    private final ConnectedSubmodelRepository connectedSubmodelRepository;

    public SubmodelRepositoryClient(@Value("${aas.submodel-repository.url}") String submodelRepositoryUrl) {
        this.submodelRepositoryUrl = submodelRepositoryUrl;
        this.connectedSubmodelRepository = new ConnectedSubmodelRepository(submodelRepositoryUrl);
    }

    public List<Submodel> getAllSubmodels() throws DeserializationException {
        WebClient webClient = WebClient.create();
        var responseBody = webClient.get()
                .uri(submodelRepositoryUrl + "/submodels")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        var jsonDeserializer = new JsonDeserializer();
        var submodels = jsonDeserializer.readList(responseBody.get("result"), Submodel.class);

        return submodels;
    }

    public Submodel getSubmodel(String submodelId) {
        var submodel = this.connectedSubmodelRepository.getSubmodel(submodelId);
        return submodel;
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

}
