package org.eclipse.slm.resource_management.common.test_utils;


import org.eclipse.slm.common.consul.client.ConsulNodesClient;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {

    @Autowired
    private ConsulNodesClient consulNodesClient;

    private String consulToken = "test_secret";

    public void cleanConsul() throws ConsulLoginFailedException {

        // Delete Nodes
        var consulNodes = this.consulNodesClient.getNodes();
        for (var node : consulNodes)
        {
            this.consulNodesClient.deleteNodeByName(node.getNodeName());
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
