package org.eclipse.slm.service_management.service.app;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;

/** Supplies the outer path variable omitted by springdoc on inherited AAS operations. */
class ServiceManagementOpenApiCustomizer implements OpenApiCustomizer {
    private static final String SUBMODEL_PREFIX = "/services/offerings/{serviceOfferingId}/versions/{aasId}/aas/";

    @Override
    public void customise(OpenAPI api) {
        if (api.getPaths() == null) {
            return;
        }
        api.getPaths().forEach((path, item) -> {
            if (path.startsWith(SUBMODEL_PREFIX) && (item.getParameters() == null || item.getParameters().stream()
                    .noneMatch(parameter -> "path".equals(parameter.getIn()) && "serviceOfferingId".equals(parameter.getName())))) {
                item.addParametersItem(new Parameter().name("serviceOfferingId").in("path").required(true)
                        .description("Id of the service offering").schema(new StringSchema().format("uuid")));
            }
        });
    }
}
