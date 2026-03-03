package org.eclipse.slm.common.consul.testing.utils;

import org.eclipse.slm.common.consul.model.acl.authmethods.AuthMethodRequest;
import org.eclipse.slm.common.consul.model.acl.authmethods.JwtAuthMethodConfig;
import org.eclipse.slm.common.consul.model.acl.bindingrules.BindingRule;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;

import java.util.List;
import java.util.Map;

public class ConsulTestContainerInitializer {

    private final ConsulTestContainer consulContainer;

    private final boolean withKeycloakIntegration;

    private static final String JWT_AUTH_METHOD_NAME = "keycloak";

    public ConsulTestContainerInitializer(ConsulTestContainer consulContainer, boolean withKeycloakIntegration) {
        this.consulContainer = consulContainer;
        this.withKeycloakIntegration = withKeycloakIntegration;
    }

    public void initKeycloakJwtAuth(String oidcDiscoveryUrl) {
        if (!this.withKeycloakIntegration) {
            throw new IllegalArgumentException("Cannot initialize Keycloak JWT Auth when withKeycloakIntegration is false");
        }

        var consulClient = ConsulTestClientFactory.getConsulClient(this.consulContainer);

        var authMethodConfig = new JwtAuthMethodConfig(
                oidcDiscoveryUrl,
                Map.of(
                        "username", "preferred_username",
                        "email", "email",
                        "name", "name"
                ),
                Map.of(
                        "groups", "groups"
                )
        );
        var authMethodRequest = new AuthMethodRequest(
                ConsulTestContainerInitializer.JWT_AUTH_METHOD_NAME,
                "jwt",
                "Keycloak JWT Auth",
                authMethodConfig,
                "1m"
        );
        consulClient.acl().createAuthMethod(authMethodRequest);
    }

    public void initUserGroup(String fullPathUserGroupId) {
        var consulClient = ConsulTestClientFactory.getConsulClient(this.consulContainer);
        var roleName = fullPathUserGroupId;
        consulClient.acl().createRole(
                roleName,
                "Role for group '" + fullPathUserGroupId + "''",
                List.of()
        );

        if (this.withKeycloakIntegration) {
            var bindingRuleUserGroup = new BindingRule(
                    null,
                    "Binding rule of group '" + fullPathUserGroupId + "' to auth method 'keycloak'",
                    ConsulTestContainerInitializer.JWT_AUTH_METHOD_NAME,
                    "\"" + fullPathUserGroupId + "\" in list.groups",
                    "role",
                    roleName
            );
            consulClient.acl().createBindingRule(bindingRuleUserGroup);
        }
    }

}
