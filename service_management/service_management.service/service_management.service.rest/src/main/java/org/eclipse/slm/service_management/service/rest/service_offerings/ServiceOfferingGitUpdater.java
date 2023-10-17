package org.eclipse.slm.service_management.service.rest.service_offerings;

import org.eclipse.slm.common.utils.files.FilesUtil;
import org.eclipse.slm.common.utils.serviceofferingimport.ServiceOfferingUtil;
import org.eclipse.slm.common.utils.serviceofferingimport.ServiceOfferingVersionUtil;
import org.eclipse.slm.service_management.model.exceptions.ServiceOfferingReferencedFileNotFound;
import org.eclipse.slm.service_management.model.offerings.*;
import org.eclipse.slm.service_management.service.rest.service_categories.ServiceCategoryHandler;
import org.eclipse.slm.service_management.service.rest.service_categories.ServiceCategoryNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionCreateException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingGitRepositoryJpaRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ServiceOfferingGitUpdater implements ServiceOfferingGitUpdateTaskListener {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingGitUpdater.class);

    private final int gitUpdateIntervalMinutes;

    private String gitCloneDirectory;

    private Map<UUID, ServiceOfferingGitUpdateTask> serviceOfferingGitRepositoryIdToUpdateTask = new HashMap<>();

    private Map<ServiceOfferingGitUpdateTask, ServiceOfferingGitRepository> serviceOfferingGitUpdateTaskToServiceOfferingGitRepository = new HashMap<>();

    private Map<ServiceOfferingGitRepository, Git> serviceOfferingGitRepositoryToGit = new HashMap<>();

    private ServiceOfferingGitUpdaterListener serviceOfferingGitUpdaterListener;

    private final ServiceCategoryHandler serviceCategoryHandler;

    private final ServiceOfferingGitRepositoryJpaRepository serviceOfferingGitRepositoryJpaRepository;

    @Autowired
    public ServiceOfferingGitUpdater(
            @Value("${git.service-offering-importer.updater-interval-minutes}") int gitUpdateIntervalMinutes,
            @Value("${git.service-offering-importer.clone-directory}") String gitCloneDirectory, ServiceCategoryHandler serviceCategoryHandler, ServiceOfferingGitRepositoryJpaRepository serviceOfferingGitRepositoryJpaRepository) {
        this.gitUpdateIntervalMinutes = gitUpdateIntervalMinutes;
        this.gitCloneDirectory = gitCloneDirectory;
        this.serviceCategoryHandler = serviceCategoryHandler;
        this.serviceOfferingGitRepositoryJpaRepository = serviceOfferingGitRepositoryJpaRepository;
    }

    public void setServiceOfferingGitUpdaterListener(ServiceOfferingGitUpdaterListener serviceOfferingGitUpdaterListener) {
        this.serviceOfferingGitUpdaterListener = serviceOfferingGitUpdaterListener;
    }

    public ServiceOfferingGitRepository initServiceOfferingFromGitRepo(ServiceOfferingGitRepository serviceOfferingGitRepository)
            throws ServiceOfferingVersionCreateException, ServiceOfferingNotFoundException, ServiceVendorNotFoundException, ServiceCategoryNotFoundException, ServiceOfferingVersionNotFoundException {
        var serviceOfferingId = UUID.randomUUID();
        var gitRepo = this.cloneGitRepo(serviceOfferingGitRepository);
        this.serviceOfferingGitRepositoryToGit.put(serviceOfferingGitRepository, gitRepo);

        this.fetchGitRepo(serviceOfferingGitRepository);
        var tags = this.getGitTags(serviceOfferingGitRepository);

        if (serviceOfferingGitRepository.getServiceOffering() != null) {
            var deletedVersionTags = new ArrayList<String>();
            for (var serviceOfferingVersion : serviceOfferingGitRepository.getServiceOffering().getVersions()) {
                if (!tags.contains(serviceOfferingVersion.getVersion())) {
                    deletedVersionTags.add(serviceOfferingVersion.getVersion());
                }
            }
            serviceOfferingGitRepository = this.handleDeletedTags(serviceOfferingGitRepository, deletedVersionTags);
        }

        serviceOfferingGitRepository = this.checkoutTagsAndCreateDTOs(tags, serviceOfferingGitRepository);
        var updateTask = new ServiceOfferingGitUpdateTask(
                gitRepo,
                this.getCredentialsProviderForServiceOfferingGitRepository(serviceOfferingGitRepository),
                this.gitUpdateIntervalMinutes,
                this);
        this.serviceOfferingGitRepositoryIdToUpdateTask.put(serviceOfferingGitRepository.getId(), updateTask);
        this.serviceOfferingGitUpdateTaskToServiceOfferingGitRepository.put(updateTask, serviceOfferingGitRepository);

        return serviceOfferingGitRepository;
    }

    public void removeServiceOfferingGitRepository(ServiceOfferingGitRepository serviceOfferingGitRepository) {
        var updateTask = this.serviceOfferingGitRepositoryIdToUpdateTask.get(serviceOfferingGitRepository.getId());
        updateTask.stop();
        this.serviceOfferingGitRepositoryIdToUpdateTask.remove(serviceOfferingGitRepository.getId());
        this.serviceOfferingGitUpdateTaskToServiceOfferingGitRepository.remove(updateTask);
    }

    public ServiceOfferingGitRepository checkoutTagsAndCreateDTOs(List<String> tagNames, ServiceOfferingGitRepository serviceOfferingGitRepository)
            throws ServiceOfferingVersionCreateException, ServiceOfferingNotFoundException, ServiceVendorNotFoundException,
            ServiceCategoryNotFoundException, ServiceOfferingVersionNotFoundException {
        var gitRepo = this.serviceOfferingGitRepositoryToGit.get(serviceOfferingGitRepository);

        String serviceOfferingPath = gitRepo.getRepository().getWorkTree() + "/fabos.yaml";
        ServiceOfferingCreateOrUpdateRequest serviceOfferingCreateOrUpdateRequest = null;
        var serviceOfferingVersions = new ArrayList<ServiceOfferingVersionDTOApi>();
        for (var tagName : tagNames) {
            if(!isVersionTag(serviceOfferingGitRepository, tagName)) {
                LOG.info("Tag '" + tagName + "' of git repo '" + serviceOfferingGitRepository.getRepositoryUrl() + "' not " +
                        "matching regular expression '" + serviceOfferingGitRepository.getGitTagRegEx() + "' --> Ignoring tag");
                continue;
            }

            if (serviceOfferingGitRepository.getServiceOffering() != null) {
                var serviceOfferingVersionForTagOptional = serviceOfferingGitRepository.getServiceOffering()
                        .getVersions().stream().filter(svo -> svo.getVersion().equals(tagName)).findAny();
                if (serviceOfferingVersionForTagOptional.isPresent()) {
                    LOG.info("For tag '" + tagName + "' of git repo '" + serviceOfferingGitRepository.getRepositoryUrl()
                            + "' the service offering version with id '" +
                            serviceOfferingVersionForTagOptional.get().getId() + "' already exists --> Ignoring tag");
                    continue;
                }
            }

            try {
                gitRepo.fetch()
                        .setRemote("origin")
                        .setCredentialsProvider(this.getCredentialsProviderForServiceOfferingGitRepository(serviceOfferingGitRepository))
                        .setTagOpt(TagOpt.FETCH_TAGS)
                        .call();
                gitRepo.checkout()
                        .setName(tagName)
                        .call();

                var serviceOfferingDTOFileImport = FilesUtil.loadFromFile(
                        new File(serviceOfferingPath), ServiceOfferingDTOFileImport.class);

                var version = tagName.replace("refs/tags/", "");
                var tagList = gitRepo.tagList().call();
                var tag = tagList.stream().filter(t -> t.getName().endsWith(tagName)).findAny().get();
                var revWalk = new RevWalk(gitRepo.getRepository());
                var tagDate = revWalk.parseTag(tag.getObjectId()).getTaggerIdent().getWhen();

                serviceOfferingDTOFileImport.getVersion().setVersion(version);

                ServiceCategory serviceCategory;
                try {
                    var serviceCategoriesWithName = serviceCategoryHandler
                            .getServiceCategoriesByName(serviceOfferingDTOFileImport.getServiceCategoryName());
                    serviceCategory = serviceCategoriesWithName.get(0);
                } catch (ServiceCategoryNotFoundException e) {
                    serviceCategory = new ServiceCategory(serviceOfferingDTOFileImport.getServiceCategoryName());
                    serviceCategory = serviceCategoryHandler.createServiceCategory(serviceCategory);
                }

                serviceOfferingCreateOrUpdateRequest = ServiceOfferingUtil.convertServiceOfferingDTOFileImportToDTOApi(
                        serviceOfferingDTOFileImport, gitRepo.getRepository().getWorkTree().getAbsolutePath(), serviceCategoryHandler.getServiceCategories());

                var serviceOfferingVersionDTOApi = ServiceOfferingVersionUtil.convertDTOFileImportToDTOApi(
                        serviceOfferingDTOFileImport.getVersion(), gitRepo.getRepository().getWorkTree());
                if (serviceOfferingVersionDTOApi.getId() == null) {
                    serviceOfferingVersionDTOApi.setId(UUID.randomUUID());
                }
                serviceOfferingVersionDTOApi.setCreated(tagDate);

                serviceOfferingVersions.add(serviceOfferingVersionDTOApi);

            } catch (ServiceOfferingReferencedFileNotFound | GitAPIException | IOException e) {
                LOG.error("Unable to create Service Offering for tag '" + tagName + "' of git repo '"
                        + serviceOfferingGitRepository.getRepositoryUrl() + "': " + e.getMessage());
            }
        }

        if (serviceOfferingCreateOrUpdateRequest != null) {
            if (serviceOfferingGitRepository.getServiceOffering() != null) {
                serviceOfferingCreateOrUpdateRequest.setId(serviceOfferingGitRepository.getServiceOffering().getId());
            }
            serviceOfferingCreateOrUpdateRequest.setGitRepository(serviceOfferingGitRepository);
            var serviceOffering = this.serviceOfferingGitUpdaterListener.onNewServiceOfferingVersionsDetected(
                    this, serviceOfferingCreateOrUpdateRequest, serviceOfferingVersions);
            serviceOfferingGitRepository.setServiceOffering(serviceOffering);
        }

        return serviceOfferingGitRepository;
    }

    private Git cloneGitRepo(ServiceOfferingGitRepository serviceOfferingGitRepository) {
        if (!this.gitCloneDirectory.endsWith("/")) {
            this.gitCloneDirectory += "/";
        }

        var repoDirPath = this.gitCloneDirectory + serviceOfferingGitRepository.getId();
        var repoDir = new File(repoDirPath);

        if (repoDir.exists()) {
            try {
                var git = Git.open(repoDir);
                return git;
            } catch (IOException e) {
                LOG.error("Unable to import service offering from git repo '"
                        + serviceOfferingGitRepository.getRepositoryUrl() + "': " + e.getMessage());
                return null;
            }
        }
        else {
            repoDir.mkdirs();
            try {
                var credentialsProvider = this.getCredentialsProviderForServiceOfferingGitRepository(serviceOfferingGitRepository);
                var git = Git.cloneRepository()
                        .setURI(serviceOfferingGitRepository.getRepositoryUrl())
                        .setCredentialsProvider(credentialsProvider)
                        .setDirectory(repoDir)
                        .call();

                return git;

            } catch (Exception e) {
                LOG.error("Unable to import service offering from git repo '"
                        + serviceOfferingGitRepository.getRepositoryUrl() + "': " + e.getMessage());
                return null;
            }
        }
    }

    private void fetchGitRepo(ServiceOfferingGitRepository serviceOfferingGitRepository) {
        var gitRepo = this.serviceOfferingGitRepositoryToGit.get(serviceOfferingGitRepository);
        try {
            var credentialsProvider = this.getCredentialsProviderForServiceOfferingGitRepository(serviceOfferingGitRepository);
            var gitFetchResult = gitRepo.fetch()
                    .setRemote("origin")
                    .setCredentialsProvider(credentialsProvider)
                    .call();

        } catch (GitAPIException e) {
            LOG.error("Unable to pull git repo '" + serviceOfferingGitRepository.getRepositoryUrl() + "'");
        }
    }

    private List<String> getGitTags(ServiceOfferingGitRepository serviceOfferingGitRepository) {
        var gitRepo = this.serviceOfferingGitRepositoryToGit.get(serviceOfferingGitRepository);
        List<String> tags;
        try {
            var credentialsProvider = this.getCredentialsProviderForServiceOfferingGitRepository(serviceOfferingGitRepository);
            var remoteTagRefs = gitRepo.lsRemote()
                    .setRemote("origin")
                    .setCredentialsProvider(credentialsProvider)
                    .setHeads(false)
                    .setTags(true)
                    .call();
            tags = remoteTagRefs.stream().map(tagRef -> tagRef.getName().replace("refs/tags/", "")).collect(Collectors.toList());
        } catch (GitAPIException e) {
            LOG.error("Unable to get tags of git repo '" + gitRepo.getRepository().getWorkTree().getAbsolutePath() + "': " + e);
            tags = new ArrayList<>();
        }

        return tags;
    }

    private CredentialsProvider getCredentialsProviderForServiceOfferingGitRepository(ServiceOfferingGitRepository serviceOfferingGitRepository) {
        if (!serviceOfferingGitRepository.getGitUsername().isEmpty()
                && !serviceOfferingGitRepository.getGitPassword().isEmpty()) {
            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    serviceOfferingGitRepository.getGitUsername(), serviceOfferingGitRepository.getGitPassword());

            return credentialsProvider;
        }

        return null;
    }

    private boolean isVersionTag(ServiceOfferingGitRepository serviceOfferingGitRepository, String tagName) {
        var regExPattern = Pattern.compile(serviceOfferingGitRepository.getGitTagRegEx());
        var matcher = regExPattern.matcher(tagName);
        return matcher.find();
    }

    @Override
    public void onNewTagsDetected(Object sender, List<String> newTags) {
        var serviceOfferingGitRepository = serviceOfferingGitUpdateTaskToServiceOfferingGitRepository.get(sender);
        try {
            this.checkoutTagsAndCreateDTOs(newTags, serviceOfferingGitRepository);
        } catch (ServiceOfferingVersionCreateException | ServiceOfferingNotFoundException |
                 ServiceVendorNotFoundException | ServiceCategoryNotFoundException |
                 ServiceOfferingVersionNotFoundException e) {
            LOG.error("Unable to add service offering versions for new tags '" + newTags + "': " + e.getMessage());
        }
    }

    private ServiceOfferingGitRepository handleDeletedTags(ServiceOfferingGitRepository serviceOfferingGitRepository, List<String> deletedTags) {
        try {
            this.serviceOfferingGitUpdaterListener.onServiceOfferingVersionTagsDeleted(
                    this, serviceOfferingGitRepository.getServiceOffering().getId(), deletedTags);
        } catch (ServiceOfferingNotFoundException | ServiceOfferingVersionNotFoundException | ServiceVendorNotFoundException | ServiceCategoryNotFoundException e) {
            LOG.error("Unable to delete service offering versions for delete tags '" + deletedTags + "': " + e.getMessage());
        }

        return serviceOfferingGitRepository;
    }

    @Override
    public void onTagsDeleted(Object sender, List<String> deletedTags) {
        var serviceOfferingGitRepository = serviceOfferingGitUpdateTaskToServiceOfferingGitRepository.get(sender);
        this.handleDeletedTags(serviceOfferingGitRepository, deletedTags);
    }
}
