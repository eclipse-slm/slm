package org.eclipse.slm.resource_management.features.capabilities;

import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest(classes = {
        CapabilityUtil.class,
        CapabilityJpaRepository.class,
        ConsulNodesApiClient.class,
        ConsulServicesApiClient.class,
        ConsulAclApiClient.class,
        RestTemplate.class
})
@ActiveProfiles("test")
public class CapabilityUtilTest {
    @Autowired
    CapabilityUtil capabilityUtil;
    @MockBean
    CapabilityJpaRepository capabilityJpaRepository;

    static Capability capability;
    static Map<String, String> configParams;

    @BeforeAll
    public static void beforeAll() {
        capability = CapabilityUtilTestConfig.getSingleHostCapability();
        configParams = CapabilityUtilTestConfig.configParameter;
    }

    @Test
    public void testGetNonSecretConfigParameter() {
        Map<String, String> nonSecretConfigParameter = capabilityUtil.getNonSecretConfigParameter(
                capability,
                configParams
        );

        assertNotNull(nonSecretConfigParameter);
        assertEquals(1, nonSecretConfigParameter.size());
        assertEquals(
                configParams.get("username"),
                nonSecretConfigParameter.get("username")
        );
    }

    @Test
    public void testGetSecretConfigParameter() {
        Map<String, String> nonSecretConfigParameter = capabilityUtil.getSecretConfigParameter(
                capability,
                configParams
        );

        assertNotNull(nonSecretConfigParameter);
        assertEquals(1, nonSecretConfigParameter.size());
        assertEquals(
                configParams.get("password"),
                nonSecretConfigParameter.get("password")
        );
    }
}
