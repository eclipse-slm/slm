package org.eclipse.slm.common.consul.client.apis;

import feign.Body;
import feign.Param;
import feign.RequestLine;
import org.eclipse.slm.common.consul.model.catalog.*;

import java.util.List;
import java.util.Map;

/**
 * Feign-based Consul HTTP API client for Catalog operations.
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog">Consul API docs</a>.
 */
public interface ConsulCatalogApiClient {

    /**
     * Lists all nodes in the Consul catalog with optional filters.
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#list-nodes">Consul API docs</a>.
     *
     * @param dc         Data center to query.
     * @param near       Node name to sort results by proximity.
     * @param filter     Additional filter expression.
     * @param partition  Partition to query.
     * @return List of nodes matching the criteria.
     */
    @RequestLine("GET /catalog/nodes?dc={dc}&near={near}&node-meta={nodeMeta}&filter={filter}&partition={partition}")
    List<Node> listNodes(
            @Param("dc") String dc,
            @Param("near") String near,
            @Param("filter") String filter,
            @Param("partition") String partition
    );

    /**
     * Retrieves services for a specific node in the Consul catalog.
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#list-nodes-for-service">Consul API docs</a>.
     *
     * @param nodeName            Name of the node.
     * @param dc                  Data center to query.
     * @param filter              Filter expression for services.
     * @param mergeCentralConfig  Whether to merge central configuration.
     * @param ns                  Namespace to query.
     * @return Services associated with the specified node.
     */
    @RequestLine("GET /catalog/node-services/{nodeName}?dc={dc}&filter={filter}&merge-central-config={mergeCentralConfig}&ns={ns}")
    NodeServicesResponse listNodeServices(
        @Param("nodeName") String nodeName,
        @Param("dc") String dc,
        @Param("filter") String filter,
        @Param("mergeCentralConfig") Boolean mergeCentralConfig,
        @Param("ns") String ns
    );

    /**
     * Returns the services registered in a given datacenter.
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#list-services">Consul API docs</a>.
     *
     * @param dc Specifies the datacenter to query.
     * @param ns <ENTERPRISE FEATURE> Specifies the namespace of the services you lookup.
     * @param filter Specifies the expression used to filter the queries results prior to returning the data.
     * @param partition <ENTERPRISE FEATURE> The admin partition to use. If not provided, the partition is inferred from the request's ACL token, or defaults to
     *                 the default partition.
     * @return Map of service names to their tags.
     */
    @RequestLine("GET /catalog/services?dc={dc}&ns={ns}&filter={filter}&partition={partition}")
    Map<String, List<String>> listServices(
            @Param("dc") String dc,
            @Param("ns") String ns,
            @Param("filter") String filter,
            @Param("partition") String partition
    );

    /**
     * List Nodes for Service
     * Returns the nodes providing a service in a given datacenter.
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#list-nodes-for-service">Consul API docs</a>.
     * @param serviceName Name of the service
     * @param dc Datacenter to query (optional)
     * @param ns <ENTERPRISE FEATURE> Namespace (optional)
     * @param filter Filter expression (optional)
     * @return List of nodes providing the service
     */
    @RequestLine("GET /catalog/service/{serviceName}?dc={dc}&ns={ns}&filter={filter}")
    List<Service> listNodesForService(
            @Param("serviceName") String serviceName,
            @Param("dc") String dc,
            @Param("ns") String ns,
            @Param("filter") String filter
    );

    /**
     * Registers an entity (service or check) in the Consul catalog.
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#register-entity">Consul API docs</a>.
     */
    @RequestLine("PUT /catalog/register")
    @Body("{catalogRegistration}")
    void registerEntity(CatalogRegistration catalogRegistration);

    /**
     * Deregisters an entity (service or check) from the Consul catalog.
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#deregister-entity">Consul API docs</a>.
     */
    @RequestLine("PUT /catalog/deregister")
    @Body("{catalogDeregistration}")
    void deregisterEntity(CatalogDeregistration catalogDeregistration);

}
