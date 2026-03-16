package org.eclipse.slm.service_management.service.app.service_instances;


import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.model.acl.policies.Policy;
import org.eclipse.slm.common.consul.model.catalog.CatalogRegistration;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.service_management.model.services.ServiceInstance;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ServiceInstancesConsulClient {

    private final static Logger LOG = LoggerFactory.getLogger(ServiceInstancesConsulClient.class);

    private final ConsulClientFactory consulClientFactory;
    private final ConsulClient consulAdminClient;

    public static final String POLICY_SERVICE_INSTANCE_PREFIX = "service-instance_";

    public static String getServiceInstancePolicyName(UUID serviceInstanceId) {
        return POLICY_SERVICE_INSTANCE_PREFIX + serviceInstanceId;
    }

    public ServiceInstancesConsulClient(ConsulClientFactory consulClientFactory) {
        this.consulClientFactory = consulClientFactory;
        this.consulAdminClient = consulClientFactory.createAdminClient();
    }

    public void registerConsulServiceForServiceInstance(ServiceInstance serviceInstance, String fullPathOwnerGroupId) {
        // Register Consul service for service instance
        var consulService = this.convertServiceInstanceToConsulService(serviceInstance);
        this.consulAdminClient.services().registerService(serviceInstance.getResourceId(), consulService);
        // Create read access policy and assign it to role of user group of owner
        var policyName = getServiceInstancePolicyName(serviceInstance.getId());
        var policyRule =  "service \"" + consulService.getServiceName() + "\" { policy = \"read\" }";
        var policy = Policy.builder(policyName)
                .rules(policyRule)
                .build();
        var createdPolicy = this.consulAdminClient.acl().createPolicy(policy);
        this.consulAdminClient.acl().addPolicyToRole(fullPathOwnerGroupId, createdPolicy.getId());
    }

    public void updateConsulServiceForServiceInstance(ServiceInstance serviceInstance) {
        try {
            var consulService = this.convertServiceInstanceToConsulService(serviceInstance);
            this.consulAdminClient.services().registerService(serviceInstance.getResourceId(), consulService);
        } catch (ConsulLoginFailedException e) {
            LOG.error(e.getMessage());
        }
    }

    public ServiceInstance getServiceInstance(UUID serviceInstanceId) throws ConsulLoginFailedException, ServiceInstanceNotFoundException {
        var consulServiceOptional = this.consulAdminClient.services().getServiceById( serviceInstanceId);
        if (consulServiceOptional.isPresent()) {
            var serviceInstnace = ServiceInstance.Companion.ofMetaDataAndTags(
                    consulServiceOptional.get().getServiceMeta(),
                    consulServiceOptional.get().getServiceTags()
            );
            return (serviceInstnace);
        }
        throw new ServiceInstanceNotFoundException(serviceInstanceId);
    }

    public void deregisterConsulServiceForServiceInstance(ServiceInstance serviceInstance) {
        // Unregister Consul service
        var consulServiceName = this.getConsulServiceNameForServiceInstance(serviceInstance.getId());
        this.consulAdminClient.services().removeServiceByName(serviceInstance.getResourceId(), consulServiceName);
        // Remove remote access policy
        var policyName = getServiceInstancePolicyName(serviceInstance.getId());
        var policy = this.consulAdminClient.acl().getPolicyByNameOrThrow(policyName);
        this.consulAdminClient.acl().deletePolicyById(policy.getId());
    }

    public String getConsulServiceNameForServiceInstance(UUID serviceInstanceId) {
        return "service-instance_" + serviceInstanceId;
    }

    public CatalogRegistration.Service convertServiceInstanceToConsulService(ServiceInstance serviceInstance) {
        var serviceName = this.getConsulServiceNameForServiceInstance(serviceInstance.getId());
        var consulService = CatalogRegistration.Service.builder(serviceName)
                .id(serviceInstance.getId().toString())
                .meta(serviceInstance.getMetaData())
                .tags(serviceInstance.getTags())
            .build();

        return consulService;
    }
}
