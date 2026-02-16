package org.eclipse.slm.common.vault.client;

import org.eclipse.slm.common.vault.client.exceptions.VaultGroupNotFoundException;
import org.eclipse.slm.common.vault.client.exceptions.VaultPolicyNotFoundException;
import org.eclipse.slm.common.vault.model.acl.GroupType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VaultClientAclTest {

        @Container
        private static final VaultTestContainer vaultContainer = new VaultTestContainer();

        private VaultClient vaultAdminClient;

        @BeforeAll
        void setUp() {
            vaultAdminClient = vaultContainer.getVaultClient();
        }

        @Nested
        class Policies {
            @Nested
            class AddPolicy {
                @Test
                void shouldAddPolicy() {
                    // Arrange
                    var policyName = "new-test-policy";
                    var policyRules = "path \"secret/data/*\" { capabilities = [\"create\", \"read\", \"update\", \"delete\", \"list\"] }";
                    // Act
                    vaultAdminClient.acl().createOrUpdatePolicy(policyName, policyRules);
                    // Assert
                    var policy = vaultAdminClient.acl().getPolicy(policyName);
                    assertThat(policy.getName()).isEqualTo(policyName);
                }
            }

            @Nested
            class GetPolicy {
                @Test
                void shouldAddPolicy() {
                    // Arrange
                    var policyName = "new-test-policy";
                    var policyRules = "path \"secret/data/*\" { capabilities = [\"create\", \"read\", \"update\", \"delete\", \"list\"] }";
                    vaultAdminClient.acl().createOrUpdatePolicy(policyName, policyRules);
                    // Act
                    var policy = vaultAdminClient.acl().getPolicy(policyName);
                    // Assert
                    assertThat(policy.getName()).isEqualTo(policyName);
                }

                @Test
                void shouldThrowVaultPolicyNotFoundExceptionWhenPolicyDoesNotExist() {
                    // Arrange
                    var policyName = "non-existing-policy";
                    // Act & Assert
                    assertThatThrownBy(() -> {
                        vaultAdminClient.acl().getPolicy(policyName);
                    }).isInstanceOf(VaultPolicyNotFoundException.class);
                }
            }

            @Nested
            class DeletePolicyByName {
                @Test
                void shouldDeletePolicy() {
                    // Arrange
                    var policyName = "new-test-policy-to-be-deleted";
                    var policyRules = "path \"secret/data/*\" { capabilities = [\"create\", \"read\", \"update\", \"delete\", \"list\"] }";
                    vaultAdminClient.acl().createOrUpdatePolicy(policyName, policyRules);
                    var policy = vaultAdminClient.acl().getPolicy(policyName);
                    assertThat(policy.getName()).isEqualTo(policyName);
                    // Act
                    vaultAdminClient.acl().deletePolicy(policyName);
                    // Assert
                    assertThatThrownBy(() -> {
                        vaultAdminClient.acl().getPolicy(policyName);
                    }).isInstanceOf(VaultPolicyNotFoundException.class);
                }

                @Test
                void shouldNotThrowExceptionWhenPolicyDoesNotExist() {
                    // Arrange
                    var policyName = "non-existing-policy";
                    // Act & Assert
                    assertDoesNotThrow(() -> {
                        vaultAdminClient.acl().deletePolicy(policyName);
                    });
                }
            }

            @Nested
            class AddRuleToPolicy {
                @Test
                void shouldAddRuleToPolicy() {
                    // Arrange
                    var policyName = "new-test-policy-for-rule-add";
                    var policyRules = "path \"secret/data/*\" { capabilities = [\"read\"] }";
                    vaultAdminClient.acl().createOrUpdatePolicy(policyName, policyRules);
                    // Act
                    var additionalRule = "path \"secret/data/special/*\" { capabilities = [\"create\", \"update\"] }";
                    vaultAdminClient.acl().addRuleToPolicy(policyName, additionalRule);
                    // Assert
                    var policy = vaultAdminClient.acl().getPolicy(policyName);
                    assertThat(policy.getRules()).contains(additionalRule);
                }

                @Test
                void shouldThrowVaultPolicyNotFoundExceptionWhenPolicyDoesNotExist() {
                    // Arrange
                    var policyName = "non-existing-policy";
                    var additionalRule = "path \"secret/data/special/*\" { capabilities = [\"create\", \"update\"] }";
                    // Act & Assert
                    assertThatThrownBy(() -> {
                        vaultAdminClient.acl().addRuleToPolicy(policyName, additionalRule);
                    }).isInstanceOf(VaultPolicyNotFoundException.class);
                }
            }

            @Nested
            class RemoveRuleFromPolicy {
                @Test
                void shouldRemoveRuleFromPolicy() {
                    // Arrange
                    var policyName = "new-test-policy-for-rule-remove";
                    var policyRules = "path \"secret/data/*\" { capabilities = [\"create\", \"read\", \"update\", \"delete\", \"list\"] }\n" +
                            "path \"secret/data/special/*\" { capabilities = [\"create\", \"update\"] }";
                    vaultAdminClient.acl().createOrUpdatePolicy(policyName, policyRules);
                    // Act
                    var ruleToRemove = "path \"secret/data/special/*\" { capabilities = [\"create\", \"update\"] }";
                    var rulePathToRemove = "secret/data/special/*";
                    vaultAdminClient.acl().removeRuleFromPolicy(policyName, rulePathToRemove);
                    // Assert
                    var policy = vaultAdminClient.acl().getPolicy(policyName);
                    assertThat(policy.getRules()).doesNotContain(ruleToRemove);
                }

                @Test
                void shouldThrowVaultPolicyNotFoundExceptionWhenPolicyDoesNotExist() {
                    // Arrange
                    var policyName = "non-existing-policy";
                    var ruleToRemove = "path \"secret/data/special/*\" { capabilities = [\"create\", \"update\"] }";
                    // Act & Assert
                    assertThatThrownBy(() -> {
                        vaultAdminClient.acl().removeRuleFromPolicy(policyName, ruleToRemove);
                    }).isInstanceOf(VaultPolicyNotFoundException.class);
                }
            }
        }

        @Nested
        class Groups {
            @Nested
            class GetAllGroups {
                @Test
                void getAllGroupNames() {
                    // Arrange
                    var groupName1 = "test-group-1";
                    var groupType = GroupType.EXTERNAL;
                    var policies = new ArrayList<String>();
                    vaultAdminClient.acl().createOrUpdateGroup(groupName1, groupType, policies);
                    var groupName2 = "test-group-2";
                    vaultAdminClient.acl().createOrUpdateGroup(groupName2, groupType, policies);
                    // Act
                    var groupNames = vaultAdminClient.acl().getAllGroupNames();
                    // Assert
                    assertThat(groupNames).contains(groupName1, groupName2);
                }
            }

            @Nested
            class AddGroup {
                @Test
                void shouldAddGroup() {
                    // Arrange
                    var groupName = "new-test-group";
                    var groupType = GroupType.EXTERNAL;
                    var policies = new ArrayList<String>();
                    // Act
                    vaultAdminClient.acl().createOrUpdateGroup(groupName, groupType, policies);
                    // Assert
                    var group = vaultAdminClient.acl().getGroupByName(groupName);
                    assertThat(group.getName()).isEqualTo(groupName);
                }
            }

            @Nested
            class DeleteGroupByName {
                @Test
                void shouldDeleteGroup() {
                    // Arrange
                    var groupName = "new-test-group-to-be-deleted";
                    var groupType = GroupType.EXTERNAL;
                    var policies = new ArrayList<String>();
                    vaultAdminClient.acl().createOrUpdateGroup(groupName, groupType, policies);
                    var group = vaultAdminClient.acl().getGroupByName(groupName);
                    assertThat(group.getName()).isEqualTo(groupName);
                    // Act
                    vaultAdminClient.acl().deleteGroupByName(groupName);
                    // Assert
                    assertThatThrownBy(() -> {
                        vaultAdminClient.acl().getGroupByName(groupName);
                    }).isInstanceOf(VaultGroupNotFoundException.class);
                }

                @Test
                void shouldNotThrowExceptionWhenGroupDoesNotExist() {
                    // Arrange
                    var groupName = "non-existing-group";
                    // Act & Assert
                    assertDoesNotThrow(() -> {
                        vaultAdminClient.acl().deleteGroupByName(groupName);
                    });
                }
            }

            @Nested
            class GetGroupByName {
                @Test
                void shouldReturnGroup() {
                    // Arrange
                    var groupName = "new-test-group";
                    var groupType = GroupType.EXTERNAL;
                    var policies = new ArrayList<String>();
                    // Act
                    vaultAdminClient.acl().createOrUpdateGroup(groupName, groupType, policies);
                    // Assert
                    var group = vaultAdminClient.acl().getGroupByName(groupName);
                    assertThat(group.getName()).isEqualTo(groupName);
                }

                @Test
                void shouldThrowVaultGroupNotFoundExceptionWhenGroupDoesNotExist() {
                    // Arrange
                    var groupName = "non-existing-group";
                    // Act & Assert
                    assertThatThrownBy(() -> {
                        vaultAdminClient.acl().getGroupByName(groupName);
                    }).isInstanceOf(VaultGroupNotFoundException.class);
                }
            }

            @Nested
            class AddPolicyToGroup {
                @Test
                void shouldAddPolicyToGroup() {
                    // Arrange
                    var groupName = "new-test-group-policy-add";
                    var groupType = GroupType.EXTERNAL;
                    var policies = new ArrayList<String>();
                    vaultAdminClient.acl().createOrUpdateGroup(groupName, groupType, policies);
                    // Act
                    vaultAdminClient.acl().addPolicyToGroup(groupName, "new-policy-to-be-added");
                    // Assert
                    var group = vaultAdminClient.acl().getGroupByName(groupName);
                    assertThat(group.getName()).isEqualTo(groupName);
                    assertThat(group.getPolicies()).contains("new-policy-to-be-added");
                }

                @Test
                void shouldThrowVaultGroupNotFoundExceptionWhenGroupDoesNotExist() {
                    // Arrange
                    var groupName = "non-existing-group";
                    // Act & Assert
                    assertThatThrownBy(() -> {
                        vaultAdminClient.acl().addPolicyToGroup(groupName, "some-policy");
                    }).isInstanceOf(VaultGroupNotFoundException.class);
                }
            }

            @Nested
            class RemotePolicyFromGroup {
                @Test
                void shouldRemovePolicyFromGroup() {
                    // Arrange
                    var groupName = "new-test-group-policy-remove";
                    var groupType = GroupType.EXTERNAL;
                    var policies = List.of("new-policy-to-be-removed");
                    vaultAdminClient.acl().createOrUpdateGroup(groupName, groupType, policies);
                    // Act
                    vaultAdminClient.acl().removePolicyFromGroup(groupName, "new-policy-to-be-removed");
                    // Assert
                    var group = vaultAdminClient.acl().getGroupByName(groupName);
                    assertThat(group.getName()).isEqualTo(groupName);
                    assertThat(group.getPolicies()).doesNotContain("new-policy-to-be-removed");
                }

                @Test
                void shouldThrowVaultGroupNotFoundExceptionWhenGroupDoesNotExist() {
                    // Arrange
                    var groupName = "non-existing-group";
                    // Act & Assert
                    assertThatThrownBy(() -> {
                        vaultAdminClient.acl().removePolicyFromGroup(groupName, "some-policy");
                    }).isInstanceOf(VaultGroupNotFoundException.class);
                }
            }
        }



}
