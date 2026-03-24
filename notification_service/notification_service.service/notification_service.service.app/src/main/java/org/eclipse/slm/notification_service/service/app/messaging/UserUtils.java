package org.eclipse.slm.notification_service.service.app.messaging;

import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserUtils {

    private final static Logger LOG = LoggerFactory.getLogger(UserUtils.class);

    private final ConsulClientFactory consulClientFactory;

    public UserUtils(ConsulClientFactory consulClientFactory) {
        this.consulClientFactory = consulClientFactory;
    }

    public List<String> getUserIdsAssociatedToPolicy(String policyName) {
        var userIdsAssociatedToPolicies = new ArrayList<String>();

        var consulClient = this.consulClientFactory.createAdminClient();

        var allConsulRoles = consulClient.acl().getRoles();
        for (var role : allConsulRoles) {
            if (role.getPolicies() != null) {
                var optionalPolicyLink = role.getPolicies().stream().filter(policyLink -> policyLink.getName().equals(policyName)).findFirst();
                if (optionalPolicyLink.isPresent()) {
                    var userId = role.getName().substring("users_".length());
                    userIdsAssociatedToPolicies.add(userId);
                }
            }
        }

        return userIdsAssociatedToPolicies;
    }
}
