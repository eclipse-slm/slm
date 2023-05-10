package org.eclipse.slm.common.utils.serviceofferingimport;

import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.exceptions.ServiceOfferingReferencedFileNotFound;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersionDTOApi;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersionDTOFileImport;
import org.eclipse.slm.service_management.model.offerings.docker.compose.DockerComposeDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.docker.compose.DockerComposeDeploymentDefinitionDTOFileImport;
import org.eclipse.slm.service_management.model.offerings.kubernetes.KubernetesDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.kubernetes.KubernetesDeploymentDefinitionDTOFileImport;
import kotlin.NotImplementedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

public class ServiceOfferingVersionUtil {

    static {
        DTOConfig.configureModelMapper();
    }

    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingVersionUtil.class);

    public static ServiceOfferingVersionDTOApi convertDTOFileImportToDTOApi(
            ServiceOfferingVersionDTOFileImport serviceOfferingVersionDTOFileImport, File directory)
            throws ServiceOfferingReferencedFileNotFound {

        if (serviceOfferingVersionDTOFileImport.getDeploymentDefinition().getDeploymentType().equals(DeploymentType.DOCKER_COMPOSE)) {
            return ServiceOfferingVersionUtil.convertDockerComposeServiceOfferingFromDTOFileImportToDTOApi(
                    serviceOfferingVersionDTOFileImport, directory);
        }
        if (serviceOfferingVersionDTOFileImport.getDeploymentDefinition().getDeploymentType().equals(DeploymentType.KUBERNETES)) {
            return ServiceOfferingVersionUtil.convertKubernetesServiceOfferingFromDTOFileImportToDTOApi(
                    serviceOfferingVersionDTOFileImport, directory);
        }
        else {
            throw new NotImplementedError();
        }

    }

    private static ServiceOfferingVersionDTOApi convertDockerComposeServiceOfferingFromDTOFileImportToDTOApi
            (ServiceOfferingVersionDTOFileImport serviceOfferingVersionDTOFileImport, File directory)
            throws ServiceOfferingReferencedFileNotFound {

        var serviceOfferingVersionDTOApi = ObjectMapperUtils.map(serviceOfferingVersionDTOFileImport, ServiceOfferingVersionDTOApi.class);
        var dockerComposeServiceOfferingInitFiles = directory.listFiles();

        var deploymentDefinitionDTOFileImport = (DockerComposeDeploymentDefinitionDTOFileImport)(serviceOfferingVersionDTOFileImport.getDeploymentDefinition());
        var deploymentDefinition = new DockerComposeDeploymentDefinition();

        // Compose file
        var composeFile = Arrays.stream(dockerComposeServiceOfferingInitFiles)
                .filter(f -> f.getName().equals(deploymentDefinitionDTOFileImport.getComposeFilename())).findAny();
        if (composeFile.isPresent()) {
            try {
                var composeFileContent = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(composeFile.get())));
                deploymentDefinition.setComposeFile(composeFileContent);
            } catch (IOException e) {
                throw new ServiceOfferingReferencedFileNotFound("Compose file '" + deploymentDefinitionDTOFileImport.getComposeFilename()
                        + "' in directory '" + directory.getAbsolutePath() + "' can't be read");
            }
        }

        // .env file
        var dotEnvFile = Arrays.stream(dockerComposeServiceOfferingInitFiles).filter(f -> f.getName().equals(".env")).findAny();
        if (dotEnvFile.isPresent()) {
            try {
                var dotEnvFileContent = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(dotEnvFile.get())));
                deploymentDefinition.setDotEnvFile(dotEnvFileContent);
            } catch (IOException e) {
                throw new ServiceOfferingReferencedFileNotFound("File '.env' in directory '"
                        + directory.getAbsolutePath() + "' can't be read");
            }
        }

        // env files
        var envFiles = new HashMap<String, String>();
        for (var envFilename : deploymentDefinitionDTOFileImport.getEnvFilenames()) {
            var envFile = Arrays.stream(dockerComposeServiceOfferingInitFiles)
                    .filter(f -> f.getName().equals(envFilename)).findAny();
            if (envFile.isPresent()) {
                try {
                    var envFileContent = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(envFile.get())));
                    envFiles.put(envFilename, envFileContent);
                } catch (IOException e) {
                    throw new ServiceOfferingReferencedFileNotFound("Env file '" + envFilename + "' in directory '"
                            + directory.getAbsolutePath() + "' can't be read");
                }
            }
        }
        deploymentDefinition.setEnvFiles(envFiles);

        serviceOfferingVersionDTOApi.setDeploymentDefinition(deploymentDefinition);

        return serviceOfferingVersionDTOApi;
    }


    private static ServiceOfferingVersionDTOApi convertKubernetesServiceOfferingFromDTOFileImportToDTOApi
            (ServiceOfferingVersionDTOFileImport serviceOfferingVersionDTOFileImport, File directory)
            throws ServiceOfferingReferencedFileNotFound {

        var serviceOfferingVersionDTOApi = ObjectMapperUtils.map(serviceOfferingVersionDTOFileImport, ServiceOfferingVersionDTOApi.class);
        var serviceOfferingInitFiles = directory.listFiles();

        var deploymentDefinitionDTOFileImport = (KubernetesDeploymentDefinitionDTOFileImport)(serviceOfferingVersionDTOFileImport.getDeploymentDefinition());
        var deploymentDefinition = new KubernetesDeploymentDefinition();

        // Manifest file
        var manifestFile = Arrays.stream(serviceOfferingInitFiles)
                .filter(f -> f.getName().equals(deploymentDefinitionDTOFileImport.getManifestFilename())).findAny();
        if (manifestFile.isPresent()) {
            try {
                var manifestFileContent = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(manifestFile.get())));
                deploymentDefinition.setManifestFile(manifestFileContent);
            } catch (IOException e) {
                throw new ServiceOfferingReferencedFileNotFound("Manifest file '" + deploymentDefinitionDTOFileImport.getManifestFilename()
                        + "' in directory '" + directory.getAbsolutePath() + "' can't be read");
            }
        }

        serviceOfferingVersionDTOApi.setDeploymentDefinition(deploymentDefinition);

        return serviceOfferingVersionDTOApi;
    }

}
