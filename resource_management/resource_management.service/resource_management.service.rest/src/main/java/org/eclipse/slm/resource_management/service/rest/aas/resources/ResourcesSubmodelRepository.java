package org.eclipse.slm.resource_management.service.rest.aas.resources;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationSupport;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.common.aas.repositories.AbstractSubmodelRepository;
import org.eclipse.slm.common.aas.repositories.api.GetSubmodelsValueOnlyResult;
import org.eclipse.slm.common.aas.repositories.api.SubmodelValueOnly;
import org.eclipse.slm.resource_management.service.rest.aas.resources.deviceinfo.DeviceInfoSubmodelServiceFactory;
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

    private final DeviceInfoSubmodelServiceFactory deviceInfoSubmodelServiceFactory;

    public ResourcesSubmodelRepository(String aasId, AasRegistryClient aasRegistryClient,
                                       AasRepositoryClient aasRepositoryClient,
                                       SubmodelRegistryClient submodelRegistryClient,
                                       SubmodelRepositoryClient submodelRepositoryClient,
                                       DeviceInfoSubmodelServiceFactory deviceInfoSubmodelServiceFactory) {
        super(aasId);
        this.aasRegistryClient = aasRegistryClient;
        this.aasRepositoryClient = aasRepositoryClient;
        this.submodelRegistryClient = submodelRegistryClient;
        this.submodelRepositoryClient = submodelRepositoryClient;
        this.deviceInfoSubmodelServiceFactory = deviceInfoSubmodelServiceFactory;
        this.addSubmodelServiceFactory("DeviceInfo", deviceInfoSubmodelServiceFactory);
    }

    @Override
    public CursorResult<List<Submodel>> getAllSubmodels(PaginationInfo pInfo) {
        var localSubmodels = super.getAllSubmodels(pInfo).getResult();

        List<String> localSubmodelIds = localSubmodels.stream().map(Submodel::getId).toList();

        var remoteSubmodels = new ArrayList<Submodel>();
        var aas = this.aasRepositoryClient.getAas(this.aasId);

        for (var submodelRef : aas.getSubmodels()) {
            var submodelId = submodelRef.getKeys().get(0).getValue();

            try {
                if (!localSubmodelIds.contains(submodelId)) {
                    this.submodelRegistryClient.findSubmodelDescriptor(submodelId).ifPresent(submodelDescriptor -> {
                        var submodel = this.submodelRepositoryClient.getSubmodel(submodelDescriptor.getId());
                        remoteSubmodels.add(submodel);
                    });
                }
            } catch (ElementDoesNotExistException e) {
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
        var aas = this.aasRepositoryClient.getAas(this.aasId);

        for (var submodelRef : aas.getSubmodels()) {
            var submodelId = submodelRef.getKeys().get(0).getValue();

            try {
                if (!localSubmodelIds.contains(submodelId)) {
                    this.submodelRegistryClient.findSubmodelDescriptor(submodelId).ifPresent(submodelDescriptor -> {
                        var submodelEndpoint = submodelDescriptor.getEndpoints().get(0).getProtocolInformation().getHref();
                        if (submodelEndpoint.contains("/submodels/")) {
                            var scopedSubmodelRepositoryClient = SubmodelRepositoryClient.FromSubmodelDescriptor(submodelDescriptor, null);

                            var submodelValueOnlyBasyx = scopedSubmodelRepositoryClient.getSubmodelValueOnly(submodelDescriptor.getId());
                            if (submodelValueOnlyBasyx != null) {
                                var submodelValueOnly = new SubmodelValueOnly();
                                submodelValueOnly.setValuesOnlyMap(submodelValueOnlyBasyx.getValuesOnlyMap());
                                submodelValueOnly.setIdShort(submodelDescriptor.getIdShort());

                                submodelsValueOnlyLocalRemote.putIfAbsent(submodelDescriptor.getIdShort(), submodelValueOnly);
                            }
                        }

                        if (submodelEndpoint.endsWith("/submodel")) {
                            LOG.info("Value only representation for submodel '{}' is currently not supported by the submodel service", submodelId);
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
