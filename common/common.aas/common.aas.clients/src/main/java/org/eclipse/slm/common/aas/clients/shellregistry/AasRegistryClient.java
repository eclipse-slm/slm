package org.eclipse.slm.common.aas.clients.shellregistry;

import feign.RequestInterceptor;
import org.apache.logging.log4j.util.Base64Util;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.slm.common.aas.clients.auth.AuthRequestInterceptor;
import org.eclipse.slm.common.aas.clients.base.FeignClientFactory;
import org.eclipse.slm.common.aas.clients.base.FeignResponseException;
import org.eclipse.slm.common.aas.model.shellregistry.exceptions.ShellDescriptorNotFoundException;
import org.eclipse.slm.common.aas.model.shellregistry.exceptions.SubmodellDescriptorNotFoundException;
import org.eclipse.slm.common.aas.model.shellregistry.requests.GetAllShellDescriptorsFilter;
import org.eclipse.slm.common.aas.model.shellregistry.respones.GetAssetAdministrationShellDescriptorsResult;
import org.eclipse.slm.common.aas.model.shellregistry.respones.GetSubmodelDescriptorsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AasRegistryClient {

    private static final Logger LOG = LoggerFactory.getLogger(AasRegistryClient.class);

    private final AasRegistryApiClient aasRegistryApiClient;

    public AasRegistryClient(String aasRegistryUrl, AuthRequestInterceptor authRequestInterceptor) {
        this.aasRegistryApiClient = FeignClientFactory.createClient(AasRegistryApiClient.class, aasRegistryUrl, authRequestInterceptor);
    }

    public GetAssetAdministrationShellDescriptorsResult getAllShellDescriptors(GetAllShellDescriptorsFilter filter) {
        var getShellDescriptorsResult = this.aasRegistryApiClient.getAllAssetAdministrationShellDescriptors(
                filter.getLimit(),
                filter.getCursor(),
                filter.getAssetKind(),
                filter.getAssetType());

        return getShellDescriptorsResult;
    }

    public Optional<AssetAdministrationShellDescriptor> getAasDescriptor(String aasId) {
        try {
            var shellDescriptor = this.getAasDescriptorOrThrow(aasId);

            return Optional.of(shellDescriptor);
        } catch (ShellDescriptorNotFoundException e) {
            return Optional.empty();
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 406) {
                // Catch HTTP 406 which is returned from Eclipse AASX Server if the AAS Descriptor is not found
                LOG.debug("Error while getting AAS Descriptor with id {}: {}", aasId, e.getMessage());
                return Optional.empty();
            }
            LOG.error("Error while getting AAS Descriptor with id {}: {}", aasId, e.getMessage());
            return Optional.empty();
        }
    }

    public AssetAdministrationShellDescriptor getAasDescriptorOrThrow(String aasId) throws ShellDescriptorNotFoundException {
        try {
            var aasIdEncoded = Base64Util.encode(aasId);
            var shellDescriptor = this.aasRegistryApiClient.getAssetAdministrationShellDescriptorById(aasIdEncoded);

            return shellDescriptor;
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new ShellDescriptorNotFoundException(aasId);
            }
            throw e;
        }
    }

    public AssetAdministrationShellDescriptor createOrUpdateShellDescriptor(AssetAdministrationShellDescriptor aasDescriptor) {
        try {
            var createdShellDescriptor = this.aasRegistryApiClient.postAssetAdministrationShellDescriptor(aasDescriptor);

            return createdShellDescriptor;
        }
        catch (FeignResponseException e) {
            if (e.getStatusCode() == 409) {
                var aasIdEncoded = Base64Util.encode(aasDescriptor.getId());
                this.aasRegistryApiClient.putAssetAdministrationShellDescriptorById(aasIdEncoded, aasDescriptor);
                return aasDescriptor;
            }
            else {
                throw e;
            }
        }
    }

    public void addSubmodelDescriptorToAas(String aasId, SubmodelDescriptor submodelDescriptor) {
        try {
            var aasIdEncoded = Base64Util.encode(aasId);
            this.aasRegistryApiClient.postSubmodelDescriptor(aasIdEncoded, submodelDescriptor);
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 409) {
                this.aasRegistryApiClient.putSubmodelDescriptorById(aasId, submodelDescriptor.getId(), submodelDescriptor);
            }
            else {
                throw new RuntimeException(e);
            }
        }
    }

    public SubmodelDescriptor createOrUpdateSubmodelDescriptor(String aasId, SubmodelDescriptor submodelDescriptor) {
        try {
            var aasIdEncoded = Base64Util.encode(aasId);
            return this.aasRegistryApiClient.postSubmodelDescriptor(aasIdEncoded, submodelDescriptor);
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 409) {
                this.aasRegistryApiClient.putSubmodelDescriptorById(aasId, submodelDescriptor.getId(), submodelDescriptor);
                return submodelDescriptor;
            } else {
                throw e;
            }
        }
    }

    public void deleteShellDescriptor(String aasId) {
        try {
            var aasIdEncoded = Base64Util.encode(aasId);
            this.aasRegistryApiClient.deleteAssetAdministrationShellDescriptorById(aasIdEncoded);
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new ShellDescriptorNotFoundException(aasId);
            }
            throw e;
        }
    }

    public GetSubmodelDescriptorsResult getAllSubmodelDescriptors(String aasId) {
        var aasIdEncoded = Base64Util.encode(aasId);
        var result = this.aasRegistryApiClient.getAllSubmodelDescriptors(aasIdEncoded);

        return result;
    }

    public void deleteSubmodelDescriptor(String aasId, String submodelId) {
        try {
            var aasIdEncoded = Base64Util.encode(aasId);
            var submodelIdEncoded = Base64Util.encode(submodelId);
            this.aasRegistryApiClient.deleteSubmodelDescriptorById(aasIdEncoded, submodelIdEncoded);
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new ShellDescriptorNotFoundException(aasId + ":" + submodelId);
            }
            throw e;
        }
    }

    public Optional<SubmodelDescriptor> getSubmodelDescriptor(String aasId, String submodelId) {
        try {
            var shellDescriptor = this.getSubmodelDescriptorOrThrow(aasId, submodelId);

            return Optional.of(shellDescriptor);
        } catch (SubmodellDescriptorNotFoundException e) {
            return Optional.empty();
        } catch (Exception e) {
            LOG.error("Error while getting Submodel Descriptor with id '{}' of AAS '{}': {}", submodelId, aasId, e.getMessage());
            return Optional.empty();
        }
    }

    public SubmodelDescriptor getSubmodelDescriptorOrThrow(String aasId, String submodelId) throws SubmodellDescriptorNotFoundException {
        try {
            var aasIdEncoded = Base64Util.encode(aasId);
            var submodelIdEncoded = Base64Util.encode(submodelId);
            var submodelDescriptor = this.aasRegistryApiClient.getSubmodelDescriptorById(aasIdEncoded, submodelIdEncoded);

            if (submodelDescriptor == null) {
                throw new SubmodellDescriptorNotFoundException(aasId, submodelId);
            } else {
                return submodelDescriptor;
            }
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new SubmodellDescriptorNotFoundException(aasId, submodelId);
            }
            throw e;
        }
    }
}
