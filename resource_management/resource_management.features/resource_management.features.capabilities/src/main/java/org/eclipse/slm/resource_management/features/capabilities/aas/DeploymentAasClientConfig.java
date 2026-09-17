package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClient;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClientFactory;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClient;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Stellt die rohen AAS-Clients als Spring-Beans bereit, damit DeploymentSubmodelRegistrar
 * sie direkt injizieren kann (statt der Factories) und dadurch unit-testbar mit Mocks bleibt.
 *
 * Es gibt an keiner anderen Stelle im Modul einen @Bean fuer SubmodelRegistryClient oder
 * AasRepositoryClient (nur die Factories selbst sind @Component), daher besteht kein
 * Kollisionsrisiko mit einer bereits vorhandenen Bean-Definition.
 */
@Configuration
public class DeploymentAasClientConfig {

    @Bean
    public SubmodelRegistryClient deploymentSubmodelRegistryClient(SubmodelRegistryClientFactory factory) {
        return factory.getClient();
    }

    @Bean
    public AasRepositoryClient deploymentAasRepositoryClient(AasRepositoryClientFactory factory) {
        return factory.getClient();
    }
}
