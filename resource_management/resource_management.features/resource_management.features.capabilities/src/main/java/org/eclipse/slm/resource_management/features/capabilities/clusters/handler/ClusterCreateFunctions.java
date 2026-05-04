package org.eclipse.slm.resource_management.features.capabilities.clusters.handler;

import org.eclipse.slm.awx.client.AwxCredential;
import org.eclipse.slm.awx.client.observer.*;
import org.eclipse.slm.awx.model.ExtraVars;

import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.model.acl.policies.Policy;
import org.eclipse.slm.common.consul.model.catalog.CatalogRegistration;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.common.vault.client.exceptions.VaultKvSecretsNotFoundException;
import org.eclipse.slm.common.vault.model.acl.GroupType;
import org.eclipse.slm.notification_service.messaging.NotificationEventMessage;
import org.eclipse.slm.notification_service.messaging.NotificationMessageSender;
import org.eclipse.slm.notification_service.model.NotificationCategory;
import org.eclipse.slm.notification_service.model.NotificationEventType;
import org.eclipse.slm.notification_service.model.NotificationSubCategory;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessManager;
import org.eclipse.slm.resource_management.features.capabilities.clusters.KubernetesKubeConfig;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterCreateRequest;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionConfigParameter;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionConfigParameterRequiredType;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.io.StringReader;
import java.util.*;

@Component
public class ClusterCreateFunctions extends AbstractClusterFunctions implements IAwxJobObserverListener {

    private final static Logger LOG = LoggerFactory.getLogger(ClusterCreateFunctions.class);

