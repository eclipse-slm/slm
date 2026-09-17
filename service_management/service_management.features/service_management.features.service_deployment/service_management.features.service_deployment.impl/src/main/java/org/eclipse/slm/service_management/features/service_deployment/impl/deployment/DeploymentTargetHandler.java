package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClient;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentSubmodelTemplate;
import org.eclipse.slm.common.model.DeploymentType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Einzige Quelle fuer die Frage "wer kann was deployen". Fragt die Submodel-Registry
 * nach der Deployment-semanticId und liest je Treffer die Faehigkeiten aus.
 */
@Component
public class DeploymentTargetHandler {

    private final SubmodelRegistryClient submodelRegistryClient;
    private final DeploymentSubmodelReader submodelReader;

    public DeploymentTargetHandler(SubmodelRegistryClient submodelRegistryClient,
                                   DeploymentSubmodelReader submodelReader) {
        this.submodelRegistryClient = submodelRegistryClient;
        this.submodelReader = submodelReader;
    }

    public List<DeploymentTarget> getDeploymentTargets(Optional<DeploymentType> deploymentTypeFilter) {
        var descriptors = this.submodelRegistryClient.findSubmodelDescriptorsWithSemanticIds(
                List.of(DeploymentSubmodelTemplate.SEMANTIC_ID_VALUE));

        return descriptors.stream()
                .map(this.submodelReader::read)
                .flatMap(Optional::stream)
                .filter(target -> deploymentTypeFilter.map(target::supports).orElse(true))
                .toList();
    }

    public DeploymentTarget getDeploymentTargetOrThrow(String submodelId) {
        return this.getDeploymentTargets(Optional.empty()).stream()
                .filter(target -> submodelId.equals(target.submodelId()))
                .findFirst()
                .orElseThrow(() -> new DeploymentTargetNotFoundException(submodelId));
    }
}
