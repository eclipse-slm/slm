package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentSubmodelTemplate;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentSubmodelTest {

    private static final UUID RESOURCE_ID = UUID.fromString("11111111-0000-0000-0000-000000000001");
    private static final UUID CAPABILITY_SERVICE_ID = UUID.fromString("22222222-0000-0000-0000-000000000002");

    private CapabilityService dockerCapabilityService() {
        var capability = new DeploymentCapability();
        capability.setName("Docker");
        capability.setSupportedDeploymentTypes(
                List.of(DeploymentType.DOCKER_CONTAINER, DeploymentType.DOCKER_COMPOSE));

        return CapabilityService.builder(RESOURCE_ID, CAPABILITY_SERVICE_ID, capability)
                .status(CapabilityServiceStatus.READY)
                .customMeta(Map.of("version", "24.0.7"))
                .build();
    }

    private SubmodelElement elementByIdShort(Submodel submodel, String idShort) {
        return submodel.getSubmodelElements().stream()
                .filter(element -> idShort.equals(element.getIdShort()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No submodel element '" + idShort + "'"));
    }

    @Test
    @DisplayName("Submodel carries the template identity")
    void submodelCarriesTemplateIdentity() {
        var submodel = new DeploymentSubmodel(dockerCapabilityService());

        assertThat(submodel.getId()).isEqualTo("Deployment-22222222-0000-0000-0000-000000000002");
        assertThat(submodel.getIdShort()).isEqualTo("Deployment_Docker");
        assertThat(submodel.getSemanticId().getKeys().get(0).getValue())
                .isEqualTo(DeploymentSubmodelTemplate.SEMANTIC_ID_VALUE);
    }

    @Test
    @DisplayName("Submodel references the AAS of the asset it belongs to")
    void submodelReferencesOwningAas() {
        var submodel = new DeploymentSubmodel(dockerCapabilityService());

        var element = elementByIdShort(submodel, DeploymentSubmodelTemplate.SME_ASSET_ADMINISTRATION_SHELL);
        assertThat(element).isInstanceOf(ReferenceElement.class);

        var reference = ((ReferenceElement) element).getValue();
        assertThat(reference.getType()).isEqualTo(ReferenceTypes.MODEL_REFERENCE);
        assertThat(reference.getKeys().get(0).getType()).isEqualTo(KeyTypes.ASSET_ADMINISTRATION_SHELL);
        assertThat(reference.getKeys().get(0).getValue()).contains(RESOURCE_ID.toString());
    }

    @Test
    @DisplayName("Supported deployment types are listed as declared by the capability")
    void supportedDeploymentTypesAreListed() {
        var submodel = new DeploymentSubmodel(dockerCapabilityService());

        var list = (SubmodelElementList) elementByIdShort(
                submodel, DeploymentSubmodelTemplate.SML_SUPPORTED_DEPLOYMENT_TYPES);

        assertThat(list.getValue())
                .extracting(element -> ((Property) element).getValue())
                .containsExactly("DOCKER_CONTAINER", "DOCKER_COMPOSE");
    }

    @Test
    @DisplayName("Mechanism metadata comes from the capability service custom meta")
    void mechanismMetadataComesFromCustomMeta() {
        var submodel = new DeploymentSubmodel(dockerCapabilityService());

        var mechanism = (SubmodelElementCollection) elementByIdShort(
                submodel, DeploymentSubmodelTemplate.SMC_MECHANISM);
        var name = mechanism.getValue().stream()
                .filter(element -> DeploymentSubmodelTemplate.SME_MECHANISM_NAME.equals(element.getIdShort()))
                .map(element -> ((Property) element).getValue())
                .findFirst().orElseThrow();
        var properties = (SubmodelElementCollection) mechanism.getValue().stream()
                .filter(element -> DeploymentSubmodelTemplate.SMC_MECHANISM_PROPERTIES.equals(element.getIdShort()))
                .findFirst().orElseThrow();

        assertThat(name).isEqualTo("Docker");
        assertThat(properties.getValue())
                .extracting(SubmodelElement::getIdShort)
                .contains("version");
    }

    @Test
    @DisplayName("Both operations are present with their declared variables")
    void bothOperationsArePresent() {
        var submodel = new DeploymentSubmodel(dockerCapabilityService());

        var deploy = (Operation) elementByIdShort(submodel, DeploymentSubmodelTemplate.OP_DEPLOY);
        var status = (Operation) elementByIdShort(submodel, DeploymentSubmodelTemplate.OP_GET_DEPLOYMENT_STATUS);

        assertThat(deploy.getInputVariables())
                .extracting(variable -> variable.getValue().getIdShort())
                .containsExactly("ServiceInstanceId", "DeploymentType",
                        "DeploymentDescriptor", "CredentialReferences");
        assertThat(deploy.getOutputVariables())
                .extracting(variable -> variable.getValue().getIdShort())
                .containsExactly("JobId", "Accepted", "Message");
        assertThat(status.getInputVariables())
                .extracting(variable -> variable.getValue().getIdShort())
                .containsExactly("JobId");
        assertThat(status.getOutputVariables())
                .extracting(variable -> variable.getValue().getIdShort())
                .containsExactly("State", "Message");
    }
}
