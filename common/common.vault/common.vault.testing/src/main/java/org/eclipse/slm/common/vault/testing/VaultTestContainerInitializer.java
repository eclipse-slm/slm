package org.eclipse.slm.common.vault.testing;


import org.eclipse.slm.common.vault.model.acl.GroupType;

import java.util.List;

public class VaultTestContainerInitializer {

    private final VaultTestContainer vaultContainer;

    private final boolean withKeycloakIntegration;

    public VaultTestContainerInitializer(VaultTestContainer vaultContainer, boolean withKeycloakIntegration) {
        this.vaultContainer = vaultContainer;
        this.withKeycloakIntegration = withKeycloakIntegration;
    }

    public void initKeycloakJwtAuth(String oidcDiscoveryUrl, String oidcClientId) {
        if (!this.withKeycloakIntegration) {
            throw new IllegalArgumentException("Cannot initialize Keycloak JWT Auth when withKeycloakIntegration is false");
        }

        var vaultClient = this.vaultContainer.getVaultAdminClient();
        vaultClient.auth().createJwtAuth(oidcDiscoveryUrl, List.of(oidcClientId));
    }

    public void initUserGroup(String fullPathUserGroupId) {
        var vaultAdminClient = this.vaultContainer.getVaultAdminClient();
        vaultAdminClient.acl().createOrUpdateGroup(fullPathUserGroupId, GroupType.EXTERNAL, List.of());

        if (this.withKeycloakIntegration) {
            var vaultUserGroup = vaultAdminClient.acl().getGroupByName(fullPathUserGroupId);
            var mountAccessor = vaultAdminClient.auth().getJwtAuthMethod().getAccessor();
            vaultAdminClient.auth().addJwtGroupAlias(
                    fullPathUserGroupId,
                    mountAccessor,
                    vaultUserGroup.getId());
        }
    }
}
