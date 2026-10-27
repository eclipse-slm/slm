package org.eclipse.slm.notification_service.service.app.messaging.resources;

import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.model.acl.roles.PolicyLink;
import org.eclipse.slm.common.consul.model.acl.roles.Role;
import org.eclipse.slm.notification_service.service.app.messaging.UserUtils;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResourceUserResolverTest {

    private final UUID id = UUID.randomUUID();

    private UserUtils userUtils;

    private ConsulClientFactory consulClientFactory;

    private ResourceUserResolver resolver;

    @BeforeEach
    void setUp() {
        userUtils = mock(UserUtils.class);
        consulClientFactory = mock(ConsulClientFactory.class, RETURNS_DEEP_STUBS);
        when(consulClientFactory.createAdminClient().acl().getRoles()).thenReturn(List.of());
        resolver = new ResourceUserResolver(userUtils, consulClientFactory);
    }

    @Test
    void refreshLoadsAndCaches() {
        when(userUtils.getUserIdsAssociatedToPolicy(ResourcesConsulClient.getResourcePolicyName(id))).thenReturn(List.of("u1"));

        assertThat(resolver.refresh(id)).containsExactly("u1");
        assertThat(resolver.getUserIds(id)).containsExactly("u1");
        verify(userUtils, times(1)).getUserIdsAssociatedToPolicy(any());
    }

    @Test
    void evictReturnsCachedAndRemoves() {
        when(userUtils.getUserIdsAssociatedToPolicy(any())).thenReturn(List.of("u1"));
        resolver.refresh(id);

        assertThat(resolver.evict(id)).containsExactly("u1");
        assertThat(resolver.getKnownResourceIds()).doesNotContain(id);
    }

    @Test
    void evictUnknownReturnsEmpty() {
        assertThat(resolver.evict(UUID.randomUUID())).isEmpty();
    }

    @Test
    void getUserIdsFallsBackToRefreshOnMiss() {
        when(userUtils.getUserIdsAssociatedToPolicy(any())).thenReturn(List.of("u2"));

        assertThat(resolver.getUserIds(id)).containsExactly("u2");
        assertThat(resolver.getKnownResourceIds()).contains(id);
    }

    @Test
    void warmsCacheFromConsulRoles() {
        var aliceRole = new Role(null, "users_alice", null,
                List.of(new PolicyLink(null, ResourcesConsulClient.POLICY_RESOURCE_PREFIX + id)));
        var managementRole = new Role(null, "users_bob", null,
                List.of(new PolicyLink(null, ResourcesConsulClient.POLICY_RESOURCE_PREFIX + "management")));
        when(consulClientFactory.createAdminClient().acl().getRoles()).thenReturn(List.of(aliceRole, managementRole));

        var warmResolver = new ResourceUserResolver(userUtils, consulClientFactory);

        assertThat(warmResolver.getUserIds(id)).containsExactly("alice");
        assertThat(warmResolver.getKnownResourceIds()).containsExactly(id);
        verifyNoInteractions(userUtils);
    }
}
