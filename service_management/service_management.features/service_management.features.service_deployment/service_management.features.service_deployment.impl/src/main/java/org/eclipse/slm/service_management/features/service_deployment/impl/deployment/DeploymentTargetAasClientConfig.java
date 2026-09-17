package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClient;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClientFactory;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClient;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Stellt die rohen AAS-Clients, die {@link DeploymentTargetHandler} und
 * {@link DeploymentSubmodelReader} im Konstruktor erwarten, als Spring-Beans bereit.
 * Die Werksklassen selbst werden nirgends sonst als Bean gebraucht.
 */
@Configuration
public class DeploymentTargetAasClientConfig {

    @Bean
    public SubmodelRegistryClient submodelRegistryClient(SubmodelRegistryClientFactory submodelRegistryClientFactory) {
        return submodelRegistryClientFactory.getClient();
    }

    @Bean
    public AasRepositoryClient aasRepositoryClient(AasRepositoryClientFactory aasRepositoryClientFactory) {
        return aasRepositoryClientFactory.getClient();
    }
}
