package org.eclipse.slm.resource_management.service.rest.handler;

import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.awx.client.observer.IAwxJobObserverListener;
import org.eclipse.slm.common.awx.model.ExtraVars;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulKeyValueApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.catalog.CatalogNode;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.common.vault.model.KvPath;
import org.eclipse.slm.notification_service.model.*;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.eclipse.slm.resource_management.model.actions.AwxAction;
import org.eclipse.slm.resource_management.model.actions.ActionConfigParameter;
import org.eclipse.slm.resource_management.model.actions.ActionConfigParameterRequiredType;
import org.eclipse.slm.resource_management.model.actions.ActionType;
import org.eclipse.slm.resource_management.service.rest.capabilities.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.service.rest.capabilities.MultiHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.model.cluster.ClusterCreateRequest;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.model.consul.capability.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.model.resource.ConnectionType;
import org.eclipse.slm.resource_management.model.utils.KubernetesKubeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.net.ssl.SSLException;
import java.io.StringReader;
import java.util.*;

@Component
class ClusterCreateFunctions extends AbstractClusterFunctions implements IAwxJobObserverListener {

    private final static Logger LOG = LoggerFactory.getLogger(ClusterCreateFunctions.class);
    private final ConsulKeyValueApiClient consulKeyValueApiClient;

    public ClusterCreateFunctions(
            NotificationServiceClient notificationServiceClient,
            AwxJobExecutor awxJobExecutor,
            MultiTenantKeycloakRegistration multiTenantKeycloakRegistration,
            ConsulServicesApiClient consulServicesApiClient,
            ConsulAclApiClient consulAclApiClient,
            ConsulNodesApiClient consulNodesApiClient,
            ConsulKeyValueApiClient consulKeyValueApiClient,
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

        this.consulKeyValueApiClient = consulKeyValueApiClient;
    }

    public ClusterJob startInstallAction(
            MultiHostCapabilityService multiHostCapabilityService,
            JwtAuthenticationToken jwtAuthenticationToken,
            ClusterCreateRequest clusterCreateRequest
    ) throws SSLException {

        var clusterJob = new ClusterJob(multiHostCapabilityService);

        if(clusterCreateRequest.getSkipInstall())
            return clusterJob;


        var capability = multiHostCapabilityService.getCapability();
        var capabilityAction = (AwxAction) capability.getActions().get(ActionType.INSTALL);

        var extraVarsMap = new HashMap<String, Object>();
        String resourceId = multiHostCapabilityService.getId().toString();
        extraVarsMap.put("resource_id", resourceId);
        extraVarsMap.put("keycloak_token", jwtAuthenticationToken.getToken().getTokenValue());
        extraVarsMap.put("service_name", multiHostCapabilityService.getService());
        extraVarsMap.put("supported_connection_types", capabilityAction.getConnectionTypes());
        ExtraVars extraVars = new ExtraVars(extraVarsMap);

        var awxJobId = awxJobExecutor.executeJob(
                new AwxCredential(jwtAuthenticationToken),
                capabilityAction.getAwxRepo(),
                capabilityAction.getAwxBranch(),
                capabilityAction.getPlaybook(),
                extraVars
        );

        var awxJobObserver = awxJobObserverInitializer.initNewObserver(
                awxJobId,
                JobTarget.RESOURCE,
                JobGoal.CREATE,
                this
        );

        clusterJob.setAwxJobObserver(awxJobObserver);

//            // adds empty kubeconfig as a default // ToDo: check if necessary, if so add switch for DeploymentCapability
//            Map<String, String> secretsOfResource = new HashMap<>();
//            secretsOfResource.put("kubeconfig", "null");
//            // add resource kv
//            KvPath resourceVaultPath = new KvPath("resources", resourceId);
//            this.vaultClient.addSecretToKvEngine(
//                    vaultCredential,
//                    resourceVaultPath.getSecretEngine(),
//                    resourceVaultPath.getPath(),
//                    secretsOfResource
//            );

        // Add read access for secret to awx policy
        VaultCredential vaultCredential = new VaultCredential();
        this.vaultClient.addRuleToPolicy(
                vaultCredential,
                "awx",
                "path \"resources/data/"+ resourceId + "\" { capabilities = [\"create\", \"read\", \"list\", \"update\"] }"
        );

        return clusterJob;
    }

