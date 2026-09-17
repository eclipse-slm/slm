package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.slm.common.model.DeploymentType;

import java.util.List;
import java.util.Map;

/**
 * Ein aufrufbares Deployment-Ziel, wie es aus einem Deployment-Submodel gelesen wurde.
 *
 * @param submodelId  Identitaet des Ziels; landet in ServiceOrder.deploymentTargetSubmodelId
 * @param submodelEndpoint HTTP-Endpoint des Submodels aus dem Registry-Deskriptor
 * @param aasId       AAS, auf die das Submodel verweist
 * @param displayName Anzeigename, aufgeloest ueber die AAS
 */
public record DeploymentTarget(
        String submodelId,
        String submodelEndpoint,
        String aasId,
        String displayName,
        String mechanismName,
        String mechanismVersion,
        List<DeploymentType> supportedDeploymentTypes,
        Map<String, String> mechanismProperties
) {
    public boolean supports(DeploymentType deploymentType) {
        return this.supportedDeploymentTypes.contains(deploymentType);
    }
}
