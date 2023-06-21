package de.fhg.ipa.ced.tests;

import de.fhg.ipa.ced.tests.stack.*;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Tests")
@SelectClasses({
        DockerStackTests.class,
        KeycloakTests.class,
        AWXTests.class,
        ConsulTests.class,
        VaultTests.class,
        NotificationServiceTests.class,
        ResourceRegistryTests.class,
        ServiceRegistryTests.class,
        DcDockerTest.class
})
public class StackTestSuite {

}
