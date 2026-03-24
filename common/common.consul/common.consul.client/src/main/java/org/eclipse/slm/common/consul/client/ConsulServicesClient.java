package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.auth.ConsulAuthentication;
import org.eclipse.slm.common.consul.model.exceptions.ConsulServiceNotFoundException;

import org.eclipse.slm.common.consul.model.catalog.*;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulNodeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Consul Services Client which provides functionality to interact with services in Consul catalog.
 */
public class ConsulServicesClient extends AbstractConsulClient {

    private final static Logger LOG = LoggerFactory.getLogger(ConsulServicesClient.class);
    public final static String CONSUL_SERVICE_NAME = "consul";
    private ConsulNodesClient consulNodesClient;

    public ConsulServicesClient(String consulUrl, ConsulAuthentication consulAuthentication, ConsulNodesClient consulNodesClient) {
        super(consulUrl, consulAuthentication);
        this.consulNodesClient = consulNodesClient;
    }

    public Map<String, List<String>> getServices(String filter) {
        var services = this.consulCatalogApiClient.listServices(this.consulDatacenter, null, filter, null);
        return services;
    }

    public Map<String, List<String>> getServices() {
        return this.getServices(null);
    }

    public Service getServiceByIdOrThrow(UUID serviceId) throws ConsulServiceNotFoundException {
        var optionalService = this.getServiceById(serviceId);

        if(optionalService.isEmpty()) {
            throw new ConsulServiceNotFoundException(serviceId);
        }

        return optionalService.get();
    }

    public Optional<Service> getServiceById(UUID serviceId) {
        var serviceIdFilter = "ServiceID == \"" + serviceId+"\"";
        var services = this.getServices(serviceIdFilter);
        var optionalServiceName = services.keySet().stream().findFirst();

        if(optionalServiceName.isPresent()) {
            Optional<List<Service>> optionalService = getServiceByName(optionalServiceName.get());

            if(optionalService.isPresent()) {
                return Optional.of(optionalService.get().get(0));
            }
        }

        return Optional.empty();
    }

    public Map<String, List<String>> getServicesByTag(String tag) {
        Map<String, List<String>> catalogServices = this.getServices();

        return catalogServices.entrySet()
                .stream()
                .filter(set ->
                        set.getValue().contains(tag)
                ).collect(Collectors.toMap(set -> set.getKey(), set -> set.getValue()));
    }

    public Map<String, List<Service>> getServicesByName(Iterable<String> serviceNames) {
        Map<String, List<Service>> services = new HashMap<>();
        for (String serviceName : serviceNames) {
            var catalogServicesOptional = this.getServiceByName(serviceName);
            catalogServicesOptional.ifPresent(catalogServices -> services.put(serviceName, catalogServices));
        }

        return services;
    }

    public Optional<List<Service>> getServiceByName(String serviceName) {
        var catalogService = this.consulCatalogApiClient.listNodesForService(serviceName, this.consulDatacenter, null, null);

        if(!catalogService.isEmpty()){
            return Optional.of(catalogService);
        }

        return Optional.empty();
    }

    public List<NodeService> getNodeServices(String nodeName) {
        var nodeServicesResponse = this.consulCatalogApiClient.listNodeServices(nodeName, null, null, null, null);
        if (nodeServicesResponse.getServices() == null) {
            return new ArrayList<>();
        } else {
            return nodeServicesResponse.getServices();
        }
    }

    public List<NodeService> getNodeServicesFilteredByServiceTag(String nodeName, String tag) {
        var filter = "\"" + tag + "\" in Tags";

        var nodeServicesResponse = this.consulCatalogApiClient.listNodeServices(nodeName, null, filter, null, null);
        var nodeServices = nodeServicesResponse.getServices();

        return nodeServices;
    }

    public List<NodeService> getNodeServicesByNodeIdAndServiceTag(UUID nodeId, String serviceTag) {
        Optional<Node> optionalNode = consulNodesClient.getNodeById(nodeId);

        if(optionalNode.isEmpty()) {
            throw new ConsulNodeNotFoundException(nodeId);
        }

        return getNodeServicesFilteredByServiceTag(optionalNode.get().getNodeName(), serviceTag);
    }

    public List<NodeService> getNodeServicesByNodeId(UUID nodeId) throws ConsulLoginFailedException {
        Optional<Node> optionalNode = consulNodesClient.getNodeById(nodeId);

        if(optionalNode.isEmpty()) {
            LOG.error("Can't find node by id = '" + nodeId + "'");
            return new ArrayList<>();
        }

        return getNodeServices(optionalNode.get().getNodeName());
    }

    public Service registerService(
            UUID nodeId,
            CatalogRegistration.Service service
    ) throws ConsulNodeNotFoundException {
        var node = this.consulNodesClient.getNodeByIdOrThrow(nodeId);
        var nodeName = node.getNodeName();

        var catalogRegistration = new CatalogRegistration.Builder()
                .skipNodeUpdate(true)
                .nodeName(nodeName)
                .service(service)
                .build();
        this.consulCatalogApiClient.registerEntity(catalogRegistration);

        var serviceId = UUID.fromString(service.getId());
        return this.getServiceByIdOrThrow(serviceId);
    }

    public void removeServiceByName(UUID nodeId, String serviceName) {
        var optionalNode = this.consulNodesClient.getNodeById(nodeId);
        if(optionalNode.isEmpty())
            return;
        var node = optionalNode.get();
        var nodeName = node.getNodeName();

        var nodeServices = this.getNodeServices(node.getNodeName());
        if (nodeServices != null ) {
            var nodeService = nodeServices
                    .stream()
                    .filter(ns -> ns.getServiceName().equals(serviceName))
                    .findFirst();

            if (nodeService.isPresent()) {
                var serviceId = UUID.fromString(nodeService.get().getId());

                var catalogDeregistration = new CatalogDeregistration(nodeName, this.consulDatacenter, null, serviceId, null, null);
                this.consulCatalogApiClient.deregisterEntity(catalogDeregistration);

                return;
            }
        }

        throw new ConsulServiceNotFoundException(nodeName, serviceName);
    }
}