    public ClusterJob create(
            MultiHostCapabilityService multiHostCapabilityService,
            JwtAuthenticationToken jwtAuthenticationToken,
            ClusterCreateRequest clusterCreateRequest
    ) throws SSLException, ConsulLoginFailedException {
        multiHostCapabilityService.setStatus(CapabilityServiceStatus.INSTALL);
        this.multiHostCapabilitiesConsulClient.addMultiHostCapabilityService(
                new ConsulCredential(),
                multiHostCapabilityService
        );

        multiHostCapabilitiesConsulClient.addReadRuleForCapabilityServiceToResourcePolicy(
                new ConsulCredential(),
                multiHostCapabilityService
        );

        // Create Keycloak role to allow user access the cluster
        this.keycloakUtil.createRealmRoleAndAssignToUser(jwtAuthenticationToken, multiHostCapabilityService.getService());
        this.keycloakUtil.createRealmRoleAndAssignToUser(jwtAuthenticationToken, "resource_" + multiHostCapabilityService.getId());

        // initialize vault kv engine and add access for user
        this.vaultClient.initKvEngineAndAddAccessForService(multiHostCapabilityService.getId().toString());

        var clusterJob = this.startInstallAction(
                multiHostCapabilityService,
                jwtAuthenticationToken,
                clusterCreateRequest
        );

        clusterJob.setJwtAuthenticationToken(jwtAuthenticationToken);
        clusterJob.setClusterCreateRequest(clusterCreateRequest);

        if (!clusterCreateRequest.getSkipInstall()) {
            this.clusterJobMap.put(clusterJob.getAwxJobObserver(), clusterJob);
            this.notificationServiceClient.postJobObserver(jwtAuthenticationToken, clusterJob.getAwxJobObserver());
        }
        else {
            this.processSuccessfulClusterInstall(jwtAuthenticationToken, multiHostCapabilityService, clusterCreateRequest);
        }

        return clusterJob;
    }

