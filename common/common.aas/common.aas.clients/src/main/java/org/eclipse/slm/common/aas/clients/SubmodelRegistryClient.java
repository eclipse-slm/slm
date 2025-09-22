package org.eclipse.slm.common.aas.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.api.SubmodelRegistryApi;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SubmodelRegistryClient {

    private static final Logger LOG = LoggerFactory.getLogger(SubmodelRegistryClient.class);

    private SubmodelRegistryApi submodelRegistryApi;

    public SubmodelRegistryClient(String submodelRegistryUrl) {
        var objectMapper = new ObjectMapper();
        var submodelRegistryApiClient = new org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiClient(HttpClient.newBuilder(), objectMapper, submodelRegistryUrl);
        this.submodelRegistryApi = new SubmodelRegistryApi(submodelRegistryApiClient);
    }

    public SubmodelRegistryClient(SubmodelRegistryApi submodelRegistryApi) {
        this.submodelRegistryApi = submodelRegistryApi;
    }

    public List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor> getAllSubmodelDescriptors() {
        List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor> submodelDescriptors = new ArrayList<>();
        try {
            var result = this.submodelRegistryApi.getAllSubmodelDescriptors(Integer.MAX_VALUE, null);
            submodelDescriptors = result.getResult().stream()
                    .map(SubmodelRegistryClient::convertSubmodelDescriptor)
                    .collect(Collectors.toList());

            return submodelDescriptors;
        } catch (ApiException e) {
            if (e.getCode() != 404) {
                LOG.error(e.getMessage());
            }
            return submodelDescriptors;
        }
    }

    public Optional<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor> findSubmodelDescriptor(String submodelId) {
        SubmodelDescriptor submodelDescriptor = null;
        try {
            submodelDescriptor = this.submodelRegistryApi.getSubmodelDescriptorById(submodelId);
            var convertedSubmodelDescriptor = SubmodelRegistryClient.convertSubmodelDescriptor(submodelDescriptor);
            return Optional.of(convertedSubmodelDescriptor);
        } catch (ApiException e) {
            if (e.getCode() != 404) {
                LOG.error(e.getMessage());
            }
            return Optional.empty();
        }
    }

    public List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor> findSubmodelDescriptorsWithSemanticIds(List<String> semanticIds) {
        List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor> submodelDescriptors = new ArrayList<>();
        try {
            var allSubmodelDescriptors = this.submodelRegistryApi.getAllSubmodelDescriptors(Integer.MAX_VALUE, null).getResult();
            var submodelDescriptorsWithSemanticId = allSubmodelDescriptors.stream()
                    .filter(smd -> {
                                if (smd.getSemanticId() != null) {
                                    return semanticIds.contains(smd.getSemanticId().getKeys().get(0).getValue());
                                } else {
                                    return false;
                                }
                            })
                    .toList();

            var convertedSubmodelDescriptors = submodelDescriptorsWithSemanticId.stream()
                    .map(SubmodelRegistryClient::convertSubmodelDescriptor)
                    .collect(Collectors.toList());

            return convertedSubmodelDescriptors;
        } catch (ApiException e) {
            if (e.getCode() != 404) {
                LOG.error(e.getMessage());
            }
            return submodelDescriptors;
        }
    }

    public void createOrUpdateSubmodelDescriptor(org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor submodelDescriptor) throws ApiException {
        var convertedSubmodelDescriptor = SubmodelRegistryClient.convertSubmodelDescriptor(submodelDescriptor);
        try {
            this.submodelRegistryApi.postSubmodelDescriptor(convertedSubmodelDescriptor);
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                this.submodelRegistryApi.putSubmodelDescriptorById(submodelDescriptor.getId(), convertedSubmodelDescriptor);
            } else {
                throw e;
            }
        }
    }

    public void registerSubmodel(String submodelUrl, String smId, String smIdShort, String semanticId) throws ApiException {
        var endpoints = new ArrayList<Endpoint>();
        var endpoint = new Endpoint();
        endpoint.setInterface("SUBMODEL-3.0");
        var protocolInformation = new org.eclipse.digitaltwin.basyx.submodelregistry.client.model.ProtocolInformation();
        protocolInformation.setEndpointProtocol("http");
        protocolInformation.setHref(submodelUrl);
        endpoint.setProtocolInformation(protocolInformation);
        endpoints.add(endpoint);

        var submodelDescriptor = new SubmodelDescriptor();
        submodelDescriptor.setId(smId);
        submodelDescriptor.setIdShort(smIdShort);
        submodelDescriptor.setEndpoints(endpoints);
        if (semanticId != null) {
            var semanticIdRef = new Reference();
            semanticIdRef.setType(ReferenceTypes.EXTERNALREFERENCE);
            var semanticIdKey = new Key();
            semanticIdKey.setType(KeyTypes.GLOBALREFERENCE);
            semanticIdKey.setValue(semanticId);
            semanticIdRef.addKeysItem(semanticIdKey);
            submodelDescriptor.setSemanticId(semanticIdRef);
        }

        try {
            this.submodelRegistryApi.postSubmodelDescriptor(submodelDescriptor);
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                this.submodelRegistryApi.putSubmodelDescriptorById(submodelDescriptor.getId(), submodelDescriptor);
            }
            else {
                throw e;
            }
        }
    }

    public void unregisterSubmodel(String submodelId) throws ApiException {
        this.submodelRegistryApi.deleteSubmodelDescriptorById(submodelId);
    }

    public static org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor convertSubmodelDescriptor(SubmodelDescriptor submodelDescriptor) {
        try {
            var aasJsonSerializer = new JsonSerializer();
            var aasJsonDeserializer = new JsonDeserializer();

            var registryModelJson = aasJsonSerializer.write(submodelDescriptor);
            var convertedSubmodelDescriptor = aasJsonDeserializer.read(registryModelJson, DefaultSubmodelDescriptor.class);

            return convertedSubmodelDescriptor;
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (DeserializationException e) {
            throw new RuntimeException(e);
        }
    }

    public static SubmodelDescriptor convertSubmodelDescriptor(org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor submodelDescriptor) {
        try {
            var aasJsonSerializer = new JsonSerializer();
            var aasJsonDeserializer = new JsonDeserializer();

            var registryModelJson = aasJsonSerializer.write(submodelDescriptor);
            var convertedSubmodelDescriptor = aasJsonDeserializer.read(registryModelJson, SubmodelDescriptor.class);

            return convertedSubmodelDescriptor;
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (DeserializationException e) {
            throw new RuntimeException(e);
        }
    }
}
