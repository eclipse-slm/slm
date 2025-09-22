package org.eclipse.slm.service_management.service.rest.service_deployment;

import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.client.observer.*;
import org.eclipse.slm.common.awx.model.ExtraVars;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.providers.ServiceHoster;
import org.eclipse.slm.resource_management.features.providers.ServiceHosterFilter;
import org.eclipse.slm.resource_management.service.client.ResourceManagementApiClientInitializer;
import org.eclipse.slm.service_management.service.rest.service_instances.ServiceInstancesConsulClient;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import javax.net.ssl.SSLException;
import java.util.UUID;

public class AbstractServiceDeploymentHandler {

    protected final ResourceManagementApiClientInitializer resourceManagementApiClientInitializer;

    protected final ServiceInstancesConsulClient serviceInstancesConsulClient;

    protected final AwxJobObserverInitializer awxJobObserverInitializer;

    protected final AwxJobExecutor awxJobExecutor;

    public AbstractServiceDeploymentHandler(ResourceManagementApiClientInitializer resourceManagementApiClientInitializer, ServiceInstancesConsulClient serviceInstancesConsulClient, AwxJobObserverInitializer awxJobObserverInitializer, AwxJobExecutor awxJobExecutor) {
        this.resourceManagementApiClientInitializer = resourceManagementApiClientInitializer;
        this.serviceInstancesConsulClient = serviceInstancesConsulClient;
        this.awxJobObserverInitializer = awxJobObserverInitializer;
        this.awxJobExecutor = awxJobExecutor;
    }

    protected AwxAction getAwxDeployCapabilityAction(ActionType actionType, Capability capability) {
        if (capability.getActions().containsKey(actionType)) {
            var capabilityAction = capability.getActions().get(actionType);

            if (capabilityAction instanceof AwxAction) {
                var awxCapabilityAction = (AwxAction) capabilityAction;
                return awxCapabilityAction;
            }
            else {
                throw new RuntimeException("'CapabilityActionType.deploy' of Deployment Capability '"
                        + capability.getName() + "' needs to be of type 'AwxCapabilityAction'");
            }
        }
        else {
            throw new RuntimeException("Deployment Capability '" + capability.getName() + "' has no 'CapabilityActionType.deploy'");
        }
    }

    protected AwxJobObserver runAwxCapabilityAction(AwxAction awxCapabilityAction,
                                                    JwtAuthenticationToken jwtAuthenticationToken,
                                                    ExtraVars extraVars,
                                                    JobGoal jobGoal,
                                                    IAwxJobObserverListener listner)
            throws SSLException {
        var jobTarget = JobTarget.SERVICE;
        var awxJobId = awxJobExecutor.executeJob(
                new AwxCredential(jwtAuthenticationToken),
                awxCapabilityAction.getAwxRepo(), awxCapabilityAction.getAwxBranch(), awxCapabilityAction.getPlaybook(),
                extraVars);
        var awxJobObserver = awxJobObserverInitializer.initNewObserver(awxJobId, jobTarget, jobGoal, listner);

        return awxJobObserver;
    }

    protected ServiceHoster getServiceHoster(
            JwtAuthenticationToken jwtAuthenticationToken, UUID deploymentCapabilityServiceId)
            throws SSLException, CapabilityServiceNotFoundException, org.eclipse.slm.resource_management.service.client.handler.ApiException {
        var resourceManagementApiClient = resourceManagementApiClientInitializer.init(jwtAuthenticationToken);
        var capabilityProvidersRestControllerApi = new org.eclipse.slm.resource_management.service.client.handler.CapabilityProvidersRestControllerApi(resourceManagementApiClient);

        var serviceHosterFilter = new ServiceHosterFilter.Builder()
                .capabilityServiceId(deploymentCapabilityServiceId)
                .build();
        var serviceHosters = capabilityProvidersRestControllerApi.getServiceHosters("fabos", serviceHosterFilter);

        if (serviceHosters.size() > 0) {
            return serviceHosters.get(0);
        }

        throw new CapabilityServiceNotFoundException(deploymentCapabilityServiceId);
    }
}
