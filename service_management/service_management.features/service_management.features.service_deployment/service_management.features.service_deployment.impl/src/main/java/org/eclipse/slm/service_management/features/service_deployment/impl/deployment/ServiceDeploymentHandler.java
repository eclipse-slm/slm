package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.aas.submodels.deployment.DeployRequest;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentJobState;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentStatus;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrderResult;
import org.eclipse.slm.service_management.features.service_deployment.api.events.ServiceInstanceEventType;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances.ServiceInstanceEventMessageSender;
import org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances.ServiceInstancesConsulClient;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOfferingVersion;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.exceptions.InvalidServiceOfferingDefinitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Orchestriert das Deployment: rendern, Ziel pruefen, Operation aufrufen, Abschluss nachbereiten.
 * Kennt weder AWX noch Capabilities.
 */
@Component
public class ServiceDeploymentHandler {

    private final static Logger LOG = LoggerFactory.getLogger(ServiceDeploymentHandler.class);

    private final DeploymentDescriptorRenderer descriptorRenderer;
    private final DeploymentTargetHandler deploymentTargetHandler;
    private final AasDeploymentClient deploymentClient;
    private final DeploymentJobPoller deploymentJobPoller;
    private final KeycloakAdminClient keycloakAdminClient;
    private final ServiceInstancesConsulClient serviceInstancesConsulClient;
    private final ServiceInstanceEventMessageSender serviceInstanceEventMessageSender;
    private final ServiceOrderJpaRepository serviceOrderJpaRepository;

    public ServiceDeploymentHandler(DeploymentDescriptorRenderer descriptorRenderer,
                                    DeploymentTargetHandler deploymentTargetHandler,
                                    AasDeploymentClient deploymentClient,
                                    DeploymentJobPoller deploymentJobPoller,
                                    KeycloakAdminClient keycloakAdminClient,
                                    ServiceInstancesConsulClient serviceInstancesConsulClient,
                                    ServiceInstanceEventMessageSender serviceInstanceEventMessageSender,
                                    ServiceOrderJpaRepository serviceOrderJpaRepository) {
        this.descriptorRenderer = descriptorRenderer;
        this.deploymentTargetHandler = deploymentTargetHandler;
        this.deploymentClient = deploymentClient;
        this.deploymentJobPoller = deploymentJobPoller;
        this.keycloakAdminClient = keycloakAdminClient;
        this.serviceInstancesConsulClient = serviceInstancesConsulClient;
        this.serviceInstanceEventMessageSender = serviceInstanceEventMessageSender;
        this.serviceOrderJpaRepository = serviceOrderJpaRepository;
    }

    public ServiceOrder deployServiceOfferingToTarget(JwtAuthenticationToken jwtAuthenticationToken,
                                                      ServiceOfferingVersion serviceOfferingVersion,
                                                      ServiceOrder serviceOrder)
            throws JsonProcessingException, ServiceOptionNotFoundException, InvalidServiceOfferingDefinitionException {

        var serviceInstanceId = UUID.randomUUID();
        serviceOrder.setServiceInstanceId(serviceInstanceId);

        var deploymentType = serviceOfferingVersion.getDeploymentType();
        var target = this.deploymentTargetHandler
                .getDeploymentTargetOrThrow(serviceOrder.getDeploymentTargetSubmodelId());

        if (!target.supports(deploymentType)) {
            throw new IllegalArgumentException("Deployment target '" + target.displayName()
                    + "' does not support deployment type '" + deploymentType + "'");
        }

        var rendered = this.descriptorRenderer.render(serviceOfferingVersion, serviceOrder);
        var accessToken = jwtAuthenticationToken.getToken().getTokenValue();

        var deployRequest = new DeployRequest(serviceInstanceId, deploymentType,
                rendered.content(), rendered.contentType(),
                this.getCredentialReferences(serviceOfferingVersion));

        var deployResult = this.deploymentClient.deploy(target, deployRequest, accessToken);
        if (!deployResult.accepted()) {
            serviceOrder.setServiceOrderResult(ServiceOrderResult.FAILED);
            this.serviceOrderJpaRepository.save(serviceOrder);
            throw new IllegalStateException("Deployment target '" + target.displayName()
                    + "' rejected the deployment: " + deployResult.message());
        }

        serviceOrder.setDeploymentJobId(deployResult.jobId());
        this.serviceOrderJpaRepository.save(serviceOrder);

        var serviceInstance = new ServiceInstance(
                serviceInstanceId,
                new ArrayList<>(),
                rendered.serviceMetaData(),
                this.resourceIdFromTarget(target),
                null,
                serviceOfferingVersion.getServiceOffering().getId(),
                serviceOfferingVersion.getId(),
                rendered.servicePorts(),
                new ArrayList<>());

        this.deploymentJobPoller.awaitTerminalState(target, deployResult.jobId(), accessToken,
                status -> this.onDeploymentFinished(status, jwtAuthenticationToken, serviceInstance, serviceOrder));

        return serviceOrder;
    }

