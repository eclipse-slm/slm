package org.eclipse.slm.resource_management.features.capabilities.clusters.handler;

import org.eclipse.slm.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.notification_service.messaging.NotificationMessageSender;
import org.eclipse.slm.common.access.AccessControlService;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessManager;
import org.eclipse.slm.resource_management.common.resources.ResourceJpaRepository;
import org.eclipse.slm.resource_management.features.capabilities.persistence.MultiHostCapabilityServicePersistence;
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

    protected final VaultClient vaultAdminClient;

    protected final RemoteAccessManager remoteAccessManager;

    protected final MultiHostCapabilityServicePersistence multiHostCapabilityServicePersistence;

    protected final ResourceJpaRepository resourceJpaRepository;

    protected final AccessControlService accessControlService;

    protected Map<AwxJobObserver, ClusterJob> clusterJobMap = new HashMap<>();

    public AbstractClusterFunctions(NotificationMessageSender notificationMessageSender,
                                    AwxJobExecutor awxJobExecutor,
                                    MultiTenantKeycloakRegistration multiTenantKeycloakRegistration,
                                    MultiHostCapabilityServicePersistence multiHostCapabilityServicePersistence,
                                    ResourceJpaRepository resourceJpaRepository,
                                    AccessControlService accessControlService,
                                    AwxJobObserverInitializer awxJobObserverInitializer,
                                    VaultClientFactory vaultClientFactory, RemoteAccessManager remoteAccessManager) {
        this.notificationMessageSender = notificationMessageSender;
        this.awxJobExecutor = awxJobExecutor;
        this.multiTenantKeycloakRegistration = multiTenantKeycloakRegistration;
        this.multiHostCapabilityServicePersistence = multiHostCapabilityServicePersistence;
        this.resourceJpaRepository = resourceJpaRepository;
        this.accessControlService = accessControlService;
        this.awxJobObserverInitializer = awxJobObserverInitializer;
        this.vaultAdminClient = vaultClientFactory.createAdminClient();
        this.remoteAccessManager = remoteAccessManager;
    }
}
