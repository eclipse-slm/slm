package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.slm.common.aas.submodels.deployment.*;
import org.springframework.stereotype.Component;

@Component
public class AasDeploymentClient {

    private final SubmodelOperationInvoker invoker;

    public AasDeploymentClient(SubmodelOperationInvoker invoker) {
        this.invoker = invoker;
    }

    public DeployResult deploy(DeploymentTarget target, DeployRequest request, String accessToken) {
        try {
            var output = this.invoker.invoke(target.submodelEndpoint(), target.submodelId(),
                    DeploymentSubmodelTemplate.OP_DEPLOY,
                    DeploymentOperationMapper.fromDeployRequest(request), accessToken);
            return DeploymentOperationMapper.toDeployResult(output);
        } catch (Exception e) {
            throw new DeploymentInvocationException(
                    target.submodelId(), DeploymentSubmodelTemplate.OP_DEPLOY, e);
        }
    }

    public DeploymentStatus getStatus(DeploymentTarget target, String jobId, String accessToken) {
        try {
            var output = this.invoker.invoke(target.submodelEndpoint(), target.submodelId(),
                    DeploymentSubmodelTemplate.OP_GET_DEPLOYMENT_STATUS,
                    DeploymentOperationMapper.fromStatusRequestJobId(jobId), accessToken);
            return DeploymentOperationMapper.toDeploymentStatus(output);
        } catch (Exception e) {
            throw new DeploymentInvocationException(
                    target.submodelId(), DeploymentSubmodelTemplate.OP_GET_DEPLOYMENT_STATUS, e);
        }
    }
}