    private void processSuccessfulClusterInstall(
            JwtAuthenticationToken jwtAuthenticationToken,
            MultiHostCapabilityService multiHostCapabilityService,
            ClusterCreateRequest clusterCreateRequest
    ) throws ConsulLoginFailedException {
        UUID mhcsId = multiHostCapabilityService.getId();
        LOG.info("Started processing of successful cluster install for mhcs with id '"+mhcsId+"' of capability with name '"+multiHostCapabilityService.getCapability().getName()+"'");

        multiHostCapabilityService.setStatus(CapabilityServiceStatus.READY);
        multiHostCapabilitiesConsulClient.updateMultiHostCapabilityService(
                new ConsulCredential(),
                multiHostCapabilityService
        );

        try {
            Set<ConnectionType> connectionTypes = multiHostCapabilityService.getCapability().getConnectionTypes();

            var configParametersForVault = new HashMap<String, String>();

            //region 1: if a cluster is managed, read config parameters from request and write to vault
            if (multiHostCapabilityService.getManaged()) {

                // Parse config parameter from request to evaluate if values should be pushed to vault
                List<ActionConfigParameter> configParametersFromCapabilityDefinition = multiHostCapabilityService.getCapability().getActions().get(ActionType.INSTALL).getConfigParameters();

                for (var configParameterValueEntry : clusterCreateRequest.getConfigParameterValues().entrySet()) {

                    // check if parameter in request is in capability definition
                    Optional<ActionConfigParameter> configParameterOptional = configParametersFromCapabilityDefinition.stream().filter(p -> p.getName().equals(configParameterValueEntry.getKey())).findFirst();
                    if (configParameterOptional.isPresent()) {

                        // ToDo: Kubernetes - config parameter durchgehen
                        // if secret = false => consul + policy
                        // if secret = true => vault + policy

                        // check for RequiredType (if always -> add always to vault; if skip -> only add if action is skipped)
                        ActionConfigParameterRequiredType actionConfigParameterRequiredType = configParameterOptional.get().getRequiredType();
                        if (actionConfigParameterRequiredType.equals(ActionConfigParameterRequiredType.ALWAYS)) {
                            configParametersForVault.put(configParameterValueEntry.getKey(), configParameterValueEntry.getValue());
                            LOG.info("Added config parameter with key '" + configParameterValueEntry.getKey() + "' (RequiredType: ALWAYS)");
                        } else if (actionConfigParameterRequiredType.equals(ActionConfigParameterRequiredType.SKIP) && clusterCreateRequest.getSkipInstall()) {
                            configParametersForVault.put(configParameterValueEntry.getKey(), configParameterValueEntry.getValue());
                            LOG.info("Added config parameter with key '" + configParameterValueEntry.getKey() + "' (RequiredType: SKIP)");
                        }

                    } else {
                        LOG.error("Config parameter of CreateRequest with key '" + configParameterValueEntry.getKey() + "' is not in the config parameters of capability '" + multiHostCapabilityService.getService() + "'");
                    }
                }

                // add cluster config parameters to vault, if present
                if (configParametersForVault.size() > 0) {

                    // add secrets as kv pairs
                    KvPath resourceVaultPath = new KvPath("resources", mhcsId.toString());
                    this.vaultClient.addSecretToKvEngine(
                            new VaultCredential(),
                            resourceVaultPath.getSecretEngine(),
                            resourceVaultPath.getPath(),
                            configParametersForVault
                    );
                    LOG.info("Config parameters have been saved to vault");
                }
            }
            //endregion


            //region 3: now reread config

            Map<String, String> secretsOfClusterFromVault = Collections.emptyMap();
            try {
                secretsOfClusterFromVault = this.vaultClient.getKvContent(
                        new VaultCredential(),
                        new KvPath("resources", mhcsId.toString())
                );
                LOG.info("Fetched '"+secretsOfClusterFromVault.size()+"' secrets from vault for cluster");
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)){
                    LOG.info("Vault has no secrets for resource '" + multiHostCapabilityService.getId() + "'. Proceeding ...");
                } else {
                    throw e;
                }
            }
            //endregion


            //region 4. prepare meta for consul
            Map<String, String> serviceMetaData = multiHostCapabilityService.getServiceMeta();
            Set<Map.Entry<String, String>> configParametersFromClusterRequest = clusterCreateRequest.getConfigParameterValues().entrySet();
            List<ActionConfigParameter> configParametersFromCapabilityDefinition = multiHostCapabilityService.getCapability().getActions().get(ActionType.INSTALL).getConfigParameters();

            // iterate through config parameters from vault
            for(Map.Entry<String, String> secretConfigParameter : secretsOfClusterFromVault.entrySet()){

                // if config parameter is in capability
                Optional<ActionConfigParameter> configParameterOptional = configParametersFromCapabilityDefinition.stream().filter(p -> p.getName().equals(secretConfigParameter.getKey())).findFirst();

                if (configParameterOptional.isPresent()) {
                    switch (configParameterOptional.get().getValueType()){
                        case KUBE_CONF -> {

                            LOG.info("Processing config parameter of type KUBE_CONF for cluster");

                            // add custom additional meta data
                            String rawYaml = secretConfigParameter.getValue();
                            // can only process provided kubeConfig
                            if (rawYaml.length() > 0) {
                                KubernetesKubeConfig kubernetesKubeConfig = KubernetesKubeConfig.loadKubeConfig(new StringReader(rawYaml));
                                // ToDo: Kubernetes - clarify handling of secrets which are not KUBE_CONF
//                                 serviceMetaData.put("cluster_namespace", clusterCreateRequest.getConfigParameterValues().containsKey("namespace") ? clusterCreateRequest.getConfigParameterValues().get("namespace") : "undefined");
//                                serviceMetaData.put("cluster_address", kubernetesKubeConfig.getServer());
                                serviceMetaData.put("cluster_name", (String) kubernetesKubeConfig.getCurrentCluster().get("name"));
                                serviceMetaData.put("cluster_user", (String) kubernetesKubeConfig.getCurrentUser().get("name"));
                            }
                        }
                    }
                }
            }
            multiHostCapabilityService.setServiceMeta(serviceMetaData);
            //endregion


