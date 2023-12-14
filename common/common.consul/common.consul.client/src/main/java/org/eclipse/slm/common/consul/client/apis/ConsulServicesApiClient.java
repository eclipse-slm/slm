package org.eclipse.slm.common.consul.client.apis;

import com.orbitz.consul.model.catalog.ImmutableCatalogDeregistration;
import com.orbitz.consul.model.catalog.ImmutableCatalogRegistration;
import com.orbitz.consul.model.health.ImmutableService;
import com.orbitz.consul.option.ImmutableQueryOptions;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.utils.ConsulObjectMapper;
import org.eclipse.slm.common.consul.model.catalog.*;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ConsulServicesApiClient extends AbstractConsulApiClient {

    public final static Logger LOG = LoggerFactory.getLogger(ConsulServicesApiClient.class);
    public final static String CONSUL_SERVICE_NAME = "consul";
    private ConsulNodesApiClient consulNodesApiClient;
    private ConsulAclApiClient consulAclApiClient;

    @Autowired
    public ConsulServicesApiClient(
                                   @Value("${consul.scheme}")       String consulScheme,
                                   @Value("${consul.host}")         String consulHost,
                                   @Value("${consul.port}")         int consulPort,
                                   @Value("${consul.acl-token}")    String consulToken,
                                   @Value("${consul.datacenter}")   String consulDatacenter,
                                   ConsulNodesApiClient             consulNodesApiClient,
                                   ConsulAclApiClient               consulAclApiClient) {
        super(consulScheme, consulHost, consulPort, consulToken, consulDatacenter);
        this.consulNodesApiClient = consulNodesApiClient;
        this.consulAclApiClient = consulAclApiClient;
    }

    //region GET
    public Map<String, List<String>> getServices(ConsulCredential consulCredential) {
        try {
            return this.getConsulClient(consulCredential).catalogClient().getServices().getResponse();
        } catch (ConsulLoginFailedException e) {
            LOG.warn(e.getMessage());
            return new HashMap<>();
        }
    }

    // Works only with consul version >=14
    public Optional<CatalogService> getServiceById(ConsulCredential consulCredential, UUID serviceId) throws ConsulLoginFailedException {
        var filter = "ServiceID == \"" + serviceId+"\"";
        var consulResponse = this.getConsulClient(consulCredential).catalogClient().getServices(ImmutableQueryOptions.builder().filter(filter).build()).getResponse();

        Optional<String> optionalServiceName = consulResponse.keySet().stream().findFirst();

        if(optionalServiceName.isPresent()) {
            Optional<List<CatalogService>> optionalService = getServiceByName(consulCredential, optionalServiceName.get());

            if(optionalService.isPresent()) {
                return Optional.of(
                        optionalService.get().get(0)
                );
            }
        }
        return Optional.empty();
    }

    public Map<String, List<String>> getServicesOfUserFilteredByTag(
            ConsulCredential consulCredential,
            String tag
    ) throws ConsulLoginFailedException {
        Map<String, List<String>> consulServicesOfUser = this.getServices(consulCredential);

        return consulServicesOfUser.entrySet()
                .stream()
                .filter(set ->
                        set.getValue().contains(tag)
                ).collect(Collectors.toMap(set -> set.getKey(), set -> set.getValue()));
    }

    public Map<String, List<CatalogService>> getServicesByName(ConsulCredential consulCredential, Iterable<String> serviceNames) throws ConsulLoginFailedException {
        Map<String, List<CatalogService>> services = new HashMap<>();
        for (String serviceName : serviceNames) {
            var catalogServicesOptional = this.getServiceByName(consulCredential, serviceName);
            if (catalogServicesOptional.isPresent()) {
                services.put(serviceName, catalogServicesOptional.get());
            }
        }

        return services;
    }

    public Optional<List<CatalogService>> getServiceByName(ConsulCredential consulCredential, String serviceName)
            throws ConsulLoginFailedException {
        var response = this.getConsulClient(consulCredential).catalogClient().getService(serviceName).getResponse();

        if(response.size() > 0){
            return Optional.of(ConsulObjectMapper.mapAll(response, CatalogService.class));
        }else{
            return Optional.empty();
        }
    }

    public List<NodeService> getNodeServices(ConsulCredential consulCredential, String nodeName) throws ConsulLoginFailedException {
        var response = this.getConsulClient(consulCredential).catalogClient().getNodeServices(nodeName).getResponse();
        return ConsulObjectMapper.mapAll(response.getServices(), NodeService.class);
    }

    public Optional<NodeService> getNodeServiceFilteredByServiceTag(ConsulCredential consulCredential, String nodeName, String tag) throws ConsulLoginFailedException {

        var filter = "\"" + tag + "\" in Tags";

        var queryOptions = ImmutableQueryOptions.builder().filter(filter).build();
        var consulResponse = this.getConsulClient(consulCredential).catalogClient()
                .getNodeServices(nodeName, queryOptions);

        var nodeServices = consulResponse.getResponse().getServices();
        if (nodeServices.size() == 0)
            return Optional.empty();

        return Optional.ofNullable(ConsulObjectMapper.map(nodeServices.get(0), NodeService.class));
    }

    public Optional<NodeService> getNodeServiceByNodeIdAndServiceTag(
            ConsulCredential consulCredential,
            UUID nodeId,
            String serviceTag
    ) throws ConsulLoginFailedException {
        Optional<Node> optionalNode = consulNodesApiClient.getNodeById(consulCredential, nodeId);

        if(optionalNode.isEmpty()) {
            LOG.error("Can't find node by id = '" + nodeId + "'");
            return Optional.empty();
        }

        return getNodeServiceFilteredByServiceTag(consulCredential, optionalNode.get().getNode(), serviceTag);
    }

    public List<NodeService> getNodeServicesByNodeId(ConsulCredential consulCredential, UUID nodeId) throws ConsulLoginFailedException {
        Optional<Node> optionalNode = consulNodesApiClient.getNodeById(consulCredential, nodeId);

        if(optionalNode.isEmpty()) {
            LOG.error("Can't find node by id = '" + nodeId + "'");
            return new ArrayList<>();
        }

        return getNodeServices(consulCredential, optionalNode.get().getNode());
    }
    //endregion

    //region REGISTER
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

    public Optional<CatalogService> registerService(
            ConsulCredential consulCredential,
            UUID nodeId,
            String serviceName,
            UUID serviceId,
            Optional<Integer> servicePort,
            List<String> serviceTags,
            Map<String, String> serviceMetaData
    ) throws ConsulLoginFailedException {

        var optionalNode = consulNodesApiClient.getNodeById(consulCredential, nodeId);
        var node = optionalNode.get();

        var newService = ImmutableService.builder()
                .setService(serviceName)
                .setId(serviceId.toString())
                .setMeta(serviceMetaData)
                .setTags(serviceTags)
                .setAddress("")
                .setPort(servicePort.orElse(0));

        var newCatalogRegistration = ImmutableCatalogRegistration.builder()
                .setSkipNodeUpdate(true)
                .setNode(node.getNode())
                .setService(newService.build());

        this.getConsulClient(consulCredential).catalogClient().register(newCatalogRegistration.build());

        return getServiceById(consulCredential, serviceId);
    }

    public void registerServices(
            ConsulCredential consulCredential,
            List<ConsulService> consulServices
    ) throws ConsulLoginFailedException {
        for (var service : consulServices)
        {
            this.registerServiceForNodeWithReadAccessViaKeycloakRole(
                    consulCredential,
                    service.getNodeId(),
                    service.getServiceName(),
                    service.getServiceId(),
                    Optional.empty(),
                    service.getServiceTags(),
                    service.getServiceMetaData());
        }
    }
    //endregion

//    public void removeServiceByName(ConsulCredential consulCredential, UUID resourceId, NodeService nodeService) throws ConsulLoginFailedException {
//        this.removeServiceByName(
//                consulCredential,
//                resourceId.toString(),
//                nodeService.getService()
//        );
//    }

    //region DEREGISTER
    public void removeServiceByName(
            ConsulCredential consulCredential,
            UUID nodeId,
            String serviceName
    ) throws ConsulLoginFailedException {
        var optionalNode = this.consulNodesApiClient.getNodeById(consulCredential, nodeId);
        if(optionalNode.isEmpty())
            return;
        var node = optionalNode.get();

        var nodeServices = this.getNodeServices(consulCredential, node.getNode());
        var nodeService = nodeServices
                .stream()
                .filter(ns -> ns.getService().equals(serviceName))
                .findAny();

        if (nodeService.isPresent()) {
            var catalogDeregistration = ImmutableCatalogDeregistration.builder();
            catalogDeregistration.setDatacenter(this.consulDatacenter);
            catalogDeregistration.setNode(node.getNode());
            catalogDeregistration.setServiceId(nodeService.get().getID());
            this.getConsulClient(consulCredential).catalogClient().deregister(catalogDeregistration.build());
        }
        else {
            LOG.error("Service with name '" + serviceName + "' not found for node with id '" + nodeId + "'");
        }
    }
//
//    public void removeServiceAndAclByName(
//            ConsulCredential consulCredential,
//            UUID nodeId,
//            String serviceName
//    ) throws ConsulLoginFailedException {
//        this.removePolicyOfService(consulCredential, serviceName);
//        this.removeServiceByName(consulCredential, nodeId, serviceName);
//    }
    //endregion
}
