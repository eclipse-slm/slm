package org.eclipse.slm.notification_service.service.app.messaging;

import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserUtils {

    private static final String DEFAULT_REALM = "fabos";

    private final static Logger LOG = LoggerFactory.getLogger(UserUtils.class);

    private final KeycloakAdminClient keycloakAdminClient;

    public UserUtils(KeycloakAdminClient keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;
    }

    public List<String> getUserIdsFromGroups(Set<String> ownerGroups) {
        if (ownerGroups == null || ownerGroups.isEmpty()) {
            return List.of();
        }
        return ownerGroups.stream()
                .map(groupPath -> {
                    try {
                        return keycloakAdminClient.getUserIdsByGroupPath(DEFAULT_REALM, groupPath);
                    } catch (Exception e) {
                        LOG.warn("Failed to resolve users for group '{}': {}", groupPath, e.getMessage());
                        return List.<String>of();
                    }
                })
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }
}
