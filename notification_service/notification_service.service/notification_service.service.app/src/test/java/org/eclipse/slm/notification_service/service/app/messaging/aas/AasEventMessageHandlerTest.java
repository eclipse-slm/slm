package org.eclipse.slm.notification_service.service.app.messaging.aas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClient;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClientFactory;
import org.eclipse.slm.notification_service.communication.websocket.NotificationWsService;
import org.eclipse.slm.notification_service.model.IEventNotification;
import org.eclipse.slm.notification_service.service.app.messaging.resources.ResourceUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AasEventMessageHandlerTest {

    private final UUID rid = UUID.randomUUID();

    private final String shell = "Resource_" + rid;

    private SubmodelShellIndex index;

    private ResourceUserResolver resolver;

    private NotificationWsService ws;

    private SubmodelRegistryClient submodelRegistryClient;

    private AasEventMessageHandler handler;

    private final ArgumentCaptor<IEventNotification> captor = ArgumentCaptor.forClass(IEventNotification.class);

    private static String b64(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(UTF_8));
    }

    @BeforeEach
    void setUp() {
        index = new SubmodelShellIndex();
        resolver = mock(ResourceUserResolver.class);
        ws = mock(NotificationWsService.class);
        when(resolver.getUserIds(rid)).thenReturn(List.of("u1", "u2"));
        submodelRegistryClient = mock(SubmodelRegistryClient.class);
        when(submodelRegistryClient.getSubmodelDescriptor(any())).thenReturn(Optional.empty());
        var submodelRegistryClientFactory = mock(SubmodelRegistryClientFactory.class);
        when(submodelRegistryClientFactory.getClient()).thenReturn(submodelRegistryClient);
        handler = new AasEventMessageHandler(resolver, index, ws, new ObjectMapper(), submodelRegistryClientFactory);
    }

    private AasEventNotification firstSent() {
        verify(ws, atLeastOnce()).notifyFrontend(captor.capture());
        return (AasEventNotification) captor.getAllValues().get(0);
    }

    @Test
    void shellUpdatedNotifiesEachUser() {
        handler.handle("aas-repository/r/shells/updated", "{\"id\":\"" + shell + "\"}");

        verify(ws, times(2)).notifyFrontend(captor.capture());
        var notifications = captor.getAllValues().stream().map(AasEventNotification.class::cast).toList();
        assertThat(notifications).extracting(AasEventNotification::getUserId).containsExactly("u1", "u2");
        var n = notifications.get(0);
        assertThat(n.getResourceId()).isEqualTo(rid);
        assertThat(n.getShellId()).isEqualTo(shell);
        assertThat(n.getElementKind()).isEqualTo(AasElementKind.SHELL);
        assertThat(n.getEventType()).isEqualTo(AasEventType.UPDATED);
        assertThat(n.getSubmodelId()).isNull();
        assertThat(n.getSemanticId()).isNull();
    }

    @Test
    void nonResourceShellSendsNothing() {
        handler.handle("aas-repository/r/shells/created", "{\"id\":\"ResourceTypes\"}");

        verifyNoInteractions(ws);
    }

    @Test
    void submodelEventResolvesViaIndex() {
        index.put("sm-1", shell);

        handler.handle("sm-repository/r/submodels/updated",
                "{\"id\":\"sm-1\",\"idShort\":\"Nameplate\",\"semanticId\":{\"keys\":[{\"type\":\"GlobalReference\",\"value\":\"urn:np\"}]}}");

        var n = firstSent();
        assertThat(n.getElementKind()).isEqualTo(AasElementKind.SUBMODEL);
        assertThat(n.getEventType()).isEqualTo(AasEventType.UPDATED);
        assertThat(n.getShellId()).isEqualTo(shell);
        assertThat(n.getResourceId()).isEqualTo(rid);
        assertThat(n.getSubmodelId()).isEqualTo("sm-1");
        assertThat(n.getSubmodelIdShort()).isEqualTo("Nameplate");
        assertThat(n.getSemanticId()).isEqualTo("urn:np");
    }

    @Test
    void submodelIndexMissSendsNothing() {
        handler.handle("sm-repository/r/submodels/updated", "{\"id\":\"unknown\"}");

        verifyNoInteractions(ws);
    }

    @Test
    void submodelDeletedKeepsIndexEntrySoRecreatedSubmodelIsResolved() {
        index.put("sm-1", shell);

        handler.handle("sm-repository/r/submodels/deleted", "{\"id\":\"sm-1\"}");
        handler.handle("sm-repository/r/submodels/created", "{\"id\":\"sm-1\"}");

        verify(ws, times(4)).notifyFrontend(any(IEventNotification.class));
        assertThat(index.getShellId("sm-1")).contains(shell);
    }

    @Test
    void repositoryReferenceCreatedUpdatesIndex() {
        handler.handle("aas-repository/r/shells/submodels/" + b64("sm-9") + "/created", "{\"id\":\"" + shell + "\"}");

        assertThat(index.getShellId("sm-9")).contains(shell);
        var n = firstSent();
        assertThat(n.getElementKind()).isEqualTo(AasElementKind.SUBMODEL_REFERENCE);
        assertThat(n.getEventType()).isEqualTo(AasEventType.CREATED);
        assertThat(n.getSubmodelId()).isEqualTo("sm-9");
        assertThat(n.getShellId()).isEqualTo(shell);
    }

    @Test
    void repositoryReferenceIsEnrichedFromSubmodelRegistry() {
        var descriptor = new DefaultSubmodelDescriptor.Builder()
                .id("sm-9")
                .idShort("Nameplate")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder().type(KeyTypes.GLOBAL_REFERENCE).value("urn:np").build())
                        .build())
                .build();
        when(submodelRegistryClient.getSubmodelDescriptor("sm-9")).thenReturn(Optional.of(descriptor));

        handler.handle("aas-repository/r/shells/submodels/sm-9/created", "{\"id\":\"" + shell + "\"}");

        var n = firstSent();
        assertThat(n.getSubmodelId()).isEqualTo("sm-9");
        assertThat(n.getSubmodelIdShort()).isEqualTo("Nameplate");
        assertThat(n.getSemanticId()).isEqualTo("urn:np");
        verify(submodelRegistryClient, times(1)).getSubmodelDescriptor("sm-9");
    }

    @Test
    void referenceIsSentWithoutMetadataWhenSubmodelRegistryFails() {
        when(submodelRegistryClient.getSubmodelDescriptor("sm-9")).thenThrow(new RuntimeException("registry down"));

        handler.handle("aas-repository/r/shells/submodels/sm-9/created", "{\"id\":\"" + shell + "\"}");

        verify(ws, times(2)).notifyFrontend(any(IEventNotification.class));
        var n = firstSent();
        assertThat(n.getSubmodelId()).isEqualTo("sm-9");
        assertThat(n.getSubmodelIdShort()).isNull();
        assertThat(n.getSemanticId()).isNull();
    }

    @Test
    void serviceReferenceDeletedUpdatesIndexAndCarriesSemanticId() {
        index.put("sm-9", shell);

        handler.handle("aas-repository/r/shells/" + b64(shell) + "/submodelReferences/deleted",
                "{\"keys\":[{\"type\":\"Submodel\",\"value\":\"sm-9\"}],\"referredSemanticId\":{\"keys\":[{\"value\":\"urn:x\"}]}}");

        assertThat(index.getShellId("sm-9")).isEmpty();
        var n = firstSent();
        assertThat(n.getElementKind()).isEqualTo(AasElementKind.SUBMODEL_REFERENCE);
        assertThat(n.getEventType()).isEqualTo(AasEventType.DELETED);
        assertThat(n.getSubmodelId()).isEqualTo("sm-9");
        assertThat(n.getSemanticId()).isEqualTo("urn:x");
    }

    @Test
    void shellDeletedWithoutUsersClearsIndexAndSendsNothing() {
        index.put("sm-1", shell);
        when(resolver.getUserIds(rid)).thenReturn(List.of());

        handler.handle("aas-repository/r/shells/deleted", "{\"id\":\"" + shell + "\"}");

        assertThat(index.getShellId("sm-1")).isEmpty();
        verifyNoInteractions(ws);
    }

    @Test
    void invalidJsonIsDroppedWithoutThrowing() {
        assertThatNoException().isThrownBy(() -> handler.handle("aas-repository/r/shells/created", "not json"));

        verifyNoInteractions(ws);
    }

    @Test
    void missingIdIsDropped() {
        handler.handle("aas-repository/r/shells/created", "{}");

        verifyNoInteractions(ws);
    }

    @Test
    void emptySemanticIdKeysYieldsNull() {
        index.put("sm-1", shell);

        handler.handle("sm-repository/r/submodels/created", "{\"id\":\"sm-1\",\"semanticId\":{\"keys\":[]}}");

        assertThat(firstSent().getSemanticId()).isNull();
    }

    @Test
    void unknownTopicSendsNothing() {
        handler.handle("foo/bar", "{}");

        verifyNoInteractions(ws, resolver);
    }

    @Test
    void resolverExceptionDoesNotPropagate() {
        when(resolver.getUserIds(any())).thenThrow(new RuntimeException("consul down"));

        assertThatNoException().isThrownBy(() -> handler.handle("aas-repository/r/shells/updated", "{\"id\":\"" + shell + "\"}"));
    }
}
