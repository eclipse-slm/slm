package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationSupport;
import org.eclipse.slm.aas.repositories.exceptions.SubmodelNotFoundException;
import org.eclipse.slm.aas.repositories.submodels.AbstractSubmodelRepository;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentSubmodelTemplate;
import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Liefert je Deployment-Capability-Service einer Resource ein Deployment-Submodel.
 *
 * Note: the token supplier produces a {@link JwtAuthenticationToken} rather than a plain token
 * string, because DeploymentSubmodelService/DeploymentJobManager require one (the external
 * AwxCredential type has no plain-string-token constructor - see DeploymentJobManager's javadoc).
 */
public class DeploymentSubmodelRepository extends AbstractSubmodelRepository {

    private final CapabilitiesConsulClient capabilitiesConsulClient;
    private final DeploymentJobManager jobManager;
    private final Supplier<JwtAuthenticationToken> accessTokenSupplier;

    public DeploymentSubmodelRepository(String aasId,
                                        CapabilitiesConsulClient capabilitiesConsulClient,
                                        DeploymentJobManager jobManager,
                                        Supplier<JwtAuthenticationToken> accessTokenSupplier) {
        super(aasId);
        this.capabilitiesConsulClient = capabilitiesConsulClient;
        this.jobManager = jobManager;
        this.accessTokenSupplier = accessTokenSupplier;
    }

    @Override
    public CursorResult<List<Submodel>> getAllSubmodels(PaginationInfo pInfo) {
        var submodels = this.deploymentCapabilityServices().stream()
                .map(capabilityService -> (Submodel) new DeploymentSubmodel(capabilityService))
                .collect(Collectors.toMap(Submodel::getId, submodel -> submodel, (a, b) -> a, TreeMap::new));

        return new PaginationSupport<>(submodels, Submodel::getId).getPaged(pInfo);
    }

    @Override
    public Submodel getSubmodel(String submodelId) throws ElementDoesNotExistException {
        return new DeploymentSubmodel(this.requireCapabilityService(submodelId));
    }

    @Override
    public OperationVariable[] invokeOperation(String submodelId, String idShortPath, OperationVariable[] input)
            throws ElementDoesNotExistException {
        return this.submodelService(this.requireCapabilityService(submodelId))
                .invokeOperation(idShortPath, input);
    }

    private DeploymentSubmodelService submodelService(CapabilityService capabilityService) {
        return new DeploymentSubmodelService(capabilityService, this.jobManager, this.accessTokenSupplier);
    }

    private List<CapabilityService> deploymentCapabilityServices() {
        var resourceId = UUID.fromString(ResourceAas.getResourceIdFromAasId(this.aasId));

        return this.capabilitiesConsulClient.getCapabilityServicesOfResource(resourceId).stream()
                .filter(capabilityService -> capabilityService.getCapability() instanceof DeploymentCapability)
                .toList();
    }

    private CapabilityService requireCapabilityService(String submodelId) {
        Optional<CapabilityService> match = this.deploymentCapabilityServices().stream()
                .filter(capabilityService -> submodelId.equals(
                        DeploymentSubmodelTemplate.submodelIdFor(capabilityService.getServiceId())))
                .findFirst();

        return match.orElseThrow(() -> new SubmodelNotFoundException(this.aasId, submodelId));
    }
}