    public ClusterCreateFunctions(
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

    public ClusterJob startInstallAction(
            MultiHostCapabilityService multiHostCapabilityService,
            JwtAuthenticationToken jwtAuthenticationToken,
            ClusterCreateRequest clusterCreateRequest
    ) {
        var clusterJob = new ClusterJob(multiHostCapabilityService);

        if(clusterCreateRequest.getSkipInstall()) {
            return clusterJob;
        }

        var capability = multiHostCapabilityService.getCapability();
        var capabilityAction = (AwxAction) capability.getActions().get(ActionType.INSTALL);

        var extraVarsMap = new HashMap<String, Object>();
        String resourceId = multiHostCapabilityService.getId().toString();
        extraVarsMap.put("resource_id", resourceId);
        extraVarsMap.put("keycloak_token", jwtAuthenticationToken.getToken().getTokenValue());
        extraVarsMap.put("service_name", multiHostCapabilityService.getServiceName());
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

        return clusterJob;
    }

    public ClusterJob create(
            MultiHostCapabilityService multiHostCapabilityService,
            JwtAuthenticationToken jwtAuthenticationToken,
            ClusterCreateRequest clusterCreateRequest
    ) throws SSLException, ConsulLoginFailedException {
        multiHostCapabilityService.setStatus(CapabilityServiceStatus.INSTALL);
        this.multiHostCapabilitiesConsulClient.addMultiHostCapabilityService(
                
                multiHostCapabilityService
        );

        multiHostCapabilitiesConsulClient.addReadRuleForCapabilityServiceToResourcePolicy(
                
                multiHostCapabilityService
        );

        // initialize vault kv engine and add access for user
        var capabilityServiceId = multiHostCapabilityService.getId().toString();
        this.vaultAdminClient.kv("resources").addSecretsToKvEngine(
                capabilityServiceId,
                new HashMap<>()
        );
        LOG.info("Vault KV engine initialised");

        //region 2: now setup vault rules (for created secrets)

        // add policy for resource
        var resourceSecretsReadPolicyName = "policy_resource_" + capabilityServiceId;
        this.vaultAdminClient.acl().createOrUpdatePolicy(
                resourceSecretsReadPolicyName,
                "path \"resources/data/" + capabilityServiceId + "*\" { capabilities = [\"list\", \"read\"] }"
        );

        // add group with link to new read access policy
        var resourceSecretsReadGroupName = "group_resource_" + capabilityServiceId;
        this.vaultAdminClient.acl().createOrUpdateGroup(
                resourceSecretsReadGroupName,
                GroupType.EXTERNAL,
                Arrays.asList(resourceSecretsReadPolicyName)
        );
        var canonicalIdReadGroup = this.vaultAdminClient.acl().getGroupByName(resourceSecretsReadGroupName).getId();

        // Add group alias to link Keycloak role with read access group
        var keycloakRole = "resource_" + capabilityServiceId;
        var mountAccessor = this.vaultAdminClient.auth().getJwtAuthMethod().getAccessor();
        if (!mountAccessor.equals(""))
            this.vaultAdminClient.auth().addJwtGroupAlias(
                    keycloakRole,
                    mountAccessor,
                    canonicalIdReadGroup
            );
        else
            LOG.warn("Keycloak mount accessor not available!");

        LOG.info("Vault policy creation done");
        //endregion

        var clusterJob = this.startInstallAction(
                multiHostCapabilityService,
                jwtAuthenticationToken,
                clusterCreateRequest
        );

        clusterJob.setJwtAuthenticationToken(jwtAuthenticationToken);
        clusterJob.setClusterCreateRequest(clusterCreateRequest);

        if (!clusterCreateRequest.getSkipInstall()) {
            this.clusterJobMap.put(clusterJob.getAwxJobObserver(), clusterJob);
        }
        else {
            this.processSuccessfulClusterInstall(jwtAuthenticationToken, multiHostCapabilityService, clusterCreateRequest);
        }

        return clusterJob;
    }

    private void processSuccessfulClusterInstall(JwtAuthenticationToken jwtAuthenticationToken,
                                                 MultiHostCapabilityService multiHostCapabilityService,
                                                 ClusterCreateRequest clusterCreateRequest) {
        // Update capability status
        multiHostCapabilityService.setStatus(CapabilityServiceStatus.READY);
        this.multiHostCapabilitiesConsulClient.updateMultiHostCapabilityService(multiHostCapabilityService);
        // If a cluster is managed, read config parameters from request and write to Vault
        if (multiHostCapabilityService.getManaged()) {
            this.readConfigParametersFromRequestAndWriteToVault(clusterCreateRequest, multiHostCapabilityService);
        }
        // Reread config from Vault
        Map<String, String> secretsOfClusterFromVault = new HashMap<>();
        try {
            secretsOfClusterFromVault = this.vaultAdminClient.kv("resources").getSecretsOfPathOrThrow(multiHostCapabilityService.getId().toString()).getData();
        } catch (VaultKvSecretsNotFoundException e) {
            LOG.debug("Vault has no secrets for MultiHostCapabilityService '" + multiHostCapabilityService.getId() + "'. Proceeding ...");
        }

        // Prepare meta for consul
        var serviceMetaData = multiHostCapabilityService.getMeta();
        Set<Map.Entry<String, String>> configParametersFromClusterRequest = clusterCreateRequest.getConfigParameterValues().entrySet();
        List<ActionConfigParameter> configParametersFromCapabilityDefinition = multiHostCapabilityService.getCapability().getActions().get(ActionType.INSTALL).getConfigParameters();

        // iterate through config parameters from vault
        for (Map.Entry<String, String> secretConfigParameter : secretsOfClusterFromVault.entrySet()) {

            // if config parameter is in capability
            Optional<ActionConfigParameter> configParameterOptional = configParametersFromCapabilityDefinition.stream().filter(p -> p.getName().equals(secretConfigParameter.getKey())).findFirst();

            if (configParameterOptional.isPresent()) {
                switch (configParameterOptional.get().getValueType()) {
                    case KUBE_CONF -> {

                        LOG.info("Processing config parameter of type KUBE_CONF for cluster");

                        // add custom additional meta data
                        String rawYaml = secretConfigParameter.getValue();
                        // can only process provided kubeConfig
                        if (rawYaml.length() > 0) {
                            KubernetesKubeConfig kubernetesKubeConfig = KubernetesKubeConfig.loadKubeConfig(new StringReader(rawYaml));
                            // ToDo: Kubernetes - clarify handling of secrets which are not KUBE_CONF
                            //  serviceMetaData.put("cluster_namespace", clusterCreateRequest.getConfigParameterValues().containsKey("namespace") ? clusterCreateRequest.getConfigParameterValues().get("namespace") : "undefined");
                            //  serviceMetaData.put("cluster_address", kubernetesKubeConfig.getServer());
                            serviceMetaData.put("cluster_name", (String) kubernetesKubeConfig.getCurrentCluster().get("name"));
                            serviceMetaData.put("cluster_user", (String) kubernetesKubeConfig.getCurrentUser().get("name"));
                        }
                    }
                }
            }
        }
        multiHostCapabilityService.setMeta(serviceMetaData);

        // Create Consul node and service representing cluster
        String dummyAddress = multiHostCapabilityService.getCapability().getName().toLowerCase() + "-cluster"; // ToDo: if changed from x+"-cluster" then check for other occurrences -> e.g. in ResourcesManager
        var clusterService = CatalogRegistration.Service.builder(multiHostCapabilityService.getServiceName())
                .id(multiHostCapabilityService.getId())
                .address(dummyAddress)
                .tags(multiHostCapabilityService.getTags())
                .meta(serviceMetaData)
                .build();
        var consulNodeName = multiHostCapabilityService.getServiceName();
        Map<String, String> nodeMetaData = new HashMap<String, String>();
        nodeMetaData.put("resource_id", multiHostCapabilityService.getId().toString());
        nodeMetaData.put("resource_managed", String.valueOf(multiHostCapabilityService.getManaged()));
        nodeMetaData.put("resource_type", "cluster"); // ToDo: change to generic ResourceType when introduced & then check for other occurrences -> e.g. in ResourcesManager
        nodeMetaData.putAll(serviceMetaData);
        var catalogRegistration = CatalogRegistration.builder()
                .nodeName(consulNodeName)
                .id(multiHostCapabilityService.getId().toString())
                .address(dummyAddress)
                .nodeMeta(nodeMetaData)
                .service(clusterService)
                .build();
        this.consulAdminClient.nodes().registerEntity(catalogRegistration);
        // Create policy for resource
        var resourcePolicyName = ResourcesConsulClient.getResourcePolicyName(multiHostCapabilityService.getServiceId());
        var resourcePolicyRule =  "node \"" + multiHostCapabilityService.getId() + "\" { policy = \"read\" }";
        var resourcePolicy = Policy.builder(resourcePolicyName)
                .description("Access policy for resource '" + multiHostCapabilityService.getId() + "'")
                .rules(resourcePolicyRule)
                .build();
        var createdPolicy = this.consulAdminClient.acl().createPolicy(resourcePolicy);
        // Assign resource policy to owner group role
        var roleName = clusterCreateRequest.getFullPathOwnerGroupId();
        this.consulAdminClient.acl().addPolicyToRole(roleName, createdPolicy.getId());
        // Send notification
        this.notificationMessageSender.sendMessage(new NotificationEventMessage(
                KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken),
                NotificationCategory.RESOURCES, NotificationSubCategory.CLUSTER, NotificationEventType.CREATED,
                null
        ));
    }

    private void readConfigParametersFromRequestAndWriteToVault(ClusterCreateRequest clusterCreateRequest, MultiHostCapabilityService multiHostCapabilityService) {
        var configParametersForVault = new HashMap<String, String>();

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
                LOG.error("Config parameter of CreateRequest with key '" + configParameterValueEntry.getKey()
                        + "' is not in the config parameters of capability '" + multiHostCapabilityService.getServiceName() + "'");
            }
        }

        // add cluster config parameters to vault, if present
        if (configParametersForVault.size() > 0) {

            // add secrets as kv pairs
            this.vaultAdminClient.kv("resources").addSecretsToKvEngine(
                    multiHostCapabilityService.getId().toString(),
                    configParametersForVault
            );
            LOG.info("Config parameters have been saved to vault");
        }
    }

    //region IAwxJobObserverListener
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
    //endregion IAwxJobObserverListener
}