            //region 5. generic add cluster to consul

            //add Consul KV path for multi-host capability service
            this.consulKeyValueApiClient.createKey(
                    new ConsulCredential(),
                    mhcsId + "/connectionTypes",
                    connectionTypes
            );


            // create consul node representing cluster
            String dummyAddress = multiHostCapabilityService.getCapability().getName().toLowerCase() + "-cluster"; // ToDo: if changed from x+"-cluster" then check for other occurrences -> e.g. in ResourcesManager
            var catalogNode = new CatalogNode();
            catalogNode.setId(multiHostCapabilityService.getId());
            catalogNode.setNode(multiHostCapabilityService.getService().toString());
            catalogNode.setAddress(dummyAddress.toString());
            Map<String, String> nodeMetaData = new HashMap<String, String>();
            nodeMetaData.put("resource_id", multiHostCapabilityService.getId().toString());
            nodeMetaData.put("resource_managed", String.valueOf(multiHostCapabilityService.getManaged()));
            nodeMetaData.put("resource_type", "cluster"); // ToDo: change to generic ResourceType when introduced & then check for other occurrences -> e.g. in ResourcesManager
            nodeMetaData.putAll(serviceMetaData);
            catalogNode.setNodeMeta(nodeMetaData);

            // create consul service, for the cluster node
            var clusterService = new CatalogNode.Service(multiHostCapabilityService.getId());
            clusterService.setAddress(catalogNode.getAddress());
            clusterService.setService(multiHostCapabilityService.getService());
            clusterService.setService(multiHostCapabilityService.getService().toString());
            clusterService.setAddress(dummyAddress.toString());
            clusterService.setTags(multiHostCapabilityService.getTags());

            // now inject the provided and custom added meta-data
            clusterService.setServiceMeta(serviceMetaData);
            catalogNode.setService(clusterService);

            this.consulNodesApiClient.registerNode(new ConsulCredential(), catalogNode);
            LOG.info("Registered consul node for cluster");

            this.consulAclApiClient.addReadAccessViaKeycloakRole(
                    multiHostCapabilityService.getId(),
                    "resource_" + multiHostCapabilityService.getId(),
                    "node");
            this.consulAclApiClient.addReadRuleToPolicy(
                    new ConsulCredential(),
                    "resource_" + multiHostCapabilityService.getId(),
                    "service",
                    multiHostCapabilityService.getService());
            LOG.info("Added access to consul node for cluster");
            //endregion

        } catch (ConsulLoginFailedException e) {
            LOG.error(e.getMessage());
        }

        this.notificationServiceClient.postNotification(
                jwtAuthenticationToken,
                Category.RESOURCES,
                JobTarget.RESOURCE,
                JobGoal.CREATE
        );
    }

    @Override
    public void onJobStateChanged(AwxJobObserver sender, JobState newState) {}

    @Override
    public void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState) {
        LOG.info("Job on cluster finished.");
        var jobGoal = sender.jobGoal;
        var clusterJob = this.clusterJobMap.get(sender);
        var jwtAuthenticationToken = clusterJob.getJwtAuthenticationToken();
        var multiHostCapabilityService = clusterJob.getMultiHostCapabilityService();
        var clusterCreateRequest = clusterJob.getClusterCreateRequest();

        if (finalState.equals(JobFinalState.SUCCESSFUL)) {
            if (jobGoal.equals(JobGoal.CREATE)) {
                try {
                    this.processSuccessfulClusterInstall(
                            jwtAuthenticationToken,
                            multiHostCapabilityService,
                            clusterCreateRequest
                    );
                } catch (ConsulLoginFailedException e) {
                    throw new RuntimeException(e);
                }
                this.clusterJobMap.remove(sender);
            }
        }
        else {
            LOG.warn("Job [id=" + sender.jobId + "] finished not successful ('" + finalState.name() + "')");
            this.clusterJobMap.remove(sender);
        }
    }
}
