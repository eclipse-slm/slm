package org.eclipse.slm.resource_management.service.rest.handler;

import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.common.vault.model.KvPath;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.eclipse.slm.resource_management.service.rest.capabilities.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.service.rest.capabilities.MultiHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.model.cluster.Cluster;
import org.eclipse.slm.resource_management.model.consul.capability.MultiHostCapabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

@Component
class ClusterGetFunctions extends AbstractClusterFunctions {
    private final static Logger LOG = LoggerFactory.getLogger(ClusterGetFunctions.class);

    public ClusterGetFunctions(
            NotificationServiceClient notificationServiceClient,
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
        super(
                notificationServiceClient,
                awxJobExecutor,
                multiTenantKeycloakRegistration,
                consulServicesApiClient,
                consulAclApiClient,
                consulNodesApiClient,
                capabilitiesConsulClient,
                multiHostCapabilitiesConsulClient,
                keycloakUtil,
                awxJobObserverInitializer,
                vaultClient);
    }

    public List<Cluster> getClusters(ConsulCredential consulCredential) {
        try {
            List<Cluster> clusterList = new ArrayList<>();

            List<MultiHostCapabilityService> multiHostCapabilityServices = this.multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
                    consulCredential
            );

            for (MultiHostCapabilityService multiHostCapabilityService : multiHostCapabilityServices) {
                var nodes = this.multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
                        consulCredential,
                        multiHostCapabilityService.getId()
                );

                // pull vault data (secrets) for cluster
                Map<String, String> secretsOfClusterFromVault = new HashMap<>();
                try {
                    secretsOfClusterFromVault = this.vaultClient.getKvContent(
                            new VaultCredential(),
                            new KvPath("resources", multiHostCapabilityService.getId().toString()));
                } catch(HttpClientErrorException e) {
                    LOG.info("Cluster has no secrets. Error Message: " + e.getMessage());
                }

                // create cluster with data from service (consul) and vault
                Cluster cluster = new Cluster(
                        multiHostCapabilityService,
                        nodes,
                        secretsOfClusterFromVault
                );

                clusterList.add(cluster);
            }

            return clusterList;
        }
        catch (ConsulLoginFailedException e) {
            return new ArrayList<>();
        }
    }

    public List<CatalogService> getClusterMembers(ConsulCredential consulCredential, String clusterName)
            throws ConsulLoginFailedException {
        return this.multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
                consulCredential,
                clusterName
        );
    }
}
