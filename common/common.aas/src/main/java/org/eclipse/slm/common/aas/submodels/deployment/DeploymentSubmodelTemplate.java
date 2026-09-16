package org.eclipse.slm.common.aas.submodels.deployment;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import java.util.UUID;

/**
 * Vertrag des Deployment-Submodels. Einzige Quelle der Wahrheit fuer Anbieter- und Nutzer-Seite.
 */
public final class DeploymentSubmodelTemplate {

    private DeploymentSubmodelTemplate() {
    }

    public static final String SEMANTIC_ID_VALUE = "https://eclipse.dev/slm/submodels/Deployment/1/0";

    public static final String SUBMODEL_ID_PREFIX = "Deployment";
    public static final String ID_SHORT_PREFIX = "Deployment_";

    public static final String SME_ASSET_ADMINISTRATION_SHELL = "AssetAdministrationShell";
    public static final String SMC_MECHANISM = "Mechanism";
    public static final String SME_MECHANISM_NAME = "Name";
    public static final String SME_MECHANISM_VERSION = "Version";
    public static final String SMC_MECHANISM_PROPERTIES = "Properties";
    public static final String SML_SUPPORTED_DEPLOYMENT_TYPES = "SupportedDeploymentTypes";

    public static final String OP_DEPLOY = "Deploy";
    public static final String OP_GET_DEPLOYMENT_STATUS = "GetDeploymentStatus";

    public static final String VAR_SERVICE_INSTANCE_ID = "ServiceInstanceId";
    public static final String VAR_DEPLOYMENT_TYPE = "DeploymentType";
    public static final String VAR_DEPLOYMENT_DESCRIPTOR = "DeploymentDescriptor";
    public static final String VAR_CREDENTIAL_REFERENCES = "CredentialReferences";
    public static final String VAR_JOB_ID = "JobId";
    public static final String VAR_ACCEPTED = "Accepted";
    public static final String VAR_MESSAGE = "Message";
    public static final String VAR_STATE = "State";

    public static String submodelIdFor(UUID capabilityServiceId) {
        return SUBMODEL_ID_PREFIX + "-" + capabilityServiceId;
    }

    public static String idShortFor(String mechanismName) {
        var sanitized = mechanismName
                .replaceAll("[^A-Za-z0-9_]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        return ID_SHORT_PREFIX + sanitized;
    }

    public static Reference semanticId() {
        return new DefaultReference.Builder()
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.CONCEPT_DESCRIPTION)
                        .value(SEMANTIC_ID_VALUE)
                        .build())
                .build();
    }
}
