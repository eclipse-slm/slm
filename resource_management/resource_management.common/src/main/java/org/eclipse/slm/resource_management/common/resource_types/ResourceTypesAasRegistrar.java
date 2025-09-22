package org.eclipse.slm.resource_management.common.resource_types;

import org.eclipse.digitaltwin.basyx.aasregistry.client.ApiException;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.common.aas.repositories.PathInspector;
import org.eclipse.slm.common.aas.clients.ShellUtils;
import org.eclipse.slm.resource_management.common.resource_types.aas.shells.ResourceTypesAasFactory;
import org.eclipse.slm.resource_management.common.resource_types.aas.shells.ResourceTypesAasServiceHTTPApiController;
import org.eclipse.slm.resource_management.common.resource_types.aas.submodels.ResourceTypeSubmodelRepositoryFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Base64;

@Component
public class ResourceTypesAasRegistrar {

    private final AasRegistryClient aasRegistryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    private final ResourceTypesAasFactory resourceTypesAasFactory;

    private final ResourceTypeSubmodelRepositoryFactory resourceTypeSubmodelRepositoryFactory;

    private final String deploymentScheme;

    private final String deploymentUrl;

    public ResourceTypesAasRegistrar(AasRegistryClientFactory aasRegistryClientFactory,
                                     SubmodelRegistryClientFactory submodelRegistryClientFactory,
                                     ResourceTypesAasFactory resourceTypesAasFactory,
                                     ResourceTypeSubmodelRepositoryFactory resourceTypeSubmodelRepositoryFactory,
                                     @Value("${deployment.scheme}") String deploymentScheme,
                                     @Value("${deployment.url}") String deploymentUrl
    ) {
        this.aasRegistryClient = aasRegistryClientFactory.getClient();
        this.submodelRegistryClient = submodelRegistryClientFactory.getClient();
        this.resourceTypesAasFactory = resourceTypesAasFactory;
        this.resourceTypeSubmodelRepositoryFactory = resourceTypeSubmodelRepositoryFactory;
        this.deploymentScheme = deploymentScheme;
        this.deploymentUrl = deploymentUrl;
    }

    @PostConstruct
    public void registerShellsAndSubmodels() throws ApiException, org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException {

        // Register Resource Types AAS
        var resourceTypesAas = resourceTypesAasFactory.createAas("");
        var resourceTypesAasControllerPathForAas = PathInspector.getRequestPathForMethod(
                ResourceTypesAasServiceHTTPApiController.class, "getAssetAdministrationShell");
        var resourceTypeAasEndpointUrl = this.deploymentUrl + resourceTypesAasControllerPathForAas;
        var resourceTypeAasDescriptor = ShellUtils.createShellDescriptorForShell(
                resourceTypesAas,
                this.deploymentScheme,
                resourceTypeAasEndpointUrl);
        this.aasRegistryClient.createOrUpdateShellDescriptor(resourceTypeAasDescriptor);

        // Register Resource Type Submodels
        var resourceTypeSubmodels = resourceTypeSubmodelRepositoryFactory.getSubmodelRepository("")
                .getAllSubmodels(new PaginationInfo(Integer.MAX_VALUE, null));

        for (var submodel : resourceTypeSubmodels.getResult()) {
            var submodelIdBase64Encoded = Base64.getEncoder().encodeToString(submodel.getId().getBytes());
            var resourceTypesAasControllerPathForSubmodel = PathInspector.getRequestPathForMethod(
                    ResourceTypesAasServiceHTTPApiController.class, "getSubmodelById");
            resourceTypesAasControllerPathForSubmodel = resourceTypesAasControllerPathForSubmodel
                    .replace("{submodelIdentifier}", submodelIdBase64Encoded);
            var resourceTypeSubmodelEndpointUrl = this.deploymentUrl + resourceTypesAasControllerPathForSubmodel;
            var submodelDescriptor = SubmodelUtils.createSubmodelDescriptorForSubmodel(
                    submodel,
                    this.deploymentScheme,
                    resourceTypeSubmodelEndpointUrl
            );

            this.submodelRegistryClient.createOrUpdateSubmodelDescriptor(submodelDescriptor);
        }

    }

}
