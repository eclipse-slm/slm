package org.eclipse.slm.resource_management.features.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.Action;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AwxActionJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String AWX_ACTION_JSON = """
            {
                "actionClass": "AwxAction",
                "actionType": "INSTALL",
                "awxRepo": "https://github.com/org/private-repo",
                "awxBranch": "main",
                "username": "user",
                "password": "secret",
                "playbook": "playbooks/install.yml"
            }
            """;

    @Test
    public void testScmCredentialsAreDeserialized() throws Exception {
        var action = (AwxAction) objectMapper.readValue(AWX_ACTION_JSON, Action.class);

        assertThat(action.getUsername()).isEqualTo("user");
        assertThat(action.getPassword()).isEqualTo("secret");
    }

    @Test
    public void testScmCredentialsAreNotSerialized() throws Exception {
        var action = (AwxAction) objectMapper.readValue(AWX_ACTION_JSON, Action.class);

        var json = objectMapper.writeValueAsString(action);

        assertThat(json).doesNotContain("username", "user", "password", "secret");
    }
}
