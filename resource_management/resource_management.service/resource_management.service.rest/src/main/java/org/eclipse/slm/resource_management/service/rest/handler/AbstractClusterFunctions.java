package org.eclipse.slm.resource_management.service.rest.handler;

import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.eclipse.slm.resource_management.service.rest.capabilities.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.service.rest.capabilities.MultiHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.service.rest.handler.ClusterJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
class AbstractClusterFunctions {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractClusterFunctions.class);

    protected final NotificationServiceClient notificationServiceClient;

    protected final KeycloakUtil keycloakUtil;

    protected final MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;

    protected final AwxJobObserverInitializer awxJobObserverInitializer;

    protected final AwxJobExecutor awxJobExecutor;

    protected final ConsulServicesApiClient consulServicesApiClient;

    protected final ConsulAclApiClient consulAclApiClient;

    protected final ConsulNodesApiClient consulNodesApiClient;

    protected final CapabilitiesConsulClient capabilitiesConsulClient;

    protected final VaultClient vaultClient;

    protected Map<AwxJobObserver, ClusterJob> clusterJobMap = new HashMap<>();
    protected MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient;

    public AbstractClusterFunctions(NotificationServiceClient notificationServiceClient,
                                    AwxJobExecutor awxJobExecutor,
                                    MultiTenantKeycloakRegistration multiTenantKeycloakRegistration,
                                    ConsulServicesApiClient consulServicesApiClient,
                                    ConsulAclApiClient consulAclApiClient,
                                    ConsulNodesApiClient consulNodesApiClient,
                                    CapabilitiesConsulClient capabilitiesConsulClient,
                                    MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient,
                                    KeycloakUtil keycloakUtil,
                                    AwxJobObserverInitializer awxJobObserverInitializer,
                                    VaultClient vaultClient) {
        this.notificationServiceClient = notificationServiceClient;
        this.awxJobExecutor = awxJobExecutor;
        this.multiTenantKeycloakRegistration = multiTenantKeycloakRegistration;
        this.consulServicesApiClient = consulServicesApiClient;
        this.consulAclApiClient = consulAclApiClient;
        this.consulNodesApiClient = consulNodesApiClient;
        this.capabilitiesConsulClient = capabilitiesConsulClient;
        this.multiHostCapabilitiesConsulClient = multiHostCapabilitiesConsulClient;
        this.keycloakUtil = keycloakUtil;
        this.awxJobObserverInitializer = awxJobObserverInitializer;
        this.vaultClient = vaultClient;
    }
}
