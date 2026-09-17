package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClient;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClient;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentSubmodelTemplate;
import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Haelt die Submodel-Registry mit den installierten Deployment-Capability-Services im Gleichklang.
 * Ohne Registry-Eintrag findet die Ziel-Suche (findSubmodelDescriptorsWithSemanticIds) das Submodel nicht,
 * obwohl es ueber die AAS direkt abrufbar waere.
 */
@Component
public class DeploymentSubmodelRegistrar {

    private final static Logger LOG = LoggerFactory.getLogger(DeploymentSubmodelRegistrar.class);

    private final SubmodelRegistryClient submodelRegistryClient;
    private final AasRepositoryClient aasRepositoryClient;
    private final CapabilitiesConsulClient capabilitiesConsulClient;
    private final String resourceManagementBaseUrl;

    public DeploymentSubmodelRegistrar(SubmodelRegistryClient submodelRegistryClient,
                                       AasRepositoryClient aasRepositoryClient,
                                       CapabilitiesConsulClient capabilitiesConsulClient,
                                       @Value("${deployment.url}") String resourceManagementBaseUrl) {
        this.submodelRegistryClient = submodelRegistryClient;
        this.aasRepositoryClient = aasRepositoryClient;
        this.capabilitiesConsulClient = capabilitiesConsulClient;
        this.resourceManagementBaseUrl = resourceManagementBaseUrl;
    }

    /** Holt Capability-Services nach, die vor einem Neustart schon installiert waren. */
    @EventListener(ApplicationReadyEvent.class)
    public void registerExistingCapabilityServices() {
        for (var capabilityService : this.capabilitiesConsulClient.getCapabilityServices()) {
            this.register(capabilityService);
        }
    }

    public void register(CapabilityService capabilityService) {
        if (!(capabilityService.getCapability() instanceof DeploymentCapability capability)) {
            return;
        }

        var submodelId = DeploymentSubmodelTemplate.submodelIdFor(capabilityService.getServiceId());
        var idShort = DeploymentSubmodelTemplate.idShortFor(capability.getName());
        var aasId = ResourceAas.createAasIdFromResourceId(capabilityService.getResourceId());
        var submodelUrl = this.resourceManagementBaseUrl + "/aas/submodels/" + submodelId;

        try {
            this.submodelRegistryClient.registerSubmodel(submodelUrl, submodelId, idShort,
                    DeploymentSubmodelTemplate.SEMANTIC_ID_VALUE);
            this.aasRepositoryClient.addSubmodelReferenceToAas(aasId,
                    new DeploymentSubmodel(capabilityService));
            LOG.info("Registered deployment submodel '{}' for AAS '{}'", submodelId, aasId);
        } catch (Exception e) {
            LOG.error("Failed to register deployment submodel '{}': {}", submodelId, e.getMessage());
        }
    }

    public void unregister(UUID resourceId, UUID capabilityServiceId) {
        var submodelId = DeploymentSubmodelTemplate.submodelIdFor(capabilityServiceId);
        var aasId = ResourceAas.createAasIdFromResourceId(resourceId);

        try {
            this.submodelRegistryClient.unregisterSubmodel(submodelId);
            this.aasRepositoryClient.removeSubmodelReferenceFromAas(aasId, submodelId);
            LOG.info("Unregistered deployment submodel '{}'", submodelId);
        } catch (Exception e) {
            LOG.error("Failed to unregister deployment submodel '{}': {}", submodelId, e.getMessage());
        }
    }
}
