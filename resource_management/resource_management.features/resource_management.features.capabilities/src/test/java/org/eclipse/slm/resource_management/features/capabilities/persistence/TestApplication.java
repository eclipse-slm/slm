package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@EntityScan(basePackages = "org.eclipse.slm.resource_management.features.capabilities")
@ActiveProfiles("test")
public class TestApplication {}
