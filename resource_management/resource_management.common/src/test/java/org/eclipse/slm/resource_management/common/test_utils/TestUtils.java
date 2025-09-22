package org.eclipse.slm.resource_management.common.test_utils;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {

    @Autowired
    private ConsulNodesApiClient consulNodesApiClient;

    private String consulToken = "test_secret";

    public void cleanConsul() throws ConsulLoginFailedException {

        var consulCredential = new ConsulCredential(this.consulToken);

        // Delete Nodes
        var consulNodes = this.consulNodesApiClient.getNodes(consulCredential);
        for (var node : consulNodes)
        {
            this.consulNodesApiClient.deleteNode(consulCredential, node.getNode());
        }

        // Delete Policies

        // Delete roles

        // Delete Binding Rules
    }

    public void cleanKeycloak()
    {
        throw new NotImplementedException();
    }

    public void cleanVault()
    {
        throw new NotImplementedException();
    }

}
