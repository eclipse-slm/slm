package org.eclipse.slm.resource_management.service.rest.capabilities;

import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityService;
import org.keycloak.KeycloakPrincipal;

import java.util.UUID;

public class CapabilityJob {
    private UUID resourceId;
    private KeycloakPrincipal keycloakPrincipal;
    private AwxCredential awxCredential;

    protected ConsulCredential consulCredential;
    private CapabilityService capabilityService;

    public CapabilityJob(UUID resourceId, KeycloakPrincipal keycloakPrincipal, CapabilityService capabilityService) {
        this.resourceId = resourceId;
        this.keycloakPrincipal = keycloakPrincipal;
        this.capabilityService = capabilityService;
    }

    public CapabilityJob(UUID resourceId, AwxCredential awxCredential, ConsulCredential consulCredential, CapabilityService capabilityService) {
        this.resourceId = resourceId;
        this.awxCredential = awxCredential;
        this.consulCredential = consulCredential;
        this.capabilityService = capabilityService;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public KeycloakPrincipal getKeycloakPrincipal() {
        return keycloakPrincipal;
    }

    public CapabilityService getCapabilityService() {
        return capabilityService;
    }
}
