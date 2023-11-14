package org.eclipse.slm.common.awx.client.test.unittests;

import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.model.JobTemplate;
import org.eclipse.slm.common.awx.model.Results;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RestTemplate.class,
        AwxClient.class
})

@TestPropertySource(properties = { "spring.config.location=classpath:application.yml" })
public class AwxClientTests {

    @Autowired
    AwxClient awxClient;

    @Test
    @Disabled
    public void testCall() {
        var token = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkMTViNzJhMS0xZDM0LTQ0YjMtODkyNC01YjVkNzY0OGZhZWYifQ.eyJleHAiOjE2OTk5MTQzMzQsImlhdCI6MTY5OTg3ODMzNCwianRpIjoiZmRhNmMwYTYtODM1ZC00ZmVhLWI4MDEtYjIxODgwZTZlMGEwIiwiaXNzIjoiaHR0cDovL2ZhYm9zLmxvY2FsOjcwODAvYXV0aC9yZWFsbXMvZmFib3MiLCJzdWIiOiIyOTUxNDRkNC1lYjYyLTQ4N2UtYTQ5OS0zZWJkYmExZTViYjEiLCJ0eXAiOiJTZXJpYWxpemVkLUlEIiwic2Vzc2lvbl9zdGF0ZSI6IjkyOGZjNDk4LTA0NDQtNDFmZi1iMDhjLTM0YTg2NzQ0ODEwZCIsInNpZCI6IjkyOGZjNDk4LTA0NDQtNDFmZi1iMDhjLTM0YTg2NzQ0ODEwZCIsInN0YXRlX2NoZWNrZXIiOiJUdzA5dDNrVnZFNjhhV056Nmt6bGJoSk1nT2FhWlc0RUc1R1M4NEo1TTRNIn0.b0bb5_P-YbblgTpuyUtfEWdnLYK-X9c51eab21kPRbo";
//        this.awxClient.getSessionIdAndCRSPToken(token);

        var k = mock(KeycloakPrincipal.class);
        var v = mock(KeycloakSecurityContext.class);
        when(k.getKeycloakSecurityContext()).thenReturn(v);
        when(v.getTokenString()).thenReturn(token);

        Results<JobTemplate> resultsJobTemplates = this.awxClient.getJobTemplateFromProjectIdAndPlaybook(
                new AwxCredential(k),
                19,
                "install.yml"
        );

    }

}
