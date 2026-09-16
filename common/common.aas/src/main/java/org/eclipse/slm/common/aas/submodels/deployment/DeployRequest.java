package org.eclipse.slm.common.aas.submodels.deployment;

import org.eclipse.slm.common.model.DeploymentType;

import java.util.List;
import java.util.UUID;

/**
 * @param serviceInstanceId   vom Aufrufer erzeugte UUID; er besitzt die Korrelation
 * @param deploymentType      einer der Werte aus SupportedDeploymentTypes des Ziels
 * @param descriptor          gerendertes Artefakt als Rohbytes (UTF-8)
 * @param descriptorContentType z.B. "application/yaml" oder "application/json"
 * @param credentialReferences ausschliesslich Vault-Pfade, niemals Klartext-Zugangsdaten
 */
public record DeployRequest(
        UUID serviceInstanceId,
        DeploymentType deploymentType,
        byte[] descriptor,
        String descriptorContentType,
        List<String> credentialReferences
) {
}
