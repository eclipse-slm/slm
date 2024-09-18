package org.eclipse.slm.common.aas.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.basyx.aasregistry.client.ApiClient;
import org.eclipse.digitaltwin.basyx.aasregistry.client.ApiException;
import org.eclipse.digitaltwin.basyx.aasregistry.client.api.RegistryAndDiscoveryInterfaceApi;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.model.SubmodelDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AasRegistryClient {

    private static final Logger LOG = LoggerFactory.getLogger(AasRegistryClient.class);

    private final String aasRegistryUrl;

    private final String aasRepositoryUrl;

    private RegistryAndDiscoveryInterfaceApi aasRegistryApi;

    private final ObjectMapper objectMapper;

    public AasRegistryClient(@Value("${aas.aas-registry.url}") String aasRegistryUrl,
                             @Value("${aas.aas-repository.url}") String aasRepositoryUrl, ObjectMapper objectMapper) {
        this.aasRegistryUrl = aasRegistryUrl;
        this.aasRepositoryUrl = aasRepositoryUrl;
        this.objectMapper = objectMapper;
        var aasRegistryClient = new ApiClient(HttpClient.newBuilder(), objectMapper, this.aasRegistryUrl);
        this.aasRegistryApi = new RegistryAndDiscoveryInterfaceApi(aasRegistryClient);
    }

    public List<AssetAdministrationShellDescriptor> getAllShellDescriptors() {
        List<AssetAdministrationShellDescriptor> aasDescriptors = new ArrayList<>();
        try {
            var result = this.aasRegistryApi.getAllAssetAdministrationShellDescriptors(Integer.MAX_VALUE, null, null, null);
            aasDescriptors = result.getResult().stream()
                    .map(AasRegistryClient::convertAasDescriptor)
                    .collect(Collectors.toList());

            return aasDescriptors;
        } catch (ApiException e) {
            if (e.getCode() != 404) {
                LOG.error(e.getMessage());
            }
            return aasDescriptors;
        }
    }

    public Optional<AssetAdministrationShellDescriptor> getAasDescriptor(String aasId) throws ApiException {
        try {
            var result = this.aasRegistryApi.getAssetAdministrationShellDescriptorByIdWithHttpInfo(aasId);
            var convertedAasDescriptor = AasRegistryClient.convertAasDescriptor(result.getData());
            return Optional.of(convertedAasDescriptor);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }

    public void addSubmodelDescriptorToAas(String aasId, SubmodelDescriptor submodelDescriptor) throws ApiException {
        var endpoints = new ArrayList<org.eclipse.digitaltwin.basyx.aasregistry.client.model.Endpoint>();
        var endpoint = new org.eclipse.digitaltwin.basyx.aasregistry.client.model.Endpoint();
        endpoint.setInterface("SUBMODEL-3.0");
        var protocolInformation = new org.eclipse.digitaltwin.basyx.aasregistry.client.model.ProtocolInformation();
        protocolInformation.setEndpointProtocol("http");
        protocolInformation.setHref(submodelDescriptor.getEndpoints().get(0).getProtocolInformation().getHref());
        endpoint.setProtocolInformation(protocolInformation);
        endpoints.add(endpoint);

        var convertedSubmodelDescriptor = new org.eclipse.digitaltwin.basyx.aasregistry.client.model.SubmodelDescriptor();
        convertedSubmodelDescriptor.setId(submodelDescriptor.getId());
        convertedSubmodelDescriptor.setIdShort(submodelDescriptor.getIdShort());
        convertedSubmodelDescriptor.setEndpoints(endpoints);

        this.addSubmodelDescriptorToAas(aasId, convertedSubmodelDescriptor);
    }

    public void addSubmodelDescriptorToAas(String aasId, org.eclipse.digitaltwin.basyx.aasregistry.client.model.SubmodelDescriptor submodelDescriptor) throws ApiException {
        try {
            this.aasRegistryApi.postSubmodelDescriptorThroughSuperpathWithHttpInfo(aasId, submodelDescriptor);
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                this.aasRegistryApi.putSubmodelDescriptorByIdThroughSuperpath(aasId, submodelDescriptor.getId(), submodelDescriptor);
            }
            else {
                throw new RuntimeException(e);
            }
        }
    }

    public static org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor convertAasDescriptor(
            org.eclipse.digitaltwin.basyx.aasregistry.client.model.AssetAdministrationShellDescriptor aasDescriptor) {
        try {
            var aasJsonSerializer = new JsonSerializer();
            var aasJsonDeserializer = new JsonDeserializer();

            var registryModelJson = aasJsonSerializer.write(aasDescriptor);
            var convertedAasDescriptor = aasJsonDeserializer.read(registryModelJson, DefaultAssetAdministrationShellDescriptor.class);

            return convertedAasDescriptor;
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (DeserializationException e) {
            throw new RuntimeException(e);
        }
    }
}
