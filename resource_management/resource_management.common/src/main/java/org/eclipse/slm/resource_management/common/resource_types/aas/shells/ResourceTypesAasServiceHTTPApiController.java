package org.eclipse.slm.resource_management.common.resource_types.aas.shells;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.aas.repositories.api.shells.AasServiceHTTPApiController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resources/types")
@Tag(name = "Resource Types")
public class ResourceTypesAasServiceHTTPApiController extends AasServiceHTTPApiController {


    protected ResourceTypesAasServiceHTTPApiController(ResourceTypesAasService aasService) {
        super(aasService);
    }

}
