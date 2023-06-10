package org.eclipse.slm.common.consul.client.apis;

import com.orbitz.consul.model.agent.ImmutableCheckDefinition;
import com.orbitz.consul.model.agent.ImmutableCheckV2;
import com.orbitz.consul.model.catalog.ImmutableCatalogDeregistration;
import com.orbitz.consul.model.catalog.ImmutableCatalogRegistration;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.utils.ConsulObjectMapper;
import org.eclipse.slm.common.consul.model.catalog.CatalogNode;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConsulHealthApiClient extends AbstractConsulApiClient {

    public final static Logger LOG = LoggerFactory.getLogger(ConsulHealthApiClient.class);
    public final static String CHECK_ID_SERF = "serfHealth";
    public final static String CHECK_STATUS_PASSING = "passing";
    private final ConsulNodesApiClient consulNodesApiClient;
    private final ConsulServicesApiClient consulServicesApiClient;

    public ConsulHealthApiClient(
            @Value("${consul.scheme}") String consulScheme,
            @Value("${consul.host}") String consulHost,
            @Value("${consul.port}") int consulPort,
            @Value("${consul.acl-token}") String consulToken,
            @Value("${consul.datacenter}") String consulDatacenter,
            ConsulNodesApiClient consulNodesApiClient,
            ConsulServicesApiClient consulServicesApiClient) {
        super(consulScheme, consulHost, consulPort, consulToken, consulDatacenter);
        this.consulNodesApiClient = consulNodesApiClient;
        this.consulServicesApiClient = consulServicesApiClient;

    }

    public String getStatusOfServiceCheck(ConsulCredential consulCredential, String serviceName, String checkId)
            throws ConsulLoginFailedException {
        var services = this.getConsulClient(consulCredential).healthClient().getHealthyServiceInstances(serviceName).getResponse();
        var service = services.get(0);
        var checkOptional = service.getChecks().stream().filter(c -> c.getCheckId().equals(checkId)).findFirst();

        if(checkOptional.isPresent()){
            return checkOptional.get().getStatus();
        }

        return null;//healthServiceResponse.getBody().get;
    }

    public void addCheckForService(ConsulCredential consulCredential, String node, UUID serviceId, CatalogNode.Check check)
            throws ConsulLoginFailedException {
        var check2 = ImmutableCheckV2.builder()
                .setId(check.getCheckId())
                .setName(check.getName())
                .setDefinition(ImmutableCheckDefinition.builder()
                        .setHttp(check.getDefinition().getHttp())
                        .setInterval(check.getDefinition().getInterval())
                        .build());
        var registration = ImmutableCatalogRegistration.builder()
                .setSkipNodeUpdate(true)
                .setNode(node).setCheck(check2.build());

        this.getConsulClient(consulCredential).catalogClient().register(registration.build());
    }

    public List<CatalogNode.Check> getChecksOfNode(ConsulCredential consulCredential, UUID nodeId)
            throws ConsulLoginFailedException {
        var node = this.consulNodesApiClient.getNodeById(consulCredential, nodeId);
        if(node.isPresent()){
            return getChecksOfNode(consulCredential, node.get().getNode());
        }
        return new ArrayList<CatalogNode.Check>();
    }

    public List<CatalogNode.Check> getChecksOfNode(ConsulCredential consulCredential, String node)
            throws ConsulLoginFailedException {
        var nodeChecks = this.getConsulClient(consulCredential).healthClient().getNodeChecks(node).getResponse();
        return ConsulObjectMapper.mapAll(nodeChecks, CatalogNode.Check.class);
    }

    public Optional<CatalogNode.Check> getSerfHealthCheckOfNode(ConsulCredential consulCredential, UUID nodeId) throws ConsulLoginFailedException {
        List<CatalogNode.Check> checks = getChecksOfNode(consulCredential, nodeId);

        return checks.stream().filter(check -> check.getCheckId().equals(CHECK_ID_SERF)).findFirst();
    }

    public Boolean hasNodeAgent(ConsulCredential consulCredential, UUID nodeId) throws ConsulLoginFailedException {
        Optional<CatalogNode.Check> optionalSerfCheck =
                getSerfHealthCheckOfNode(consulCredential, nodeId);

        if(optionalSerfCheck.isEmpty() || !optionalSerfCheck.get().getStatus().equals(CHECK_STATUS_PASSING))
            return false;
        else
            return true;
    }

    public void removeCheckFromNode(ConsulCredential consulCredential, String nodeName, String checkId) throws ConsulLoginFailedException {
        var deregister = ImmutableCatalogDeregistration.builder().setNode(nodeName)
                .setCheckId(checkId);
        this.getConsulClient(consulCredential).catalogClient().deregister(deregister.build());
    }

    public void removeCheckFromNode(ConsulCredential consulCredential, UUID nodeId, String checkId)
            throws ConsulLoginFailedException {
        var node = this.consulNodesApiClient.getNodeById(consulCredential, nodeId);
        if (node.isPresent()){
            removeCheckFromNode(consulCredential, node.get().getNode(), checkId);
        }
    }
}
