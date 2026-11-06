package org.eclipse.slm.service_management.service.app;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceManagementOpenApiCustomizerTest {
    @Test
    void suppliesTheServiceOfferingIdForInheritedSubmodelEndpoints() {
        String path = "/services/offerings/{serviceOfferingId}/versions/{aasId}/aas/submodels";
        var submodels = new PathItem().get(new Operation());
        var unrelated = new PathItem().get(new Operation());
        var api = new OpenAPI().paths(new Paths().addPathItem(path, submodels).addPathItem("/unrelated", unrelated));

        new ServiceManagementOpenApiCustomizer().customise(api);

        assertEquals(1, submodels.getParameters().size());
        var parameter = submodels.getParameters().getFirst();
        assertEquals("serviceOfferingId", parameter.getName());
        assertEquals("path", parameter.getIn());
        assertTrue(parameter.getRequired());
        assertEquals("uuid", parameter.getSchema().getFormat());
        assertTrue(unrelated.getParameters() == null || unrelated.getParameters().isEmpty());
    }
}
