package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.testutils.ConsulTestClientFactory;
import org.eclipse.slm.common.consul.model.acl.policies.Policy;
import org.eclipse.slm.common.consul.model.acl.roles.PolicyLink;
import org.eclipse.slm.common.consul.model.acl.roles.Role;
import org.eclipse.slm.common.consul.model.acl.authmethods.AuthMethodRequest;
import org.eclipse.slm.common.consul.model.acl.authmethods.JwtAuthMethodConfig;
import org.eclipse.slm.common.consul.model.acl.bindingrules.BindingRule;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;
import org.eclipse.slm.common.keycloak.testing.KeycloakTestContainer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Testcontainers
public class ConsulAclClientTest {
    private final static Logger LOG = LoggerFactory.getLogger(ConsulAclClientTest.class);

    @Container
    private final static ConsulTestContainer consulContainer = new ConsulTestContainer();

    @Container
    static KeycloakTestContainer keycloakContainer = new KeycloakTestContainer();

    public static ConsulClient consulClient;
    public static ConsulAclClient consulAclClient;

    @BeforeAll
    public static void beforeAll() {
        consulClient = ConsulTestClientFactory.getConsulClient(consulContainer);
        consulAclClient = consulClient.acl();
    }

    @Nested
    @Order(10)
    class Policies {
        @Nested
        class GetPolicies {
            @Test
            void shouldReturnPoliciesListInitially() {
                var policies = consulAclClient.getPolicies();
                assertThat(policies).isNotNull();
            }
        }

        @Nested
        class CreateReadUpdateDeletePolicy {
            @Test
            void shouldCreateReadUpdateAndDeletePolicy() {
                // Arrange: Create
                var policy = new Policy();
                policy.setName("node-read-test");
                policy.setDescription("Grants read access to all node information (test)");
                policy.setRules("node_prefix \"\" { policy = \"read\" }");

                // Act: Create
                var created = consulAclClient.createPolicy(policy);
                assertThat(created).isNotNull();
                assertThat(created.getId()).isNotBlank();
                assertThat(created.getName()).isEqualTo("node-read-test");

                // Act: Read by name
                var read = consulAclClient.getPolicyByNameOrThrow("node-read-test");
                assertThat(read).isNotNull();
                assertThat(read.getId()).isEqualTo(created.getId());

                // Act: Update (change description)
                read.setDescription("Updated description");
                var updated = consulAclClient.updatePolicy(read);
                assertThat(updated.getDescription()).isEqualTo("Updated description");

                // Act: Delete
                consulAclClient.deletePolicyById(created.getId());

                // Assert: subsequent read by name throws not found
                assertThatThrownBy(() -> consulAclClient.getPolicyByNameOrThrow("node-read-test"))
                    .isInstanceOf(org.eclipse.slm.common.consul.model.exceptions.ConsulPolicyNotFoundException.class);
            }
        }

        @Nested
        class AddAndRemoveReadRule {
            @Test
            void shouldAddAndRemoveReadRule() {
                // Arrange: ensure policy exists
                var policy = new Policy();
                policy.setName("nodes-policy-rules");
                policy.setDescription("Policy for rule add/remove test");
                policy.setRules("node_prefix \"\" { policy = \"read\" }");
                var created = consulAclClient.createPolicy(policy);

                // Act: add read rule for a service
                consulAclClient.addReadRuleToPolicy("nodes-policy-rules", "service", "app");
                var afterAdd = consulAclClient.getPolicyByNameOrThrow("nodes-policy-rules");
                assertThat(afterAdd.getRules()).contains("service \"app\" { policy = \"read\" }");

                // Act: remove read rule
                consulAclClient.removeReadRuleFromPolicy("nodes-policy-rules", "service", "app");
                var afterRemove = consulAclClient.getPolicyByNameOrThrow("nodes-policy-rules");
                assertThat(afterRemove.getRules()).doesNotContain("service \"app\" { policy = \"read\" }");

                // Cleanup
                consulAclClient.deletePolicyById(created.getId());
            }
        }
    }

    @Nested
    @Order(20)
    class Roles {
        @Nested
        class ListAndCreateRole {
            @Test
            void shouldListRolesAndCreateNewRoleLinkedToPolicyByName() throws org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException {
                // Arrange: create a policy to link
                var policy = new Policy();
                policy.setName("role-link-policy");
                policy.setDescription("Policy for role linkage");
                policy.setRules("node_prefix \"\" { policy = \"read\" }");
                var createdPolicy = consulAclClient.createPolicy(policy);

                // Act: list roles initially
                var rolesBefore = consulAclClient.getRoles();
                assertThat(rolesBefore).isNotNull();

                // Act: create role linked to policy name
                consulAclClient.createRole("example-role-acl", "Showcases linkage", List.of(PolicyLink.builder().name("role-link-policy").build()));

                // Assert: role exists by name and by list
                var roleByName = consulAclClient.getRoleByName("example-role-acl");
                assertThat(roleByName).isNotNull();
                assertThat(roleByName.getName()).isEqualTo("example-role-acl");

                var rolesAfter = consulAclClient.getRoles();
                assertThat(rolesAfter.stream().map(Role::getName)).contains("example-role-acl");

                // Cleanup: delete policy used (role remains, but this is fine for the test container lifecycle)
                consulAclClient.deletePolicyById(createdPolicy.getId());
            }
        }

