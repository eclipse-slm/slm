package org.eclipse.slm.common.aas.clients.submodelregistry;

import org.apache.logging.log4j.util.Base64Util;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.slm.common.aas.clients.auth.AuthRequestInterceptor;
import org.eclipse.slm.common.aas.clients.base.FeignClientFactory;
import org.eclipse.slm.common.aas.clients.base.FeignResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubmodelRegistryClient {

    private static final Logger LOG = LoggerFactory.getLogger(SubmodelRegistryClient.class);

    private final SubmodelRegistryApiClient submodelRegistryApiClient;

    public SubmodelRegistryClient(String submodelRegistryUrl, AuthRequestInterceptor authRequestInterceptor) {
        this.submodelRegistryApiClient = FeignClientFactory.createClient(SubmodelRegistryApiClient.class, submodelRegistryUrl, authRequestInterceptor);
    }

    public List<SubmodelDescriptor> getAllSubmodelDescriptors() {
        List<SubmodelDescriptor> submodelDescriptors = new ArrayList<>();
        try {
            var result = this.submodelRegistryApiClient.getAllSubmodelDescriptors(Integer.MAX_VALUE, null);
            submodelDescriptors = result.getResult();

            return submodelDescriptors;
        } catch (FeignResponseException e) {
            if (e.getStatusCode() != 404) {
                LOG.error(e.getMessage());
            }
            return submodelDescriptors;
        }
    }

    public Optional<SubmodelDescriptor> getSubmodelDescriptor(String submodelId) {
        var submodelIdEncoded = Base64Util.encode(submodelId);
        try {
            var submodelDescriptor = this.submodelRegistryApiClient.getSubmodelDescriptorById(submodelIdEncoded);
            return Optional.of(submodelDescriptor);
        } catch (FeignResponseException e) {
            if (e.getStatusCode() != 404) {
                LOG.error(e.getMessage());
            }
            return Optional.empty();
        }
    }

    public List<SubmodelDescriptor> findSubmodelDescriptorsWithSemanticIds(List<String> semanticIds) {
        List<SubmodelDescriptor> submodelDescriptors = new ArrayList<>();
        try {
            var allSubmodelDescriptors = this.submodelRegistryApiClient.getAllSubmodelDescriptors(Integer.MAX_VALUE, null).getResult();
            var submodelDescriptorsWithSemanticId = allSubmodelDescriptors.stream()
                    .filter(smd -> {
                                if (smd.getSemanticId() != null) {
                                    return semanticIds.contains(smd.getSemanticId().getKeys().get(0).getValue());
                                } else {
                                    return false;
                                }
                            })
                    .toList();

            return submodelDescriptorsWithSemanticId;
        } catch (FeignResponseException e) {
            if (e.getStatusCode() != 404) {
                LOG.error(e.getMessage());
            }
            return submodelDescriptors;
        }
    }

    public void createOrUpdateSubmodelDescriptor(SubmodelDescriptor submodelDescriptor) {
        try {
            this.submodelRegistryApiClient.postSubmodelDescriptor(submodelDescriptor);
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 409) {
                var submodelIdEncoded = Base64Util.encode(submodelDescriptor.getId());
                this.submodelRegistryApiClient.putSubmodelDescriptorById(submodelIdEncoded, submodelDescriptor);
            } else {
                throw e;
            }
        }
    }

    public void registerSubmodel(String submodelUrl, String smId, String smIdShort, String semanticId) {
        var semanticIdRef = new DefaultReference.Builder()
                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.SUBMODEL)
                        .value(semanticId)
                        .build()
                ).build();

        this.registerSubmodel(submodelUrl, smId, smIdShort, semanticIdRef);
    }

    public void registerSubmodel(String submodelUrl, String smId, String smIdShort, Reference semanticId) {
        var endpoints = new ArrayList<Endpoint>();
        var endpoint = new DefaultEndpoint();
        endpoint.set_interface("SUBMODEL-3.0");
        var protocolInformation = new DefaultProtocolInformation();
        protocolInformation.setEndpointProtocol("http");
        protocolInformation.setHref(submodelUrl);
        endpoint.setProtocolInformation(protocolInformation);
        endpoints.add(endpoint);

        var submodelDescriptor = new DefaultSubmodelDescriptor();
        submodelDescriptor.setId(smId);
        submodelDescriptor.setIdShort(smIdShort);
        submodelDescriptor.setEndpoints(endpoints);
        submodelDescriptor.setSemanticId(semanticId);

        var submodelIdEncoded = Base64Util.encode(smId);

        try {
            this.submodelRegistryApiClient.postSubmodelDescriptor(submodelDescriptor);
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 409) {
                this.submodelRegistryApiClient.putSubmodelDescriptorById(submodelIdEncoded, submodelDescriptor);
            }
            else {
                throw e;
            }
        }
    }


    public void unregisterSubmodel(String submodelId) {
        var submodelIdEncoded = Base64Util.encode(submodelId);
        this.submodelRegistryApiClient.deleteSubmodelDescriptorById(submodelIdEncoded);
    }
}
