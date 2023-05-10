package org.eclipse.slm.service_management.service.initializer;

import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.common.utils.serviceofferingimport.DTOConfig;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingDTOApi;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingDTOFileImport;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersionDTOApi;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersionDTOFileImport;
import org.eclipse.slm.service_management.model.offerings.docker.container.DockerContainerDeploymentDefinitionDTOFileImport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class DTOTest {

    @BeforeAll
    public static void init()
    {
        DTOConfig.configureModelMapper();
    }

    @Test
    public void entityToDto() {

        DTOConfig.configureModelMapper();

        var deploymentDefinition = new DockerContainerDeploymentDefinitionDTOFileImport(UUID.randomUUID());
        deploymentDefinition.setImageRepository("myImage");
        deploymentDefinition.setImageTag("1.0.0");
        deploymentDefinition.setRestartPolicy("unless-stopped");

        var serviceOfferingVersionDTOFileImport = new ServiceOfferingVersionDTOFileImport();
        serviceOfferingVersionDTOFileImport.setId(UUID.randomUUID());
        serviceOfferingVersionDTOFileImport.setVersion("1.0.0");
        serviceOfferingVersionDTOFileImport.setDeploymentDefinition(deploymentDefinition);

        var serviceOfferingDTOFileImport = new ServiceOfferingDTOFileImport(
                "TestName", "TestDescription", "TestShortDescription", "TestCategry", UUID.randomUUID());
        serviceOfferingDTOFileImport.setVersion(serviceOfferingVersionDTOFileImport);

        var serviceOfferingDTOApi = ObjectMapperUtils.map(serviceOfferingDTOFileImport, ServiceOfferingDTOApi.class);
        var serviceOfferingVersionDTOApi = ObjectMapperUtils.map(serviceOfferingDTOFileImport.getVersion(), ServiceOfferingVersionDTOApi.class);
    }
}