    /**
     * Leitet die SLM-Resource-UUID aus der AAS-Id des Ziels ab, falls es sich um eine
     * SLM-verwaltete Resource handelt. Fuer ein Fremd-Asset (keine SLM-AAS-Id) gibt es
     * keine solche UUID -- null.
     */
    private UUID resourceIdFromTarget(DeploymentTarget target) {
        if (!target.aasId().startsWith(ResourceAas.AAS_ID_PREFIX)) {
            return null;
        }
        try {
            return UUID.fromString(ResourceAas.getResourceIdFromAasId(target.aasId()));
        } catch (IllegalArgumentException e) {
            LOG.warn("AAS id '{}' looks SLM-managed but its resource id is not a valid UUID: {}",
                    target.aasId(), e.getMessage());
            return null;
        }
    }

    private void onDeploymentFinished(DeploymentStatus status,
                                      JwtAuthenticationToken jwtAuthenticationToken,
                                      ServiceInstance serviceInstance,
                                      ServiceOrder serviceOrder) {
        var userUuid = KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken);
        // TODO: Get full path owner group id from REST call that initiated the service deployment
        var fullPathOwnerGroupId = "/users/" + userUuid;

        if (status.state() == DeploymentJobState.SUCCEEDED) {
            LOG.info("Service '{}' deployed for user '{}'", serviceInstance.getId(), userUuid);

            this.keycloakAdminClient.createRealmRoleAndAssignToUser(
                    jwtAuthenticationToken.getToken().getSubject(), "service_" + serviceInstance.getId());

            if (serviceInstance.getResourceId() != null) {
                this.serviceInstancesConsulClient.registerConsulServiceForServiceInstance(
                        serviceInstance, fullPathOwnerGroupId);
            } else {
                LOG.info("Service '{}' was deployed to a foreign target with no Consul node; "
                        + "skipping Consul service registration", serviceInstance.getId());
            }

            serviceOrder.setServiceOrderResult(ServiceOrderResult.SUCCESSFULL);
            this.serviceInstanceEventMessageSender.sendMessage(serviceInstance, ServiceInstanceEventType.CREATED);
        } else {
            if (status.state() == DeploymentJobState.UNKNOWN) {
                LOG.warn("Deployment target lost track of the job for service '{}' of user '{}': {}",
                        serviceInstance.getId(), userUuid, status.message());
            } else {
                LOG.info("Service not deployed for user '{}': {}", userUuid, status.message());
            }
            serviceOrder.setServiceOrderResult(ServiceOrderResult.FAILED);
        }

        this.serviceOrderJpaRepository.save(serviceOrder);
    }

    private List<String> getCredentialReferences(ServiceOfferingVersion serviceOfferingVersion) {
        var vaultPaths = new ArrayList<String>();
        for (var serviceRepositoryId : serviceOfferingVersion.getServiceRepositories()) {
            vaultPaths.add("vendor_" + serviceOfferingVersion.getServiceOffering().getServiceVendor().getId()
                    + "/" + serviceRepositoryId);
        }
        return vaultPaths;
    }
}
