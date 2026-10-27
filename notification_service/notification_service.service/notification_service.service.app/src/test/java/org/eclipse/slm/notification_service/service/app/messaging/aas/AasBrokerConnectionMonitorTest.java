package org.eclipse.slm.notification_service.service.app.messaging.aas;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClient;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClientFactory;
import org.eclipse.slm.notification_service.service.app.messaging.resources.ResourceUserResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AasBrokerConnectionMonitorTest {

    private static final String BROKER_URL = "tcp://aas-broker:1884";

    private final UUID rid = UUID.randomUUID();

    private final String shellId = "Resource_" + rid;

    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();

    private SubmodelShellIndex index;

    private AasRepositoryClient client;

    private AasBrokerConnectionMonitor monitor;

    @BeforeEach
    void setUp() {
        logs.start();
        ((Logger) LoggerFactory.getLogger(AasBrokerConnectionMonitor.class)).addAppender(logs);

        index = new SubmodelShellIndex();
        client = mock(AasRepositoryClient.class);
        var clientFactory = mock(AasRepositoryClientFactory.class);
        when(clientFactory.getClient()).thenReturn(client);
        var resolver = mock(ResourceUserResolver.class);
        when(resolver.getKnownResourceIds()).thenReturn(Set.of(rid));
        when(client.getAas(shellId)).thenReturn(Optional.of(new DefaultAssetAdministrationShell.Builder()
                .id(shellId)
                .submodels(List.of(new DefaultReference.Builder().type(ReferenceTypes.MODEL_REFERENCE)
                        .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value("sm-1").build()).build()))
                .build()));

        monitor = new AasBrokerConnectionMonitor(BROKER_URL, index, resolver, clientFactory);
    }

    @AfterEach
    void tearDown() {
        ((Logger) LoggerFactory.getLogger(AasBrokerConnectionMonitor.class)).detachAppender(logs);
    }

    private List<ILoggingEvent> logsAt(Level level) {
        return logs.list.stream().filter(event -> event.getLevel() == level).toList();
    }

    @Test
    void checkWarnsWhileNotSubscribed() {
        monitor.check();

        assertThat(logsAt(Level.WARN)).singleElement()
                .extracting(ILoggingEvent::getFormattedMessage).asString().contains(BROKER_URL);
    }

    @Test
    void subscribedLogsInfoAndRefreshesIndex() {
        monitor.onSubscribed();

        assertThat(logsAt(Level.INFO)).anySatisfy(event -> assertThat(event.getFormattedMessage()).contains(BROKER_URL));
        assertThat(index.getShellId("sm-1")).contains(shellId);
    }

    @Test
    void checkIsQuietAfterSubscribe() {
        monitor.onSubscribed();
        monitor.check();

        assertThat(logsAt(Level.WARN)).isEmpty();
    }

    @Test
    void connectionFailureLogsWarnAndCheckWarnsAgain() {
        monitor.onSubscribed();

        monitor.onConnectionFailed(new RuntimeException("connection lost"));
        monitor.check();

        assertThat(logsAt(Level.WARN)).hasSize(2);
    }

    @Test
    void checkRetriesIndexRefreshUntilComplete() {
        when(client.getAas(shellId))
                .thenThrow(new RuntimeException("aas repository down"))
                .thenReturn(Optional.empty());

        monitor.onSubscribed();
        monitor.check();
        monitor.check();

        verify(client, times(2)).getAas(shellId);
    }
}
