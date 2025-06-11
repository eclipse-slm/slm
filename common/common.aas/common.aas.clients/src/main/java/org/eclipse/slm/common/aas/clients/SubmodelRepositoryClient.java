package org.eclipse.slm.common.aas.clients;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonMapperFactory;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.SimpleAbstractTypeResolverFactory;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.client.internal.ApiClient;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.internal.SubmodelRepositoryApi;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelValueOnly;
import org.eclipse.digitaltwin.basyx.submodelservice.value.exception.ValueMapperNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class SubmodelRepositoryClient {

    private static final Logger LOG = LoggerFactory.getLogger(SubmodelRepositoryClient.class);

    private String submodelRepositoryUrl;

    private final DiscoveryClient discoveryClient;

    private final String submodelRepositoryDiscoveryInstanceId = "submodel-repository";

    private final ConnectedSubmodelRepository connectedSubmodelRepository;

    @Autowired
    public SubmodelRepositoryClient(@Value("${aas.submodel-repository.url}") String submodelRepositoryUrl,
                                    DiscoveryClient discoveryClient) {
        this.submodelRepositoryUrl = submodelRepositoryUrl;
        this.connectedSubmodelRepository = this.getConnectedSubmodelRepository(submodelRepositoryUrl, null);
        this.discoveryClient = discoveryClient;

        var submodelRepositoryServiceInstance = this.discoveryClient.getInstances(submodelRepositoryDiscoveryInstanceId).get(0);
        if (submodelRepositoryServiceInstance != null) {
            this.submodelRepositoryUrl = "http://" + submodelRepositoryServiceInstance.getHost()
                    + ":" + submodelRepositoryServiceInstance.getPort();
        } else {
            LOG.warn("No service instance '" + submodelRepositoryDiscoveryInstanceId + "' found via discovery client. Using default URL from application.yml.");
        }
    }

    public SubmodelRepositoryClient(String submodelRepositoryUrl, JwtAuthenticationToken jwtAuthenticationToken) {
        this.submodelRepositoryUrl = submodelRepositoryUrl;
        this.connectedSubmodelRepository = this.getConnectedSubmodelRepository(submodelRepositoryUrl, jwtAuthenticationToken);
        this.discoveryClient = null;
    }

    public static SubmodelRepositoryClient FromSubmodelDescriptor(SubmodelDescriptor submodelDescriptor, JwtAuthenticationToken jwtAuthenticationToken) {
        var submodelEndpoint = submodelDescriptor.getEndpoints().get(0).getProtocolInformation().getHref();

        if (submodelEndpoint.contains("/submodels/")) {
            var regExPattern = Pattern.compile("(.*)/submodels");
            var matcher = regExPattern.matcher(submodelEndpoint);
            var matchesFound = matcher.find();
            if (matchesFound) {
                var submodelRepositoryBaseUrl = matcher.group(1);
                var submodelRepositoryClient = new SubmodelRepositoryClient(submodelRepositoryBaseUrl, jwtAuthenticationToken);

                return submodelRepositoryClient;
            }
        }

        throw new IllegalArgumentException("Submodel endpoint '" + submodelEndpoint + "' not valid for submodel repository");
    }

    private ConnectedSubmodelRepository getConnectedSubmodelRepository(String submodelRepositoryUrl,
                                                                       JwtAuthenticationToken jwtAuthenticationToken) {

            var apiClient = ClientUtils.getApiClient(submodelRepositoryUrl, jwtAuthenticationToken);
            var submodelRepositoryApi = new SubmodelRepositoryApi(apiClient);

            var newConnectedSubmodelRepository = new ConnectedSubmodelRepository(submodelRepositoryUrl, submodelRepositoryApi);

            return newConnectedSubmodelRepository;
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
