package org.eclipse.slm.platform_management.service.client;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("org.eclipse.slm.platform_management.service.client")
public class PlatformManagementFeignClientConfiguration {
}
