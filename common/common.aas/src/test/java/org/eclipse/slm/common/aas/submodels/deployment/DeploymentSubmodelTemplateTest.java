package org.eclipse.slm.common.aas.submodels.deployment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentSubmodelTemplateTest {

    @Test
    @DisplayName("Submodel id is derived from the capability service id")
    void submodelIdIsDerivedFromCapabilityServiceId() {
        var capabilityServiceId = UUID.fromString("11111111-2222-3333-4444-555555555555");

        var submodelId = DeploymentSubmodelTemplate.submodelIdFor(capabilityServiceId);

        assertThat(submodelId).isEqualTo("Deployment-11111111-2222-3333-4444-555555555555");
    }

    @Test
    @DisplayName("idShort is prefixed and sanitized so it stays a valid AAS idShort")
    void idShortIsPrefixedAndSanitized() {
        assertThat(DeploymentSubmodelTemplate.idShortFor("Docker")).isEqualTo("Deployment_Docker");
        assertThat(DeploymentSubmodelTemplate.idShortFor("K3s Cluster (multi)"))
                .isEqualTo("Deployment_K3s_Cluster_multi");
    }

    @Test
    @DisplayName("semanticId reference carries the template IRI")
    void semanticIdCarriesTemplateIri() {
        var semanticId = DeploymentSubmodelTemplate.semanticId();

        assertThat(semanticId.getKeys()).hasSize(1);
        assertThat(semanticId.getKeys().get(0).getValue())
                .isEqualTo("https://eclipse.dev/slm/submodels/Deployment/1/0");
    }
}
