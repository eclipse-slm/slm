package org.eclipse.slm.common.database.testing;

import org.testcontainers.containers.MariaDBContainer;

public class DatabaseTestContainer extends MariaDBContainer<DatabaseTestContainer> {

    private static final String DOCKER_IMAGE_NAME = "mariadb:10.5";

    public DatabaseTestContainer() {
        super(DatabaseTestContainer.DOCKER_IMAGE_NAME);
    }
}
