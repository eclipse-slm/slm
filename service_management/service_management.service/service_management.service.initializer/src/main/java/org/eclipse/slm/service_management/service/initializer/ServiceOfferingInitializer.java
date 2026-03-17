package org.eclipse.slm.service_management.service.initializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.aas.clients.base.FeignResponseException;
import org.eclipse.slm.common.utils.files.FilesUtil;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.common.utils.serviceofferingimport.ServiceOfferingUtil;
import org.eclipse.slm.common.utils.serviceofferingimport.ServiceOfferingVersionUtil;
import org.eclipse.slm.service_management.model.exceptions.ServiceOfferingReferencedFileNotFound;
import org.eclipse.slm.service_management.model.offerings.ServiceCategory;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingDTOFileImport;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersionDTOApi;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionCreateException;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.eclipse.slm.service_management.service.app.service_categories.ServiceCategoryNotFoundException;
import org.eclipse.slm.service_management.service.client.ServiceManagementClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.util.*;

@Component
public class ServiceOfferingInitializer extends AbstractInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingInitializer.class);

    private List<ServiceCategory> serviceCategories;

    public ServiceOfferingInitializer(ServiceManagementClientFactory serviceManagementClientFactory) {
        super(serviceManagementClientFactory);
    }

    public void init(String initDirectory) throws FileNotFoundException, JsonProcessingException {
        this.serviceCategories = this.serviceManagementClient.serviceCategories().getServiceCategories();
        this.initDockerContainerServiceOfferings(initDirectory);
        this.initDockerComposeServiceOfferings(initDirectory);
        this.initKubernetesServiceOfferings(initDirectory);

        LOG.info("Service offerings initialization finished");
    }

    private void initDockerComposeServiceOfferings(String initDirectory) throws JsonProcessingException {
        var dockerComposeServiceOfferingsInitBaseDirectory = initDirectory + "service-offerings/docker-compose/";
        var dockerComposeServiceOfferingInitDirectories = FilesUtil.findFiles(
                dockerComposeServiceOfferingsInitBaseDirectory, "", "");
        if (dockerComposeServiceOfferingInitDirectories.length == 0) {
            LOG.info("No init files in directory '" + dockerComposeServiceOfferingsInitBaseDirectory + "' found -> Skipping initialization of Docker Compose Service Offerings");
        }
        for (var dockerComposeServiceOfferingInitDirectory : dockerComposeServiceOfferingInitDirectories) {
            var dockerComposeServiceOfferingInitFiles = dockerComposeServiceOfferingInitDirectory.listFiles();

            var serviceOfferingDefinitionFile = Arrays.stream(dockerComposeServiceOfferingInitFiles)
                    .filter(f -> f.getName().equals("fabos.yaml") || f.getName().equals("fabos.yml")).findAny();
            if (serviceOfferingDefinitionFile.isPresent()) {

                var serviceOfferingDTOFileImport = FilesUtil.loadFromFile(
                        serviceOfferingDefinitionFile.get(), ServiceOfferingDTOFileImport.class);
                try {
                    var serviceOfferingDTOApi = ServiceOfferingUtil.convertServiceOfferingDTOFileImportToDTOApi(
                            serviceOfferingDTOFileImport, dockerComposeServiceOfferingInitDirectory.getAbsolutePath(), this.serviceCategories);
                    var responseCreateServiceOffering = this.serviceManagementClient.serviceOfferings().createOrUpdateServiceOfferingWithId(
                            serviceOfferingDTOApi.getId(), serviceOfferingDTOApi);

                    var serviceOfferingVersionDTOApi = ServiceOfferingVersionUtil
                            .convertDTOFileImportToDTOApi(serviceOfferingDTOFileImport.getVersion(), dockerComposeServiceOfferingInitDirectory);
                    serviceOfferingVersionDTOApi.setServiceOfferingId(responseCreateServiceOffering.getBody().getServiceOfferingId());
                    serviceOfferingVersionDTOApi.setCreated(null);
                    var responseCreateServiceOfferingVersion = this.serviceManagementClient.serviceOfferingVersions()
                            .createOrUpdateServiceOfferingVersionWithId(serviceOfferingVersionDTOApi.getServiceOfferingId(),
                                    serviceOfferingVersionDTOApi.getId(), serviceOfferingVersionDTOApi);

                } catch(NoSuchElementException e) {
                    LOG.error("Service category with name '" + serviceOfferingDTOFileImport.getServiceCategoryName() +"' of Docker Compose Service Offering '"
                            + dockerComposeServiceOfferingInitDirectory.getName() + "' is not defined --> Skipping import of service offering");
                } catch (ServiceOfferingReferencedFileNotFound e) {
                    LOG.error("File referenced in service offering definition of Docker Compose Service Offering '"
                            + dockerComposeServiceOfferingInitDirectory.getName() + "' not found --> Skipping import of service offering: " + e.getMessage());
                } catch (FeignResponseException e) {
                    var objectMapper = new ObjectMapper();
                    LOG.error("API call for service offering '" + objectMapper.writeValueAsString(serviceOfferingDTOFileImport) + "' failed: " +
                            "HTTP Code: " + e.getStatusCode() + " | Message: " + e.getMessage() + " | Body: " + e.getBody());
                } catch (ServiceVendorNotFoundException | ServiceCategoryNotFoundException | ServiceOfferingVersionCreateException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                LOG.error("File 'fabos.yaml' for Docker Compose Service Offering '"
                        + dockerComposeServiceOfferingInitDirectory.getName() + "' is missing --> Skipping import of service offering");
            }
        }
    }



    private void initKubernetesServiceOfferings(String initDirectory) {
        var kubernetesServiceOfferingsInitBaseDirectory = initDirectory + "service-offerings/kubernetes/";
        var kubernetesServiceOfferingInitDirectories = FilesUtil.findFiles(
                kubernetesServiceOfferingsInitBaseDirectory, "", "");
        if (kubernetesServiceOfferingInitDirectories.length == 0) {
            LOG.info("No init files in directory '" + kubernetesServiceOfferingsInitBaseDirectory + "' found -> Skipping initialization of Kubernetes Service Offerings");
        }
        for (var kubernetesServiceOfferingInitDirectory : kubernetesServiceOfferingInitDirectories) {
            var kubernetesServiceOfferingInitFiles = kubernetesServiceOfferingInitDirectory.listFiles();

            var serviceOfferingDefinitionFile = Arrays.stream(kubernetesServiceOfferingInitFiles)
                    .filter(f -> f.getName().equals("fabos.yaml") || f.getName().equals("fabos.yml")).findAny();
            if (serviceOfferingDefinitionFile.isPresent()) {

                var serviceOfferingDTOFileImport = FilesUtil.loadFromFile(
                        serviceOfferingDefinitionFile.get(), ServiceOfferingDTOFileImport.class);
                try {
                    var serviceOfferingDTOApi = ServiceOfferingUtil.convertServiceOfferingDTOFileImportToDTOApi(
                            serviceOfferingDTOFileImport, kubernetesServiceOfferingInitDirectory.getAbsolutePath(), this.serviceCategories);
                    var responseCreateServiceOffering = this.serviceManagementClient.serviceOfferings().createOrUpdateServiceOfferingWithId(
                            serviceOfferingDTOApi.getId(), serviceOfferingDTOApi);

                    var serviceOfferingVersionDTOApi = ServiceOfferingVersionUtil
                            .convertDTOFileImportToDTOApi(serviceOfferingDTOFileImport.getVersion(), kubernetesServiceOfferingInitDirectory);
                    serviceOfferingVersionDTOApi.setServiceOfferingId(responseCreateServiceOffering.getBody().getServiceOfferingId());
                    serviceOfferingVersionDTOApi.setCreated(null);
                    var responseCreateServiceOfferingVersion = this.serviceManagementClient.serviceOfferingVersions()
                            .createOrUpdateServiceOfferingVersionWithId(serviceOfferingVersionDTOApi.getServiceOfferingId(),
                                    serviceOfferingVersionDTOApi.getId(), serviceOfferingVersionDTOApi);

                } catch(NoSuchElementException e) {
                    LOG.error("Service category with name '" + serviceOfferingDTOFileImport.getServiceCategoryName() +"' of Kubernetes Service Offering '"
                            + kubernetesServiceOfferingInitDirectory.getName() + "' is not defined --> Skipping import of service offering");
                } catch (ServiceOfferingReferencedFileNotFound e) {
                    LOG.error("File referenced in service offering definition of Docker Compose Service Offering '"
                            + kubernetesServiceOfferingInitDirectory.getName() + "' not found --> Skipping import of service offering: " + e.getMessage());
                } catch (ServiceVendorNotFoundException | ServiceCategoryNotFoundException | ServiceOfferingVersionCreateException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                LOG.error("File 'fabos.yaml' for Docker Compose Service Offering '"
                        + kubernetesServiceOfferingInitDirectory.getName() + "' is missing --> Skipping import of service offering");
            }
        }
    }


    private void initDockerContainerServiceOfferings(String initDirectory) {
        var dockerContainerServiceOfferingsInitBaseDirectory = initDirectory + "service-offerings/docker-container/";
        var dockerContainerServiceOfferingInitDirectories = FilesUtil.findFiles(
                dockerContainerServiceOfferingsInitBaseDirectory, "", "");
        if (dockerContainerServiceOfferingInitDirectories.length == 0) {
            LOG.info("No init files in directory '" + dockerContainerServiceOfferingsInitBaseDirectory + "' found -> Skipping initialization of Docker Container Service Offerings");
        }
        for (var dockerContainerServiceOfferingInitDirectory : dockerContainerServiceOfferingInitDirectories) {
            var dockerContainerServiceOfferingInitFiles = dockerContainerServiceOfferingInitDirectory.listFiles();

            var serviceOfferingDefinitionFile = Arrays.stream(dockerContainerServiceOfferingInitFiles)
                    .filter(f -> f.getName().equals("fabos.yaml") || f.getName().equals("fabos.yml")).findAny();
            if (serviceOfferingDefinitionFile.isPresent()) {
                var serviceOfferingDTOFileImport = FilesUtil.loadFromFile(
                        serviceOfferingDefinitionFile.get(), ServiceOfferingDTOFileImport.class);
                try {
                    var serviceOfferingCreateOrUpdateRequest = ServiceOfferingUtil.convertServiceOfferingDTOFileImportToDTOApi(
                            serviceOfferingDTOFileImport, dockerContainerServiceOfferingInitDirectory.getAbsolutePath(), this.serviceCategories);
                    var responseCreateServiceOffering = this.serviceManagementClient.serviceOfferings().createOrUpdateServiceOfferingWithId(
                            serviceOfferingCreateOrUpdateRequest.getId(), serviceOfferingCreateOrUpdateRequest);

                    var serviceOfferingVersionDTOApi = ObjectMapperUtils.map(serviceOfferingDTOFileImport.getVersion(), ServiceOfferingVersionDTOApi.class);
                    serviceOfferingVersionDTOApi.setServiceOfferingId(responseCreateServiceOffering.getBody().getServiceOfferingId());
                    serviceOfferingVersionDTOApi.setCreated(null);
                    var responseCreateServiceOfferingVersion = this.serviceManagementClient.serviceOfferingVersions()
                            .createOrUpdateServiceOfferingVersionWithId(serviceOfferingVersionDTOApi.getServiceOfferingId(),
                                    serviceOfferingVersionDTOApi.getId(), serviceOfferingVersionDTOApi);
                } catch (NoSuchElementException e) {
                    LOG.error("Service category with name '" + serviceOfferingDTOFileImport.getServiceCategoryName() + "' of service offering '"
                            + dockerContainerServiceOfferingInitDirectory + "' is not defined --> Skipping import of service offering");
                } catch (ServiceOfferingReferencedFileNotFound e) {
                    LOG.error("Referenced file in service offering [name='" + serviceOfferingDTOFileImport.getName() + "'] not found " +
                            "--> Skipping import of service offering");
                } catch (ServiceVendorNotFoundException | ServiceCategoryNotFoundException | ServiceOfferingVersionCreateException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                LOG.error("File 'fabos.yaml' for Docker Compose Service Offering '"
                        + dockerContainerServiceOfferingInitDirectory.getName() + "' is missing --> Skipping import of service offering");
            }
        }
    }
}
