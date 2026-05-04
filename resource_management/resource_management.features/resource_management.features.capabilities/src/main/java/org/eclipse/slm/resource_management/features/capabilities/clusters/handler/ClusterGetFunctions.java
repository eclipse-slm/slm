package org.eclipse.slm.resource_management.features.capabilities.clusters.handler;

import org.eclipse.slm.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.awx.client.observer.AwxJobObserverInitializer;

import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.model.catalog.Service;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.common.vault.client.exceptions.VaultRuntimeException;
import org.eclipse.slm.notification_service.messaging.NotificationMessageSender;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessManager;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.Cluster;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
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
            ConsulClientFactory consulClientFactory,
            CapabilitiesConsulClient capabilitiesConsulClient,
            MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient,
            AwxJobObserverInitializer awxJobObserverInitializer,
            VaultClientFactory vaultClientFactory,
            RemoteAccessManager remoteAccessManager) {
        super(
                notificationMessageSender,
                awxJobExecutor,
                multiTenantKeycloakRegistration,
                consulClientFactory,
                capabilitiesConsulClient,
                multiHostCapabilitiesConsulClient,
                awxJobObserverInitializer,
                vaultClientFactory,
                remoteAccessManager);
    }

    public List<Cluster> getClusters() {
        try {
            List<Cluster> clusterList = new ArrayList<>();

            List<MultiHostCapabilityService> multiHostCapabilityServices = this.multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser();

            for (MultiHostCapabilityService multiHostCapabilityService : multiHostCapabilityServices) {
                var nodes = this.multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(multiHostCapabilityService.getId());

                // pull vault data (secrets) for cluster
                Map<String, String> secretsOfClusterFromVault = new HashMap<>();
                try {
                    secretsOfClusterFromVault = this.vaultAdminClient.kv("resources")
                            .getSecretsOfPathOrThrow(multiHostCapabilityService.getId().toString()).getData();
                } catch(VaultRuntimeException e) {
                    LOG.info("Cluster has no secrets. Error Message: " + e.getMessage());
                }

                // create cluster with data from service (consul) and vault
//                Cluster cluster = new Cluster(
//                        multiHostCapabilityService,
//                        nodes,
//                        secretsOfClusterFromVault
//                );
//
//                clusterList.add(cluster);
            }

            return clusterList;
        }
        catch (ConsulLoginFailedException e) {
            return new ArrayList<>();
        }
    }

    public List<Service> getClusterMembers(String clusterName)
            throws ConsulLoginFailedException {
        return this.multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
                
                clusterName
        );
    }
}
