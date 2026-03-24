package org.eclipse.slm.resource_management.common.resource_types;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.resource_management.common.resources.ResourcesManager;
import org.eclipse.slm.resource_management.common.resources.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/resources/types")
@Tag(name = "Resource Types")
public class ResourceTypesRestController {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceTypesRestController.class);

    private final ResourceTypesManager resourceTypesManager;

    @Autowired
    public ResourceTypesRestController(
            ResourcesManager resourcesManager, ResourceTypesManager resourceTypesManager
    ) {
        this.resourceTypesManager = resourceTypesManager;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary="Get available resource types")
    public Collection<ResourceType> getResourceTypes() {
        return resourceTypesManager.getResourceTypes();
    }
}
