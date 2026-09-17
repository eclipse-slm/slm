package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.slm.aas.repositories.submodels.AbstractSubmodelService;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentOperationMapper;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentSubmodelTemplate;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.function.Supplier;

/**
 * Liefert das Deployment-Submodel eines Capability-Service aus und fuehrt seine Operationen aus.
 * Der Token-Supplier entkoppelt den Service vom Spring-Security-Kontext und macht ihn testbar.
 *
 * Note: the supplier produces a {@link JwtAuthenticationToken} rather than a plain token string,
 * because DeploymentJobManager.deploy requires one (the external AwxCredential type has no
 * plain-string-token constructor - see DeploymentJobManager's own javadoc for details).
 */
public class DeploymentSubmodelService extends AbstractSubmodelService {

    private final CapabilityService capabilityService;
    private final DeploymentJobManager jobManager;
    private final Supplier<JwtAuthenticationToken> accessTokenSupplier;

    public DeploymentSubmodelService(CapabilityService capabilityService,
                                     DeploymentJobManager jobManager,
                                     Supplier<JwtAuthenticationToken> accessTokenSupplier) {
        this.capabilityService = capabilityService;
        this.jobManager = jobManager;
        this.accessTokenSupplier = accessTokenSupplier;
    }

    @Override
    public Submodel getSubmodel() {
        return new DeploymentSubmodel(this.capabilityService);
    }

    @Override
    public OperationVariable[] invokeOperation(String idShortPath, OperationVariable[] input)
            throws ElementDoesNotExistException {

        if (DeploymentSubmodelTemplate.OP_DEPLOY.equals(idShortPath)) {
            var request = DeploymentOperationMapper.toDeployRequest(input);
            var result = this.jobManager.deploy(
                    this.capabilityService.getServiceId(), request, this.accessTokenSupplier.get());
            return DeploymentOperationMapper.fromDeployResult(result);
        }

        if (DeploymentSubmodelTemplate.OP_GET_DEPLOYMENT_STATUS.equals(idShortPath)) {
            var jobId = DeploymentOperationMapper.toStatusRequestJobId(input);
            return DeploymentOperationMapper.fromDeploymentStatus(this.jobManager.getStatus(jobId));
        }

        throw new ElementDoesNotExistException(idShortPath);
    }
}
