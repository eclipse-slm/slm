package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.slm.aas.clients.submodelrepository.SubmodelRepositoryClient;
import org.eclipse.slm.aas.clients.submodelservice.SubmodelServiceClient;
import org.eclipse.slm.aas.clients.auth.BearerTokenAuthRequestInterceptor;
import org.springframework.stereotype.Component;

/**
 * Adapter auf das AAS-SDK. Waehlt anhand der Endpoint-Form den passenden Client:
 * ein Repository-Endpoint endet auf /submodels/{id}, ein Service-Endpoint auf /submodel.
 */
@Component
public class SdkSubmodelOperationInvoker implements SubmodelOperationInvoker {

    @Override
    public OperationVariable[] invoke(String endpoint, String submodelId, String idShortPath,
                                      OperationVariable[] input, String accessToken) {
        var interceptor = new BearerTokenAuthRequestInterceptor(accessToken);

        if (endpoint.endsWith("/submodel")) {
            return new SubmodelServiceClient(endpoint, interceptor).invokeOperation(idShortPath, input);
        }

        var repositoryUrl = endpoint.substring(0, endpoint.indexOf("/submodels/"));
        return new SubmodelRepositoryClient(repositoryUrl, interceptor)
                .invokeOperation(submodelId, idShortPath, input);
    }
}
