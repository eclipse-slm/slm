package org.eclipse.slm.common.aas.submodels.deployment;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.slm.common.model.DeploymentType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.eclipse.slm.common.aas.submodels.deployment.DeploymentSubmodelTemplate.*;

/**
 * Uebersetzt zwischen den typisierten DTOs und der OperationVariable-Darstellung der AAS.
 * Bewusst symmetrisch: jede from*-Methode hat genau eine to*-Gegenrichtung.
 */
public final class DeploymentOperationMapper {

    private DeploymentOperationMapper() {
    }

    // ---------- Deploy: Eingabe ----------

    public static OperationVariable[] fromDeployRequest(DeployRequest request) {
        var variables = new ArrayList<OperationVariable>();
        variables.add(stringVariable(VAR_SERVICE_INSTANCE_ID, request.serviceInstanceId().toString()));
        variables.add(stringVariable(VAR_DEPLOYMENT_TYPE, request.deploymentType().name()));
        variables.add(wrap(new DefaultBlob.Builder()
                .idShort(VAR_DEPLOYMENT_DESCRIPTOR)
                .contentType(request.descriptorContentType())
                .value(request.descriptor())
                .build()));
        variables.add(wrap(new DefaultSubmodelElementList.Builder()
                .idShort(VAR_CREDENTIAL_REFERENCES)
                .typeValueListElement(AasSubmodelElements.PROPERTY)
                .value(request.credentialReferences().stream()
                        .map(reference -> (SubmodelElement) new DefaultProperty.Builder()
                                .valueType(DataTypeDefXsd.STRING)
                                .value(reference)
                                .build())
                        .toList())
                .build()));
        return variables.toArray(new OperationVariable[0]);
    }

    public static DeployRequest toDeployRequest(OperationVariable[] variables) {
        var serviceInstanceId = UUID.fromString(requiredString(variables, VAR_SERVICE_INSTANCE_ID));
        var rawDeploymentType = requiredString(variables, VAR_DEPLOYMENT_TYPE);

        DeploymentType deploymentType;
        try {
            deploymentType = DeploymentType.valueOf(rawDeploymentType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown deployment type '" + rawDeploymentType
                    + "', expected one of " + Arrays.toString(DeploymentType.values()));
        }

        var blob = requiredBlob(variables, VAR_DEPLOYMENT_DESCRIPTOR);
        var credentialReferences = new ArrayList<String>();
        var credentialsElement = optionalElement(variables, VAR_CREDENTIAL_REFERENCES);
        if (credentialsElement instanceof SubmodelElementList list) {
            for (var element : list.getValue()) {
                if (element instanceof Property property && property.getValue() != null) {
                    credentialReferences.add(property.getValue());
                }
            }
        }

        return new DeployRequest(serviceInstanceId, deploymentType,
                blob.getValue(), blob.getContentType(), List.copyOf(credentialReferences));
    }

    // ---------- Deploy: Ausgabe ----------

    public static OperationVariable[] fromDeployResult(DeployResult result) {
        return new OperationVariable[]{
                stringVariable(VAR_JOB_ID, result.jobId()),
                wrap(new DefaultProperty.Builder()
                        .idShort(VAR_ACCEPTED)
                        .valueType(DataTypeDefXsd.BOOLEAN)
                        .value(Boolean.toString(result.accepted()))
                        .build()),
                stringVariable(VAR_MESSAGE, result.message())
        };
    }

    public static DeployResult toDeployResult(OperationVariable[] variables) {
        return new DeployResult(
                requiredString(variables, VAR_JOB_ID),
                Boolean.parseBoolean(requiredString(variables, VAR_ACCEPTED)),
                optionalString(variables, VAR_MESSAGE));
    }

    // ---------- GetDeploymentStatus ----------

    public static OperationVariable[] fromStatusRequestJobId(String jobId) {
        return new OperationVariable[]{stringVariable(VAR_JOB_ID, jobId)};
    }

    public static String toStatusRequestJobId(OperationVariable[] variables) {
        return requiredString(variables, VAR_JOB_ID);
    }

    public static OperationVariable[] fromDeploymentStatus(DeploymentStatus status) {
        return new OperationVariable[]{
                stringVariable(VAR_STATE, status.state().name()),
                stringVariable(VAR_MESSAGE, status.message())
        };
    }

    public static DeploymentStatus toDeploymentStatus(OperationVariable[] variables) {
        var rawState = requiredString(variables, VAR_STATE);
        DeploymentJobState state;
        try {
            state = DeploymentJobState.valueOf(rawState);
        } catch (IllegalArgumentException e) {
            state = DeploymentJobState.UNKNOWN;
        }
        return new DeploymentStatus(state, optionalString(variables, VAR_MESSAGE));
    }

    // ---------- Testhilfe ----------

    /** Ersetzt den Wert einer String-Variablen in place. Nur fuer Tests gedacht. */
    public static void overwriteStringValue(OperationVariable[] variables, String idShort, String value) {
        var element = requiredElement(variables, idShort);
        ((Property) element).setValue(value);
    }

    // ---------- intern ----------

    private static OperationVariable stringVariable(String idShort, String value) {
        return wrap(new DefaultProperty.Builder()
                .idShort(idShort)
                .valueType(DataTypeDefXsd.STRING)
                .value(value)
                .build());
    }

    private static OperationVariable wrap(SubmodelElement element) {
        return new DefaultOperationVariable.Builder().value(element).build();
    }

    private static SubmodelElement optionalElement(OperationVariable[] variables, String idShort) {
        if (variables == null) {
            return null;
        }
        for (var variable : variables) {
            var value = variable.getValue();
            if (value != null && idShort.equals(value.getIdShort())) {
                return value;
            }
        }
        return null;
    }

    private static SubmodelElement requiredElement(OperationVariable[] variables, String idShort) {
        var element = optionalElement(variables, idShort);
        if (element == null) {
            throw new IllegalArgumentException("Required operation variable '" + idShort + "' is missing");
        }
        return element;
    }

    private static String requiredString(OperationVariable[] variables, String idShort) {
        var element = requiredElement(variables, idShort);
        if (!(element instanceof Property property) || property.getValue() == null) {
            throw new IllegalArgumentException("Operation variable '" + idShort
                    + "' must be a Property with a value");
        }
        return property.getValue();
    }

    private static Blob requiredBlob(OperationVariable[] variables, String idShort) {
        var element = requiredElement(variables, idShort);
        if (!(element instanceof Blob blob)) {
            throw new IllegalArgumentException("Operation variable '" + idShort + "' must be a Blob");
        }
        return blob;
    }

    private static String optionalString(OperationVariable[] variables, String idShort) {
        var element = optionalElement(variables, idShort);
        if (element instanceof Property property && property.getValue() != null) {
            return property.getValue();
        }
        return "";
    }
}
