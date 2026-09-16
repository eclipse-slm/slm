package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import org.eclipse.slm.common.access.AccessControlService;
import org.eclipse.slm.common.access.UserContext;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceInstancePersistenceTest {

    private ServiceInstanceJpaRepository repository;
    private AccessControlService accessControlService;
    private ServiceInstancePersistence persistence;

    private ServiceInstance instance(UUID id) {
        return new ServiceInstance(
                id, List.of(), Map.of(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                List.of(), List.of());
    }

    // ServiceInstance's domain constructor requires non-null resourceId/capabilityServiceId/etc,
    // so test fixtures need those populated even though only the id matters for these tests.
    private ServiceInstanceEntity entityWithId(UUID id) {
        var entity = new ServiceInstanceEntity(id);
        entity.setResourceId(UUID.randomUUID());
        entity.setCapabilityServiceId(UUID.randomUUID());
        entity.setServiceOfferingId(UUID.randomUUID());
        entity.setServiceOfferingVersionId(UUID.randomUUID());
        return entity;
    }

    @BeforeEach
    public void setUp() {
        repository = mock(ServiceInstanceJpaRepository.class);
        accessControlService = mock(AccessControlService.class);
        persistence = new ServiceInstancePersistence(
                repository, new ServiceInstancePersistenceMapper(), accessControlService);
    }

    @Test
    public void createRegistersOwnerPolicy() {
        var id = UUID.randomUUID();

        persistence.create(instance(id), "/users/user-1");

        verify(repository).save(any(ServiceInstanceEntity.class));
        verify(accessControlService).createSingleObjectPolicy(
                anyString(), eq("/users/user-1"),
                eq(ServiceInstanceObjectTypes.SERVICE_INSTANCE), eq(id));
    }

    @Test
    public void createUsesPolicyNamePrefix() {
        var id = UUID.randomUUID();
        var nameCaptor = ArgumentCaptor.forClass(String.class);

        persistence.create(instance(id), "/users/user-1");

        verify(accessControlService).createSingleObjectPolicy(
                nameCaptor.capture(), anyString(), anyString(), any(UUID.class));
        assertThat(nameCaptor.getValue())
                .isEqualTo(ServiceInstancePersistence.SERVICE_INSTANCE_POLICY_PREFIX + id);
    }

    @Test
    public void deleteRemovesRowAndPolicies() {
        var id = UUID.randomUUID();

        persistence.delete(id);

        verify(repository).deleteById(id);
        verify(accessControlService).removeObjectFromAllPolicies(
                ServiceInstanceObjectTypes.SERVICE_INSTANCE, id);
    }

    @Test
    public void getAccessibleReturnsOnlyPermittedInstancesForNonAdmin() {
        var permitted = UUID.randomUUID();
        var userContext = new UserContext(Set.of("/Org/CustomerA"), false);
        when(accessControlService.getAccessibleObjectIds(
                userContext, ServiceInstanceObjectTypes.SERVICE_INSTANCE))
                .thenReturn(Optional.of(Set.of(permitted)));
        when(repository.findByIdIn(Set.of(permitted)))
                .thenReturn(List.of(entityWithId(permitted)));

        var result = persistence.getAccessible(userContext);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(permitted);
    }

    @Test
    public void getAccessibleReturnsAllInstancesForAdmin() {
        var a = UUID.randomUUID();
        var b = UUID.randomUUID();
        var userContext = new UserContext(Set.of(), true);
        when(accessControlService.getAccessibleObjectIds(
                userContext, ServiceInstanceObjectTypes.SERVICE_INSTANCE))
                .thenReturn(Optional.empty());
        when(repository.findAll())
                .thenReturn(List.of(entityWithId(a), entityWithId(b)));

        var result = persistence.getAccessible(userContext);

        assertThat(result).hasSize(2);
    }

    @Test
    public void getAccessibleReturnsNothingWhenUserHasNoPermittedIds() {
        var userContext = new UserContext(Set.of("/Org/CustomerB"), false);
        when(accessControlService.getAccessibleObjectIds(
                userContext, ServiceInstanceObjectTypes.SERVICE_INSTANCE))
                .thenReturn(Optional.of(Set.of()));

        assertThat(persistence.getAccessible(userContext)).isEmpty();
        verify(repository, org.mockito.Mockito.never()).findAll();
    }

    @Test
    public void getByIdReturnsEmptyWhenAccessDenied() {
        var id = UUID.randomUUID();
        var userContext = new UserContext(Set.of("/Org/CustomerB"), false);
        when(accessControlService.hasAccess(
                ServiceInstanceObjectTypes.SERVICE_INSTANCE, id, userContext))
                .thenReturn(false);

        assertThat(persistence.getById(id, userContext)).isEmpty();
    }

    @Test
    public void getByIdReturnsInstanceWhenAccessGranted() {
        var id = UUID.randomUUID();
        var userContext = new UserContext(Set.of("/Org/CustomerA"), false);
        when(repository.findById(id)).thenReturn(Optional.of(entityWithId(id)));
        when(accessControlService.hasAccess(
                ServiceInstanceObjectTypes.SERVICE_INSTANCE, id, userContext))
                .thenReturn(true);

        assertThat(persistence.getById(id, userContext)).isPresent();
    }

    @Test
    public void getByIdUnfilteredSkipsAccessCheck() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithId(id)));

        assertThat(persistence.getByIdUnfiltered(id)).isPresent();
        verify(accessControlService, org.mockito.Mockito.never())
                .hasAccess(anyString(), any(UUID.class), any(UserContext.class));
    }

    @Test
    public void getOwnerGroupsDelegatesToAccessControl() {
        var id = UUID.randomUUID();
        when(accessControlService.getSubjectsForObject(
                ServiceInstanceObjectTypes.SERVICE_INSTANCE, id))
                .thenReturn(Set.of("/users/user-1"));

        assertThat(persistence.getOwnerGroups(id)).containsExactly("/users/user-1");
    }
}
