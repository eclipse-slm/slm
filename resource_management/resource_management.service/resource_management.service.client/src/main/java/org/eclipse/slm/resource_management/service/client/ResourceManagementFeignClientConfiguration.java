package org.eclipse.slm.resource_management.service.client;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("org.eclipse.slm.resource_management.service.client")
public class ResourceManagementFeignClientConfiguration {
}
