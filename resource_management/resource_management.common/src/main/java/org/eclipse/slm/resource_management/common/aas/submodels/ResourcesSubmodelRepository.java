package org.eclipse.slm.resource_management.common.aas.submodels;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationSupport;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.common.aas.clients.exceptions.ShellNotFoundException;
import org.eclipse.slm.common.aas.repositories.submodels.AbstractSubmodelRepository;
import org.eclipse.slm.common.aas.repositories.api.submodels.GetSubmodelsValueOnlyResult;
import org.eclipse.slm.common.aas.repositories.api.submodels.SubmodelValueOnly;
import org.eclipse.slm.resource_management.common.aas.submodels.deviceinfo.DeviceInfoSubmodelServiceFactory;
import org.modelmapper.internal.bytebuddy.pool.TypePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ResourcesSubmodelRepository extends AbstractSubmodelRepository {

    public final static Logger LOG = LoggerFactory.getLogger(ResourcesSubmodelRepository.class);

    private final AasRegistryClient aasRegistryClient;

    private final AasRepositoryClient aasRepositoryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    private final SubmodelRepositoryClient submodelRepositoryClient;

    public ResourcesSubmodelRepository(String aasId, AasRegistryClientFactory aasRegistryClientFactory,
                                       AasRepositoryClientFactory aasRepositoryClientFactory,
                                       SubmodelRegistryClientFactory submodelRegistryClientFactory,
                                       SubmodelRepositoryClientFactory submodelRepositoryClientFactory,
                                       DeviceInfoSubmodelServiceFactory deviceInfoSubmodelServiceFactory) {
        super(aasId);
        this.aasRegistryClient = aasRegistryClientFactory.getClient();
        this.aasRepositoryClient = aasRepositoryClientFactory.getClient();
        this.submodelRegistryClient = submodelRegistryClientFactory.getClient();
        this.submodelRepositoryClient = submodelRepositoryClientFactory.getClient();
        this.addSubmodelServiceFactory("DeviceInfo", deviceInfoSubmodelServiceFactory);
    }

    @Override
    public CursorResult<List<Submodel>> getAllSubmodels(PaginationInfo pInfo) {
        var localSubmodels = super.getAllSubmodels(pInfo).getResult();

        List<String> localSubmodelIds = localSubmodels.stream().map(Submodel::getId).toList();

        var remoteSubmodels = new ArrayList<Submodel>();
        var resourceAasOptional = aasRepositoryClient.getAas(this.aasId);
        if (resourceAasOptional.isEmpty()) {
            LOG.error("Resource AAS with ID {} not found", this.aasId);
            throw new ShellNotFoundException(this.aasId);
        }
        var resourceAas = resourceAasOptional.get();

        for (var submodelRef : resourceAas.getSubmodels()) {
            var submodelId = submodelRef.getKeys().get(0).getValue();

            try {
                if (!localSubmodelIds.contains(submodelId)) {
                    this.submodelRegistryClient.findSubmodelDescriptor(submodelId).ifPresent(submodelDescriptor -> {
                        var submodelEndpoint = submodelDescriptor.getEndpoints().get(0).getProtocolInformation().getHref();
                        if (submodelEndpoint.contains("/submodels/")) {
                            var scopedSubmodelRepositoryClient = SubmodelRepositoryClientFactory.FromSubmodelDescriptor(submodelDescriptor, null);
                            var submodel = scopedSubmodelRepositoryClient.getSubmodel(submodelDescriptor.getId());
                            remoteSubmodels.add(submodel);
                        }
                        if (submodelEndpoint.endsWith("/submodel")) {
                            LOG.debug("Value only representation for submodel '{}' is currently not supported by the submodel service", submodelId);
                        }
                    });
                }
            } catch (ElementDoesNotExistException | IllegalArgumentException e) {
                LOG.info(e.getMessage());
            }
        }

        var allSubmodels = new ArrayList<Submodel>();
        allSubmodels.addAll(localSubmodels);
        allSubmodels.addAll(remoteSubmodels);

        TreeMap<String, Submodel> submodelMap = allSubmodels.stream().collect(Collectors.toMap(Submodel::getId, submodel -> submodel, (a, b) -> a, TreeMap::new));
        PaginationSupport<Submodel> paginationSupport = new PaginationSupport<>(submodelMap, Submodel::getId);

        return paginationSupport.getPaged(pInfo);
    }

    @Override
    public GetSubmodelsValueOnlyResult getAllSubmodelsValueOnly(PaginationInfo pInfo) {
        var submodelsValueOnlyLocal = super.getAllSubmodelsValueOnly(pInfo);

        var localSubmodels = super.getAllSubmodels(pInfo).getResult();
        List<String> localSubmodelIds = localSubmodels.stream().map(Submodel::getId).toList();

        var submodelsValueOnlyLocalRemote = new GetSubmodelsValueOnlyResult();
        var resourceAasOptional = aasRepositoryClient.getAas(this.aasId);
        if (resourceAasOptional.isEmpty()) {
            LOG.error("Resource AAS with ID {} not found", this.aasId);
            throw new ShellNotFoundException(this.aasId);
        }
        var resourceAas = resourceAasOptional.get();

        for (var submodelRef : resourceAas.getSubmodels()) {
            var submodelId = submodelRef.getKeys().get(0).getValue();

            try {
                if (!localSubmodelIds.contains(submodelId)) {
                    this.submodelRegistryClient.findSubmodelDescriptor(submodelId).ifPresent(submodelDescriptor -> {
                        var submodelEndpoint = submodelDescriptor.getEndpoints().get(0).getProtocolInformation().getHref();
                        if (submodelEndpoint.contains("/submodels/")) {
                            var scopedSubmodelRepositoryClient = SubmodelRepositoryClientFactory.FromSubmodelDescriptor(submodelDescriptor, null);

                            var submodelValueOnlyBasyx = scopedSubmodelRepositoryClient.getSubmodelValueOnly(submodelDescriptor.getId());
                            if (submodelValueOnlyBasyx != null) {
                                var submodelValueOnly = new SubmodelValueOnly();
                                submodelValueOnly.setValuesOnlyMap(submodelValueOnlyBasyx.getValuesOnlyMap());
                                submodelValueOnly.setIdShort(submodelDescriptor.getIdShort());
                                submodelValueOnly.setId(submodelDescriptor.getId());
                                try {
                                    submodelValueOnly.setSemanticId(submodelDescriptor.getSemanticId().getKeys().get(0).getValue());
                                } catch (Exception e) {
                                    LOG.debug("No semantic ID for submodel '{}'", submodelDescriptor.getIdShort());
                                }

                                submodelsValueOnlyLocalRemote.putIfAbsent(submodelDescriptor.getIdShort(), submodelValueOnly);
                            }
                        }

                        if (submodelEndpoint.endsWith("/submodel")) {
                            LOG.debug("Value only representation for submodel '{}' is currently not supported by the submodel service", submodelId);
                        }
                    });
                }
            } catch (ElementDoesNotExistException | org.eclipse.digitaltwin.basyx.client.internal.ApiException | IllegalStateException | NullPointerException e) {
                LOG.debug("Failed to get value only representation for submodel '{}': {}", submodelId, e.getMessage());
            }
        }

        var allSubmodelSubmodelsValueOnly = new GetSubmodelsValueOnlyResult();
        allSubmodelSubmodelsValueOnly.putAll(submodelsValueOnlyLocal);
        allSubmodelSubmodelsValueOnly.putAll(submodelsValueOnlyLocalRemote);

        return allSubmodelSubmodelsValueOnly;
    }
}
