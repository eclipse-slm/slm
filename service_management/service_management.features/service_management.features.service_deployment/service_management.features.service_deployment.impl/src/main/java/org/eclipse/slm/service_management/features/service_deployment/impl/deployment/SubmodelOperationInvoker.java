package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;

/** Ruft eine AAS-Operation an einem Submodel-Endpoint auf. */
public interface SubmodelOperationInvoker {

    OperationVariable[] invoke(String endpoint, String submodelId, String idShortPath,
                              OperationVariable[] input, String accessToken);
}
