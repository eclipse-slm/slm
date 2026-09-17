package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentSubmodelTemplate;
import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.slm.common.aas.submodels.deployment.DeploymentSubmodelTemplate.*;

/**
 * Deployment-Submodel eines einzelnen Deployment-Capability-Service.
 * Rein deklarativ: es beschreibt den Mechanismus und die Operationssignaturen,
 * die Ausfuehrung liegt im DeploymentSubmodelService.
 */
public class DeploymentSubmodel extends DefaultSubmodel {

    public DeploymentSubmodel(CapabilityService capabilityService) {
        super();

        var rawCapability = capabilityService.getCapability();
        if (!(rawCapability instanceof DeploymentCapability)) {
            throw new IllegalArgumentException("Capability service '" + capabilityService.getServiceId()
                    + "' does not wrap a DeploymentCapability, but a "
                    + rawCapability.getClass().getName());
        }
        var capability = (DeploymentCapability) rawCapability;

        this.id = DeploymentSubmodelTemplate.submodelIdFor(capabilityService.getServiceId());
        this.idShort = DeploymentSubmodelTemplate.idShortFor(capability.getName());
        this.setSemanticId(DeploymentSubmodelTemplate.semanticId());

        var elements = new ArrayList<SubmodelElement>();
        elements.add(aasReference(capabilityService));
        elements.add(mechanism(capabilityService, capability));
        elements.add(supportedDeploymentTypes(capability));
        elements.add(deployOperation());
        elements.add(deploymentStatusOperation());

        this.setSubmodelElements(elements);
    }

    private ReferenceElement aasReference(CapabilityService capabilityService) {
        var aasId = ResourceAas.createAasIdFromResourceId(capabilityService.getResourceId());

        return new DefaultReferenceElement.Builder()
                .idShort(SME_ASSET_ADMINISTRATION_SHELL)
                .value(new DefaultReference.Builder()
                        .type(ReferenceTypes.MODEL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.ASSET_ADMINISTRATION_SHELL)
                                .value(aasId)
                                .build())
                        .build())
                .build();
    }

    private SubmodelElementCollection mechanism(CapabilityService capabilityService,
                                                DeploymentCapability capability) {
        var properties = new ArrayList<SubmodelElement>();
        capabilityService.getCustomMeta().forEach((key, value) ->
                properties.add(stringProperty(key, value)));

        return new DefaultSubmodelElementCollection.Builder()
                .idShort(SMC_MECHANISM)
                .value(List.of(
                        stringProperty(SME_MECHANISM_NAME, capability.getName()),
                        // "version" is deliberately duplicated below: it is both a first-class field here
                        // and part of the complete, unfiltered customMeta passthrough in SMC_MECHANISM_PROPERTIES.
                        stringProperty(SME_MECHANISM_VERSION,
                                capabilityService.getCustomMeta().getOrDefault("version", "")),
                        new DefaultSubmodelElementCollection.Builder()
                                .idShort(SMC_MECHANISM_PROPERTIES)
                                .value(properties)
                                .build()))
                .build();
    }

    private SubmodelElementList supportedDeploymentTypes(DeploymentCapability capability) {
        var values = new ArrayList<SubmodelElement>();
        for (var deploymentType : capability.getSupportedDeploymentTypes()) {
            values.add(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.STRING)
                    .value(deploymentType.name())
                    .build());
        }

        return new DefaultSubmodelElementList.Builder()
                .idShort(SML_SUPPORTED_DEPLOYMENT_TYPES)
                .typeValueListElement(AasSubmodelElements.PROPERTY)
                .value(values)
                .build();
    }

    private Operation deployOperation() {
        return new DefaultOperation.Builder()
                .idShort(OP_DEPLOY)
                .inputVariables(List.of(
                        stringVariable(VAR_SERVICE_INSTANCE_ID),
                        stringVariable(VAR_DEPLOYMENT_TYPE),
                        variable(new DefaultBlob.Builder().idShort(VAR_DEPLOYMENT_DESCRIPTOR).build()),
                        variable(new DefaultSubmodelElementList.Builder()
                                .idShort(VAR_CREDENTIAL_REFERENCES)
                                .typeValueListElement(AasSubmodelElements.PROPERTY)
                                .build())))
                .outputVariables(List.of(
                        stringVariable(VAR_JOB_ID),
                        variable(new DefaultProperty.Builder()
                                .idShort(VAR_ACCEPTED)
                                .valueType(DataTypeDefXsd.BOOLEAN)
                                .build()),
                        stringVariable(VAR_MESSAGE)))
                .build();
    }

    private Operation deploymentStatusOperation() {
        return new DefaultOperation.Builder()
                .idShort(OP_GET_DEPLOYMENT_STATUS)
                .inputVariables(List.of(stringVariable(VAR_JOB_ID)))
                .outputVariables(List.of(stringVariable(VAR_STATE), stringVariable(VAR_MESSAGE)))
                .build();
    }

    private static Property stringProperty(String idShort, String value) {
        return new DefaultProperty.Builder()
                .idShort(idShort)
                .valueType(DataTypeDefXsd.STRING)
                .value(value)
                .build();
    }

    private static OperationVariable stringVariable(String idShort) {
        return variable(new DefaultProperty.Builder()
                .idShort(idShort)
                .valueType(DataTypeDefXsd.STRING)
                .build());
    }

    private static OperationVariable variable(SubmodelElement element) {
        return new DefaultOperationVariable.Builder().value(element).build();
    }
}
