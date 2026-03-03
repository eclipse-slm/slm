package org.eclipse.slm.common.consul.client.apis;

import feign.Param;
import feign.RequestLine;
import org.eclipse.slm.common.consul.model.health.Check;

import java.util.List;

/**
 * Feign-based Consul HTTP API client for Health Checks.
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/health">Consul API docs</a>.
 */
public interface ConsulHealthApiClient {

    /**
     * List health checks for a given node
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/health#list-checks-for-node">Consul API docs</a>.
     * @param node The node to list the health checks for
     * @param filter An optional filter expression
     * @param dc An optional datacenter
     * @param ns <ENTERPRISE FEATURE> An optional namespace
     * @return The list of health checks for the given node
     */
    @RequestLine("GET /health/node/{node}?filter={filter}&dc={dc}&ns={ns}")
    List<Check> listChecksForNode(
            @Param("node") String node,
            @Param("filter") String filter,
            @Param("dc") String dc,
            @Param("ns") String ns
    );

}
