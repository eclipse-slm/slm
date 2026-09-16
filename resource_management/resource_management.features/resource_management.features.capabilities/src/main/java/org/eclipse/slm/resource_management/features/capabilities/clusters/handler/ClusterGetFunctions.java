package org.eclipse.slm.resource_management.features.capabilities.clusters.handler;

import org.eclipse.slm.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.awx.client.observer.AwxJobObserverInitializer;

import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.common.vault.client.exceptions.VaultRuntimeException;
import org.eclipse.slm.notification_service.messaging.NotificationMessageSender;
import org.eclipse.slm.common.access.AccessControlService;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessManager;
import org.eclipse.slm.resource_management.common.resources.ResourceJpaRepository;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.Cluster;
import org.eclipse.slm.resource_management.features.capabilities.persistence.MultiHostCapabilityServicePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ClusterGetFunctions extends AbstractClusterFunctions {
    private final static Logger LOG = LoggerFactory.getLogger(ClusterGetFunctions.class);

    public ClusterGetFunctions(
            NotificationMessageSender notificationMessageSender,
            AwxJobExecutor awxJobExecutor,
            MultiTenantKeycloakRegistration multiTenantKeycloakRegistration,
            MultiHostCapabilityServicePersistence multiHostCapabilityServicePersistence,
            ResourceJpaRepository resourceJpaRepository,
            AccessControlService accessControlService,
            AwxJobObserverInitializer awxJobObserverInitializer,
            VaultClientFactory vaultClientFactory,
            RemoteAccessManager remoteAccessManager) {
        super(
                notificationMessageSender,
                awxJobExecutor,
                multiTenantKeycloakRegistration,
                multiHostCapabilityServicePersistence,
                resourceJpaRepository,
                accessControlService,
                awxJobObserverInitializer,
                vaultClientFactory,
                remoteAccessManager);
    }

    public List<Cluster> getClusters() {
        List<Cluster> clusterList = new ArrayList<>();

        for (MultiHostCapabilityService mhcs : this.multiHostCapabilityServicePersistence.getAll()) {
            List<UUID> memberResourceIds = mhcs.getMemberMapping() == null
                    ? new ArrayList<>() : new ArrayList<>(mhcs.getMemberMapping().keySet());

            // pull vault data (secrets) for cluster
            Map<String, String> secretsOfClusterFromVault = new HashMap<>();
            try {
                secretsOfClusterFromVault = this.vaultAdminClient.kv("resources")
                        .getSecretsOfPathOrThrow(mhcs.getServiceId().toString()).getData();
            } catch (VaultRuntimeException e) {
                LOG.info("Cluster has no secrets. Error Message: " + e.getMessage());
            }

            clusterList.add(new Cluster(mhcs, memberResourceIds, secretsOfClusterFromVault));
        }

        return clusterList;
    }

    public List<UUID> getClusterMembers(UUID clusterServiceId) {
        return this.multiHostCapabilityServicePersistence.getById(clusterServiceId)
                .map(mhcs -> mhcs.getMemberMapping() == null
                        ? new ArrayList<UUID>() : new ArrayList<>(mhcs.getMemberMapping().keySet()))
                .orElse(new ArrayList<>());
    }
}
