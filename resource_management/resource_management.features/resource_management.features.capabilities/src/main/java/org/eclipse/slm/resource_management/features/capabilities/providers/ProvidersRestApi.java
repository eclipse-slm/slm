package org.eclipse.slm.resource_management.features.capabilities.providers;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

public interface ProvidersRestApi {

    @GetMapping("/service-hoster")
    @Operation(summary = "Get all service hoster")
    @ResponseBody
    List<ServiceHoster> getServiceHosters(@SpringQueryMap ServiceHosterFilter filter);
}
