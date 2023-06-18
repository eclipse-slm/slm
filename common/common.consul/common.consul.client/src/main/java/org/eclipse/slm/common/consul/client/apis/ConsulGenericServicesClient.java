package org.eclipse.slm.common.consul.client.apis;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ConsulGenericServicesClient {
    private final static Logger LOG = LoggerFactory.getLogger(ConsulGenericServicesClient.class);
    private final ConsulHealthApiClient consulHealthApiClient;
    private final ConsulServicesApiClient consulServicesApiClient;
    private final ConsulAgentApiClient consulAgentApiClient;
    private ConsulAclApiClient consulAclApiClient;

    public ConsulGenericServicesClient(
            ConsulHealthApiClient consulHealthApiClient,
            ConsulServicesApiClient consulServicesApiClient,
            ConsulAgentApiClient consulAgentApiClient,
            ConsulAclApiClient consulAclApiClient
    ) {
        this.consulHealthApiClient = consulHealthApiClient;
        this.consulServicesApiClient = consulServicesApiClient;
        this.consulAgentApiClient = consulAgentApiClient;
        this.consulAclApiClient = consulAclApiClient;
    }

    //region REGISTER
    public Optional<CatalogService> registerService(
            ConsulCredential consulCredential,
            UUID nodeId,
            String serviceName,
            UUID serviceId,
            Optional<Integer> servicePort,
            List<String> serviceTags,
            Map<String, String> serviceMetaData
    ) throws ConsulLoginFailedException {
        if(consulHealthApiClient.hasNodeAgent(consulCredential, nodeId)) {
            consulAgentApiClient.registerService(
                    consulCredential,
                    nodeId,
                    serviceName,
                    serviceId,
                    servicePort,
                    serviceTags,
                    serviceMetaData
            );
        } else {
            consulServicesApiClient.registerService(
                    consulCredential,
                    nodeId,
                    serviceName,
                    serviceId,
                    servicePort,
                    serviceTags,
                    serviceMetaData
            );
        }

        return consulServicesApiClient.getServiceById(consulCredential, serviceId);
    }

    public void registerServiceForNodeWithReadAccessViaKeycloakRole(
            ConsulCredential consulCredential,
            UUID nodeId,
            String serviceName,
            UUID serviceId,
            Optional<Integer> servicePort,
            List<String> serviceTags,
            Map<String, String> serviceMetaData
    ) throws ConsulLoginFailedException {
        this.registerService(consulCredential, nodeId, serviceName, serviceId, servicePort, serviceTags, serviceMetaData);
        this.consulAclApiClient.addReadAccessViaKeycloakRole(serviceId, serviceName, "service");
    }
    //endregion

    //region DEREGISTER
    public void deregisterService(
            ConsulCredential consulCredential,
            UUID nodeId,
            String serviceName
    ) throws ConsulLoginFailedException {
        if(consulHealthApiClient.hasNodeAgent(consulCredential, nodeId)) {
                consulAgentApiClient.removeServiceByName(
                        consulCredential,
                        nodeId,
                        serviceName
                );
        } else {
            consulServicesApiClient.removeServiceByName(
                    consulCredential,
                    nodeId,
                    serviceName
            );
        }
    }


    public void deregisterServiceAndPolicy(
            ConsulCredential consulCredential,
            UUID nodeId,
            String serviceName
    ) throws ConsulLoginFailedException {
        this.removePolicyOfService(consulCredential, serviceName);
        this.deregisterService(consulCredential, nodeId, serviceName);
    }
    //endregion

    //region POLICY
    private void removePolicyOfService(ConsulCredential consulCredential, String serviceName) throws ConsulLoginFailedException {
        var policy = this.consulAclApiClient.getPolicyByName(consulCredential, serviceName);
        if (policy != null) {
            this.consulAclApiClient.deletePolicyById(consulCredential, policy.getId());
        }

        var role = this.consulAclApiClient.getRoleByName(consulCredential, serviceName);
        if (role != null) {
            this.consulAclApiClient.deleteRoleById(consulCredential, role.getId());
        }

        var bindingRules = this.consulAclApiClient.getBindingRules(consulCredential);
        var bindingRulesOfService = bindingRules.stream().filter(r -> r.getBindName().equals(serviceName)).collect(Collectors.toList());
        if (bindingRulesOfService.size() > 0)
        {
            for (var bindingRule : bindingRulesOfService)
            {
                this.consulAclApiClient.deleteBindingRuleById(consulCredential, bindingRule.getId());
            }
        }
    }
    //endregion

}
