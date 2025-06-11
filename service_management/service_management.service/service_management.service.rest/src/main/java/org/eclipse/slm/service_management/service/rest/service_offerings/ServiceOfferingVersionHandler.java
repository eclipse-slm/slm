package org.eclipse.slm.service_management.service.rest.service_offerings;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.slm.common.minio.client.MinioClient;
import org.eclipse.slm.common.minio.model.exceptions.*;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.offerings.ServiceOffering;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingCreateOrUpdateRequest;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersionDTOApi;
import org.eclipse.slm.service_management.model.offerings.codesys.CodesysDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionCreateException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingGitRepositoryJpaRepository;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingVersionJpaRepository;
import org.eclipse.slm.service_management.service.rest.service_categories.ServiceCategoryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class ServiceOfferingVersionHandler implements ServiceOfferingGitUpdaterListener {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingVersionHandler.class);

    private final ApplicationEventPublisher publisher;
    private final ServiceOfferingVersionJpaRepository serviceOfferingVersionJpaRepository;

    private final ServiceOfferingHandler serviceOfferingHandler;

    private final ServiceOfferingGitUpdater serviceOfferingGitUpdater;

    private final ServiceOfferingGitRepositoryJpaRepository serviceOfferingGitRepositoryJpaRepository;

    private final MinioClient minioClient;

    public ServiceOfferingVersionHandler(ServiceOfferingVersionJpaRepository serviceOfferingVersionJpaRepository,
                                         ServiceOfferingHandler serviceOfferingHandler,
                                         ServiceOfferingGitUpdater serviceOfferingGitUpdater,
                                         ServiceOfferingGitRepositoryJpaRepository serviceOfferingGitRepositoryJpaRepository,
                                         ApplicationEventPublisher publisher,
                                         MinioClient minioClient) {
        this.serviceOfferingVersionJpaRepository = serviceOfferingVersionJpaRepository;
        this.serviceOfferingHandler = serviceOfferingHandler;
        this.serviceOfferingGitUpdater = serviceOfferingGitUpdater;
        this.serviceOfferingGitRepositoryJpaRepository = serviceOfferingGitRepositoryJpaRepository;
        this.serviceOfferingGitUpdater.setServiceOfferingGitUpdaterListener(this);
        this.publisher = publisher;
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void init() throws ServiceOfferingVersionCreateException, ServiceOfferingNotFoundException, ServiceVendorNotFoundException, ServiceCategoryNotFoundException, ServiceOfferingVersionNotFoundException {
        var serviceOfferingGitRepos = this.serviceOfferingGitRepositoryJpaRepository.findAll();
        for (var serviceOfferingGitRepo : serviceOfferingGitRepos) {
            this.serviceOfferingGitUpdater.initServiceOfferingFromGitRepo(serviceOfferingGitRepo);
        }
    }

    public List<ServiceOfferingVersion> getServiceOfferingVersionsOfServiceOffering(UUID serviceOfferingId)
            throws ServiceOfferingNotFoundException {
        // Check if service offering exists
        var serviceOffering = this.serviceOfferingHandler.getServiceOfferingById(serviceOfferingId);

        var serviceOfferingVersions = this.serviceOfferingVersionJpaRepository
                .findByServiceOfferingId(serviceOffering.getId());

        return serviceOfferingVersions;
    }

    public ServiceOfferingVersion getServiceOfferingVersionById(UUID serviceOfferingId,
                                                                          UUID serviceOfferingVersionId)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        // Check if service offering exists
        var serviceOffering = this.serviceOfferingHandler.getServiceOfferingById(serviceOfferingId);

        var serviceOfferingVersionOptional = this.serviceOfferingVersionJpaRepository
                .findById(serviceOfferingVersionId);

        if (serviceOfferingVersionOptional.isPresent()) {
            return serviceOfferingVersionOptional.get();
        }
        else {
            throw new ServiceOfferingVersionNotFoundException(serviceOfferingVersionId);
        }
    }

    public ServiceOfferingVersion createServiceOfferingVersionWithAutoGeneratedId(
            ServiceOfferingVersionDTOApi serviceOfferingVersionDTOApi)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionCreateException {
        var serviceOffering = this.serviceOfferingHandler
                .getServiceOfferingById(serviceOfferingVersionDTOApi.getServiceOfferingId());

        serviceOfferingVersionDTOApi.setId(UUID.randomUUID());
        var serviceOfferingVersion = ObjectMapperUtils.map(serviceOfferingVersionDTOApi, ServiceOfferingVersion.class);
        serviceOfferingVersion.setServiceOffering(serviceOffering);

        return this.createOrUpdateServiceOfferingVersionWithId(serviceOfferingVersion);
    }

    public ServiceOfferingVersion createServiceOfferingVersionWithAutoGeneratedId(
            ServiceOfferingVersion serviceOfferingVersion)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionCreateException {
        serviceOfferingVersion.setId(UUID.randomUUID());
        return this.createOrUpdateServiceOfferingVersionWithId(serviceOfferingVersion);
    }

    public ServiceOfferingVersion createOrUpdateServiceOfferingVersionWithId(ServiceOfferingVersionDTOApi serviceOfferingVersionDTOApi)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionCreateException {
        var serviceOffering = this.serviceOfferingHandler
                .getServiceOfferingById(serviceOfferingVersionDTOApi.getServiceOfferingId());

        var serviceOfferingVersion = ObjectMapperUtils.map(serviceOfferingVersionDTOApi, ServiceOfferingVersion.class);
        serviceOfferingVersion.setServiceOffering(serviceOffering);

        return this.createOrUpdateServiceOfferingVersionWithId(serviceOfferingVersion);
    }

    public ServiceOfferingVersion createOrUpdateServiceOfferingVersionWithId(ServiceOfferingVersion serviceOfferingVersion)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionCreateException {
        // Check if service offering exists
        var serviceOffering = this.serviceOfferingHandler
                .getServiceOfferingById(serviceOfferingVersion.getServiceOffering().getId());

        var serviceOfferingVersionOptional = this.serviceOfferingVersionJpaRepository
                                                            .findById(serviceOfferingVersion.getId());
        try {
            if (serviceOfferingVersionOptional.isPresent()) {
                var savedServiceOfferingVersion = serviceOfferingVersionOptional.get();
                var updateServiceOfferingVersionToSafe = ObjectMapperUtils.map(serviceOfferingVersion, savedServiceOfferingVersion);
                serviceOfferingVersion = this.serviceOfferingVersionJpaRepository.save(updateServiceOfferingVersionToSafe);
                serviceOffering.getVersions().add(serviceOfferingVersion);
                this.serviceOfferingHandler.saveServiceOffering(serviceOffering);
                LOG.info("Service offering version " + serviceOfferingVersion + " of service offering " + serviceOffering + " updated");
                publisher.publishEvent(new ServiceOfferingVersionEvent(this, serviceOfferingVersion.getId(), ServiceOfferingVersionEvent.Operation.UPDATE));
            } else {
                serviceOfferingVersion.setServiceOffering(serviceOffering);
                serviceOffering.getVersions().add(serviceOfferingVersion);
                serviceOffering = this.serviceOfferingHandler.saveServiceOffering(serviceOffering);
                LOG.info("Service offering version " + serviceOfferingVersion + " added to service offering " + serviceOffering);
                publisher.publishEvent(new ServiceOfferingVersionEvent(this, serviceOfferingVersion.getId(), ServiceOfferingVersionEvent.Operation.CREATE));
            }
        } catch (DataIntegrityViolationException e) {
            var errorMessage = e.getCause().getCause().getMessage();
            if (errorMessage.contains("Duplicate entry")
                    && errorMessage.contains("UniqueVersionNamePerServiceOffering")) {
                throw new ServiceOfferingVersionCreateException("Service offering " + serviceOffering + " has already a version '"
                        + serviceOfferingVersion.getVersion() + "'");
            } else {
                throw e;
            }
        }

        return serviceOfferingVersion;
    }

    public void createOrUpdateServiceOfferingFile(UUID serviceOfferingId, UUID serviceOfferingVersionId, MultipartFile file) throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, MinioRemoveObjectException, MinioUploadException, MinioBucketCreateException, MinioObjectPathNameException, MinioBucketNameException {

        if(file.isEmpty()){
            throw new IllegalArgumentException("File is empty");
        }

        var serviceOffering = this.serviceOfferingHandler
                .getServiceOfferingById(serviceOfferingId);
        var serviceOfferingVersionOptional = this.serviceOfferingVersionJpaRepository
                .findById(serviceOfferingVersionId);

        if(serviceOfferingVersionOptional.isEmpty()){
            throw new ServiceOfferingVersionNotFoundException(serviceOfferingVersionId);
        }

        var serviceOfferingVersion = serviceOfferingVersionOptional.get();
        var deploymentDefinition = serviceOfferingVersionOptional.get().getDeploymentDefinition();
        if( deploymentDefinition != null && deploymentDefinition.getDeploymentType() == DeploymentType.CODESYS){

            if(!FilenameUtils.getExtension(file.getOriginalFilename()).equals("zip")){
                throw new IllegalArgumentException("File Format not supported");
            }

            var bucketName = "slm";
            var path = "/service-offering/" + serviceOfferingId + "/version/" + serviceOfferingVersionId + "/";
            var objectName = path + "application.zip";
            var codesysDeploymentDefinition = (CodesysDeploymentDefinition)deploymentDefinition;

            if (!codesysDeploymentDefinition.getApplicationPath().isEmpty() && minioClient.objectExist(bucketName, codesysDeploymentDefinition.getApplicationPath())){
                minioClient.removeObject(bucketName, codesysDeploymentDefinition.getApplicationPath());
            }

            try {
                minioClient.putObject(bucketName, objectName, file.getInputStream(), file.getSize(), file.getContentType());
            } catch (IOException e) {
                throw new MinioUploadException(bucketName, objectName);
            }

            codesysDeploymentDefinition.setApplicationPath(objectName);
            serviceOfferingVersion.setDeploymentDefinition(codesysDeploymentDefinition);

            serviceOfferingVersion = this.serviceOfferingVersionJpaRepository.save(serviceOfferingVersion);
            serviceOffering.getVersions().add(serviceOfferingVersion);
            this.serviceOfferingHandler.saveServiceOffering(serviceOffering);
        }

    }

    public ServiceOfferingFile getServiceOfferingFile(UUID serviceOfferingId, UUID serviceOfferingVersionId, String fileName) throws Exception {
        if(fileName.isEmpty()){
            throw new IllegalArgumentException("Filename is empty");
        }

        var serviceOffering = this.serviceOfferingHandler
                .getServiceOfferingById(serviceOfferingId);
        var serviceOfferingVersionOptional = this.serviceOfferingVersionJpaRepository
                .findById(serviceOfferingVersionId);

        if(serviceOfferingVersionOptional.isEmpty()){
            throw new ServiceOfferingVersionNotFoundException(serviceOfferingVersionId);
        }

        var deploymentDefinition = serviceOfferingVersionOptional.get().getDeploymentDefinition();
        if( deploymentDefinition != null && deploymentDefinition.getDeploymentType() == DeploymentType.CODESYS){
            var bucketName = "slm";
            var path = "/service-offering/" + serviceOfferingId + "/version/" + serviceOfferingVersionId + "/";
            var objectName = "application.zip";
            var codesysDeploymentDefinition = (CodesysDeploymentDefinition)deploymentDefinition;

            if (codesysDeploymentDefinition.getApplicationPath().isEmpty() &&
                    !minioClient.objectExist(bucketName, codesysDeploymentDefinition.getApplicationPath()) &&
                    (path + objectName).equals(codesysDeploymentDefinition.getApplicationPath())
            ){
                throw new IllegalArgumentException("Application Path is empty");
            }

            var inputStream = minioClient.getObject(bucketName, codesysDeploymentDefinition.getApplicationPath());
            return new ServiceOfferingFile(codesysDeploymentDefinition.getApplicationPath(), inputStream);
        }

        throw new IllegalArgumentException("No file found");
    }


    public void deleteServiceOfferingVersionById(UUID serviceOfferingId, UUID serviceOfferingVersionId)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        // Check if service offering and service offering version exist
        var serviceOffering = this.serviceOfferingHandler.getServiceOfferingById(serviceOfferingId);
        var serviceOfferingVersion
                = this.getServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersionId);
        serviceOffering.getVersions().remove(serviceOfferingVersion);

        if(serviceOfferingVersion.getDeploymentType() == DeploymentType.CODESYS){
            var deployment = (CodesysDeploymentDefinition)serviceOfferingVersion.getDeploymentDefinition();

            if(deployment != null && !deployment.getApplicationPath().isEmpty()){
                var bucket = "slm";
                try {
                    minioClient.removeObject(bucket, deployment.getApplicationPath());
                } catch (MinioRemoveObjectException | MinioObjectPathNameException | MinioBucketNameException e) {
                    LOG.error("Could not remove Object " + deployment.getApplicationPath(), e);
                }
            }
        }

        this.serviceOfferingVersionJpaRepository.deleteById(serviceOfferingVersionId);
        LOG.info("Service offering version " + serviceOfferingVersion + " deleted for service offering " + serviceOffering);
        publisher.publishEvent(new ServiceOfferingVersionEvent(this, serviceOfferingVersionId, ServiceOfferingVersionEvent.Operation.DELETE));
    }

    public void deleteServiceOfferingVersionByName(UUID serviceOfferingId, String serviceOfferingVersionName)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        var serviceOfferingVersions = this.serviceOfferingVersionJpaRepository
                .findByVersion(serviceOfferingVersionName);

        if (serviceOfferingVersions.size() == 0) {
            throw new ServiceOfferingVersionNotFoundException(serviceOfferingVersionName);
        }

        this.deleteServiceOfferingVersionById(serviceOfferingId, serviceOfferingVersions.get(0).getId());
    }

    @Override
    public ServiceOffering onNewServiceOfferingVersionsDetected(Object sender, ServiceOfferingCreateOrUpdateRequest serviceOfferingCreateOrUpdateRequest,
                                                                List<ServiceOfferingVersionDTOApi> newServiceOfferingVersions)
            throws ServiceVendorNotFoundException, ServiceCategoryNotFoundException, ServiceOfferingVersionCreateException, ServiceOfferingNotFoundException {
        if (serviceOfferingCreateOrUpdateRequest.getId() == null) {
            serviceOfferingCreateOrUpdateRequest.setId(UUID.randomUUID());
        }
        var serviceOffering = this.serviceOfferingHandler.createOrUpdateServiceOfferingWithId(serviceOfferingCreateOrUpdateRequest);

        for (var newServiceOfferingVersion : newServiceOfferingVersions) {
            newServiceOfferingVersion.setServiceOfferingId(serviceOffering.getId());
            this.createOrUpdateServiceOfferingVersionWithId(newServiceOfferingVersion);
        }

        serviceOffering = this.serviceOfferingHandler.getServiceOfferingById(serviceOffering.getId());

        return serviceOffering;
    }

    @Override
    public void onServiceOfferingVersionTagsDeleted(Object sender, UUID serviceOfferingId, List<String> serviceOfferingVersionNames)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException {
        for (var serviceOfferingVersionName : serviceOfferingVersionNames) {
            this.deleteServiceOfferingVersionByName(serviceOfferingId, serviceOfferingVersionName);
        }
    }
}
