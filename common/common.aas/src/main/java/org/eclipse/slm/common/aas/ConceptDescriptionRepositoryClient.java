package org.eclipse.slm.common.aas;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
public class ConceptDescriptionRepositoryClient {

    private static final Logger LOG = LoggerFactory.getLogger(ConceptDescriptionRepositoryClient.class);

    private final String conceptDescriptionRepositoryUrl;

    public ConceptDescriptionRepositoryClient(@Value("${aas.concept-descriptions-repository.url}") String conceptDescriptionRepositoryUrl) {
        this.conceptDescriptionRepositoryUrl = conceptDescriptionRepositoryUrl;
    }

    public void createOrUpdateConceptDescription(ConceptDescription conceptDescription) {
        var webClient = WebClient.create();

        var jsonSerializer = new JsonSerializer();
        try {
            var serializedConceptDescription = jsonSerializer.write(conceptDescription);
            try {
                var responseBody = webClient.post()
                        .uri(conceptDescriptionRepositoryUrl + "/concept-descriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(serializedConceptDescription))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }
            catch (WebClientResponseException.Conflict e) {
                var conceptDescriptionIdEncoded = Base64UrlEncodedIdentifier.encodeIdentifier(conceptDescription.getId());
                var responseBody = webClient.put()
                        .uri(conceptDescriptionRepositoryUrl + "/concept-descriptions/" + conceptDescriptionIdEncoded)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(serializedConceptDescription))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }
        }
        catch (SerializationException e) {
            throw new RuntimeException(e);
        }

    }

}