        @Nested
        class GetRolesLinkedToPolicy {
            @Test
            void shouldReturnRolesLinkedToSpecificPolicy() {
                // Arrange: ensure policy and role linkage present
                var policy = new Policy();
                policy.setName("linked-policy-test");
                policy.setDescription("Policy for linked roles test");
                policy.setRules("node_prefix \"\" { policy = \"read\" }");
                var createdPolicy = consulAclClient.createPolicy(policy);

                consulAclClient.createRole("linked-role-a", "desc", List.of(PolicyLink.builder().name("linked-policy-test").build()));
                consulAclClient.createRole("linked-role-b", "desc", List.of(PolicyLink.builder().name("linked-policy-test").build()));

                // Act
                var linkedRoles = consulAclClient.getRolesLinkedToPolicy(createdPolicy.getId());
                // Assert
                assertThat(linkedRoles).isNotNull();
                assertThat(linkedRoles.stream().map(Role::getName))
                    .contains("linked-role-a", "linked-role-b");

                // Cleanup
                consulAclClient.deletePolicyById(createdPolicy.getId());
            }
        }

        @Nested
        class ReadUpdateDeleteRole {
            @Test
            void shouldReadUpdateAndDeleteRole() throws org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException {
                // Arrange: create policy and role
                var policy = new Policy();
                policy.setName("role-mgmt-policy");
                policy.setDescription("Policy for role management");
                policy.setRules("node_prefix \"\" { policy = \"read\" }");
                consulAclClient.createPolicy(policy);

                consulAclClient.createRole("role-to-manage", "initial", List.of(PolicyLink.builder().name("role-mgmt-policy").build()));
                var role = consulAclClient.getRoleByName("role-to-manage");
                assertThat(role).isNotNull();

                // Act: read by id
                var readById = consulAclClient.getRoleById(role.getId());
                assertThat(readById.getName()).isEqualTo("role-to-manage");

                // Act: add another policy link by id
                var anotherPolicy = new Policy();
                anotherPolicy.setName("role-mgmt-policy-2");
                anotherPolicy.setDescription("Second policy");
                anotherPolicy.setRules("service \"api\" { policy = \"read\" }");
                var createdPolicy2 = consulAclClient.createPolicy(anotherPolicy);

                consulAclClient.addPolicyToRole(role.getName(), createdPolicy2.getId());
                var updatedRole = consulAclClient.getRoleById(role.getId());
                assertThat(updatedRole.getPolicies()).isNotEmpty();
                assertThat(updatedRole.getPolicies().stream().anyMatch(pl -> java.util.Objects.equals(createdPolicy2.getId(), pl.getId())))
                    .isTrue();

                // Act: delete role
                assertThatCode(() -> consulAclClient.deleteRoleById(role.getId())).doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @Order(30)
    class BindingRules {
        @Nested
        class ListCreateDelete {
            @Test
            void shouldListCreateAndDeleteBindingRule() {
                // Arrange: initial list
                var initial = consulAclClient.getBindingRules();
                assertThat(initial).isNotNull();

                // Arrange: Ensure required Auth Method exists for provider "keycloak"
                var authConfig = new JwtAuthMethodConfig(keycloakContainer.getTestRealmIssuerUri(), new HashMap<>(), new HashMap<>());
                var authRequest = new AuthMethodRequest("keycloak", "jwt", "Test Auth Method", authConfig, null);
                assertThatCode(() -> consulAclClient.createAuthMethod(authRequest)).doesNotThrowAnyException();

                // Act: create a binding rule (provider "keycloak" is expected by client implementation)
                var bindingRuleName = "test-" + UUID.randomUUID();
                var userId = keycloakContainer.TEST_USER1_GROUP_ID;
                var bindingRuleUserGroup = new BindingRule(
                        null,
                        "Binding rule of group '" + userId + "' to auth method 'keycloak'",
                        "keycloak",
                        "\"" + userId + "\" in list.groups",
                        "role",
                        bindingRuleName
                );

                assertThatCode(() -> consulAclClient.createBindingRule(bindingRuleUserGroup)).doesNotThrowAnyException();

                // Assert: list contains the rule
                var listAfterCreate = consulAclClient.getBindingRules();
                assertThat(listAfterCreate.stream().anyMatch(br -> bindingRuleName.equals(br.getBindName())))
                    .isTrue();

                // Act: delete by id (find id from list)
                var createdRuleOpt = listAfterCreate.stream()
                    .filter(br -> bindingRuleName.equals(br.getBindName()))
                    .findFirst();
                if (createdRuleOpt.isPresent()) {
                    assertThatCode(() -> consulAclClient.deleteBindingRuleById(createdRuleOpt.get().getId()))
                        .doesNotThrowAnyException();

                    // Assert: no longer present
                    var listAfterDelete = consulAclClient.getBindingRules();
                    assertThat(listAfterDelete.stream().anyMatch(br -> bindingRuleName.equals(br.getBindName())))
                        .isFalse();
                } else {
                    fail("Created binding rule not found in listing");
                }
            }
        }
    }
}
