package org.eclipse.slm.resource_management.features.capabilities.clusters.handler;

import org.eclipse.slm.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.common.consul.client.*;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.notification_service.messaging.NotificationMessageSender;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessManager;
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

    protected final MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;

    protected final AwxJobObserverInitializer awxJobObserverInitializer;

    protected final AwxJobExecutor awxJobExecutor;

    protected final ConsulClient consulAdminClient;

    protected final CapabilitiesConsulClient capabilitiesConsulClient;

    protected final VaultClient vaultAdminClient;

    protected final RemoteAccessManager remoteAccessManager;

    protected Map<AwxJobObserver, ClusterJob> clusterJobMap = new HashMap<>();
    protected MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient;

    public AbstractClusterFunctions(NotificationMessageSender notificationMessageSender,
                                    AwxJobExecutor awxJobExecutor,
                                    MultiTenantKeycloakRegistration multiTenantKeycloakRegistration,
                                    ConsulClientFactory consulClientFactory,
                                    CapabilitiesConsulClient capabilitiesConsulClient,
                                    MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient,
                                    AwxJobObserverInitializer awxJobObserverInitializer,
                                    VaultClientFactory vaultClientFactory, RemoteAccessManager remoteAccessManager) {
        this.notificationMessageSender = notificationMessageSender;
        this.awxJobExecutor = awxJobExecutor;
        this.multiTenantKeycloakRegistration = multiTenantKeycloakRegistration;
        this.consulAdminClient = consulClientFactory.createAdminClient();
        this.capabilitiesConsulClient = capabilitiesConsulClient;
        this.multiHostCapabilitiesConsulClient = multiHostCapabilitiesConsulClient;
        this.awxJobObserverInitializer = awxJobObserverInitializer;
        this.vaultAdminClient = vaultClientFactory.createAdminClient();
        this.remoteAccessManager = remoteAccessManager;
    }
}
