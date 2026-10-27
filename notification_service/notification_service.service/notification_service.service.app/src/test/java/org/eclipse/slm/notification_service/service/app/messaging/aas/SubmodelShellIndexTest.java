package org.eclipse.slm.notification_service.service.app.messaging.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubmodelShellIndexTest {

    private final SubmodelShellIndex index = new SubmodelShellIndex();

    private static Reference submodelReference(String submodelId) {
        return new DefaultReference.Builder()
                .type(ReferenceTypes.MODEL_REFERENCE)
                .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value(submodelId).build())
                .build();
    }

    @Test
    void putGetRemove() {
        index.put("sm", "shell");
        assertThat(index.getShellId("sm")).contains("shell");

        index.remove("sm");
        assertThat(index.getShellId("sm")).isEmpty();
    }

    @Test
    void removeShellDropsAllItsSubmodels() {
        index.put("a", "s1");
        index.put("b", "s1");
        index.put("c", "s2");

        index.removeShell("s1");

        assertThat(index.getShellId("a")).isEmpty();
        assertThat(index.getShellId("b")).isEmpty();
        assertThat(index.getShellId("c")).contains("s2");
    }

    @Test
    void initializeFromResourceShells() {
        var r1 = UUID.randomUUID();
        var r2 = UUID.randomUUID();
        var r3 = UUID.randomUUID();
        var client = mock(AasRepositoryClient.class);
        var shell = new DefaultAssetAdministrationShell.Builder()
                .id("Resource_" + r1)
                .submodels(List.of(submodelReference("sm-1"), submodelReference("sm-2")))
                .build();
        when(client.getAas("Resource_" + r1)).thenReturn(Optional.of(shell));
        when(client.getAas("Resource_" + r2)).thenThrow(new RuntimeException("aas repository down"));
        when(client.getAas("Resource_" + r3)).thenReturn(Optional.empty());

        index.initialize(List.of(r2, r1, r3), client);

        assertThat(index.getShellId("sm-1")).contains("Resource_" + r1);
        assertThat(index.getShellId("sm-2")).contains("Resource_" + r1);
    }
}
