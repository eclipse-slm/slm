package org.eclipse.slm.tests;

import org.eclipse.slm.tests.stack.*;
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
        ResourceManagementTests.class,
        ServiceManagementTests.class,
        DcDockerTest.class
})
public class StackTestSuite {

}
