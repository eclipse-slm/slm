package org.eclipse.slm.resource_management.features.capabilities;

import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityUtilTest {

    static Capability capability;
    static Map<String, String> configParams;

    @BeforeAll
    public static void beforeAll() {
        capability = CapabilityUtilTestConfig.getSingleHostCapability();
        configParams = CapabilityUtilTestConfig.configParameter;
    }

    @Test
    public void testGetNonSecretConfigParameter() {
        Map<String, String> nonSecretConfigParameter = CapabilityUtil.getNonSecretConfigParameter(
                capability,
                configParams
        );

        assertThat(nonSecretConfigParameter).isNotNull();
        assertThat(nonSecretConfigParameter).hasSize(1);
        assertThat(nonSecretConfigParameter.get("username"))
                .isEqualTo(configParams.get("username"));
    }

    @Test
    public void testGetSecretConfigParameter() {
        Map<String, String> nonSecretConfigParameter = CapabilityUtil.getSecretConfigParameter(
                capability,
                configParams
        );

        assertThat(nonSecretConfigParameter).isNotNull();
        assertThat(nonSecretConfigParameter).hasSize(1);
        assertThat(nonSecretConfigParameter.get("password"))
                .isEqualTo(configParams.get("password"));
    }
}
