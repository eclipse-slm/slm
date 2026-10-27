package org.eclipse.slm.notification_service.service.app.messaging.aas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AasEventNotificationSerializationTest {

    @Test
    void serialisesWithTypeDiscriminatorAndAllFields() throws Exception {
        var notification = new AasEventNotification("u1", new Date(0), AasEventType.UPDATED, AasElementKind.SUBMODEL,
                UUID.fromString("11111111-1111-1111-1111-111111111111"), "Resource_11111111-1111-1111-1111-111111111111",
                "sm-1", "Nameplate", "https://admin-shell.io/zvei/nameplate/2/0/Nameplate");

        var objectMapper = new ObjectMapper();
        var json = objectMapper.readTree(objectMapper.writeValueAsString(notification));

        assertThat(json.get("type").asText()).isEqualTo("AAS_EVENT");
        assertThat(json.get("eventType").asText()).isEqualTo("UPDATED");
        assertThat(json.get("elementKind").asText()).isEqualTo("SUBMODEL");
        assertThat(json.get("resourceId").asText()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(json.get("shellId").asText()).isEqualTo("Resource_11111111-1111-1111-1111-111111111111");
        assertThat(json.get("submodelId").asText()).isEqualTo("sm-1");
        assertThat(json.get("submodelIdShort").asText()).isEqualTo("Nameplate");
        assertThat(json.get("semanticId").asText()).isEqualTo("https://admin-shell.io/zvei/nameplate/2/0/Nameplate");
        assertThat(json.get("userId").asText()).isEqualTo("u1");
    }
}
