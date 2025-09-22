package org.eclipse.slm.resource_management.features.capabilities.clusters.handler;

import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.notification_service.messaging.NotificationMessageSender;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
class AbstractClusterFunctions {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractClusterFunctions.class);

    protected final NotificationMessageSender notificationMessageSender;

    protected final KeycloakAdminClient keycloakAdminClient;

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

    public AbstractClusterFunctions(NotificationMessageSender notificationMessageSender,
                                    AwxJobExecutor awxJobExecutor,
                                    MultiTenantKeycloakRegistration multiTenantKeycloakRegistration,
                                    ConsulServicesApiClient consulServicesApiClient,
                                    ConsulAclApiClient consulAclApiClient,
                                    ConsulNodesApiClient consulNodesApiClient,
                                    CapabilitiesConsulClient capabilitiesConsulClient,
                                    MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient,
                                    KeycloakAdminClient keycloakAdminClient,
                                    AwxJobObserverInitializer awxJobObserverInitializer,
                                    VaultClient vaultClient) {
        this.notificationMessageSender = notificationMessageSender;
        this.awxJobExecutor = awxJobExecutor;
        this.multiTenantKeycloakRegistration = multiTenantKeycloakRegistration;
        this.consulServicesApiClient = consulServicesApiClient;
        this.consulAclApiClient = consulAclApiClient;
        this.consulNodesApiClient = consulNodesApiClient;
        this.capabilitiesConsulClient = capabilitiesConsulClient;
        this.multiHostCapabilitiesConsulClient = multiHostCapabilitiesConsulClient;
        this.keycloakAdminClient = keycloakAdminClient;
        this.awxJobObserverInitializer = awxJobObserverInitializer;
        this.vaultClient = vaultClient;
    }
}
