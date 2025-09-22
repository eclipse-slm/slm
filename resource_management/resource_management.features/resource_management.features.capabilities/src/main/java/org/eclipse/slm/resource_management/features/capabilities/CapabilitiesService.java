package org.eclipse.slm.resource_management.features.capabilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.client.AwxProjectUpdateFailedException;
import org.eclipse.slm.common.awx.model.*;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotCreatedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceRuntimeException;
import org.eclipse.slm.resource_management.common.ports.ICapabilitiesService;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.exceptions.CapabilityNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityFilter;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceDTO;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceMapper;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilitiesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.ExecutionEnvironment;

import javax.net.ssl.SSLException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class CapabilitiesService implements ICapabilitiesService {
    private final static Logger LOG = LoggerFactory.getLogger(CapabilitiesService.class);

    private final CapabilitiesConsulClient capabilitiesConsulClient;
    private final SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient;
    private final AwxClient awxClient;
    private final CapabilityJpaRepository capabilityJpaRepository;
    private final MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient;

    private final List<String> jobTemplateCredentialNames = List.of("Consul", "HashiCorp Vault", "Minio");

    @Value("${awx.default-execution-environment}")
    private String defaultExecutionEnvironment;

    @Autowired
    public CapabilitiesService(
        CapabilitiesConsulClient capabilitiesConsulClient,
        SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient,
        MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient,
        AwxClient awxClient,
        CapabilityJpaRepository capabilityJpaRepository
    ) {
        this.singleHostCapabilitiesConsulClient = singleHostCapabilitiesConsulClient;
        this.multiHostCapabilitiesConsulClient = multiHostCapabilitiesConsulClient;
        this.capabilitiesConsulClient = capabilitiesConsulClient;
        this.awxClient = awxClient;
        this.capabilityJpaRepository = capabilityJpaRepository;
    }

    public List<Capability> getCapabilities() {
        return capabilityJpaRepository.findAll();
    }

    public List<Capability> getCapabilities(Optional<CapabilityFilter> capabilityFilter) {
        List<Capability> capabilitiesList = this.getCapabilities();

        if (capabilityFilter.isPresent()) {
            if (capabilityFilter.get().getCapabilityId() != null) {
                capabilitiesList = capabilitiesList.stream()
                        .filter(c -> c.getId().equals(capabilityFilter.get().getCapabilityId()))
                        .collect(Collectors.toList());
            }

            if (capabilityFilter.get().getCapabilityHostType() != null) {
                Predicate<Capability> filterCapabilities =
                        capabilityFilter.get().getCapabilityHostType().equals(CapabilityFilter.CapabilityHostType.SINGLE_HOST) ?
                                c -> c.getClusterMemberTypes().size() == 0 :
                                c -> c.getClusterMemberTypes().size() > 0;

                capabilitiesList = capabilitiesList
                        .stream()
                        .filter(filterCapabilities)
                        .collect(Collectors.toList());
            }
        }

        return capabilitiesList;
    }

    public Optional<Capability> getCapabilityByName(String name) {
        return getCapabilities().stream()
                .filter(c -> c.getName().equals(name)).findFirst();
    }

    public Capability getCapabilityByIdOrThrow(UUID capabilityId) {
        return getCapabilities().stream()
                .filter(c -> c.getId().equals(capabilityId)).findFirst()
                .orElseThrow(() -> new CapabilityNotFoundException(capabilityId));

    }

    public void addCapability(Capability capability)
            throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, ResourceNotCreatedException, JsonProcessingException, ResourceRuntimeException {

        var ee = createExecutionEnvironment(capability);

        capability.getActions().forEach((capabilityActionType, capabilityAction) -> {
                    if(capabilityAction.getActionClass().equals(AwxAction.class.getSimpleName())) {
                        AwxAction awxCapabilityAction = (AwxAction) capabilityAction;
                        try {
                            var executionEnvironmentName = defaultExecutionEnvironment;
                            if (ee.isPresent()){
                                executionEnvironmentName = ee.get().getName();
                            }
                            var jobTemplateCredentialNames = this.jobTemplateCredentialNames;
                            JobTemplate jobTemplate;
                            if(!awxCapabilityAction.getUsername().equals("") && !awxCapabilityAction.getPassword().equals("")) {
                                jobTemplate = awxClient.createJobTemplateAddExecuteRoleToDefaultTeamAddScmCredential(
                                        awxCapabilityAction.getAwxRepo(),
                                        awxCapabilityAction.getAwxBranch(),
                                        awxCapabilityAction.getPlaybook(),
                                        awxCapabilityAction.getUsername(),
                                        awxCapabilityAction.getPassword(),
                                        jobTemplateCredentialNames,
                                        executionEnvironmentName
                                );
                            } else {
                                jobTemplate = awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(
                                        awxCapabilityAction.getAwxRepo(),
                                        awxCapabilityAction.getAwxBranch(),
                                        awxCapabilityAction.getPlaybook(),
                                        jobTemplateCredentialNames,
                                        executionEnvironmentName);
                            }

                            List<SurveyItem> params = awxCapabilityAction.getParameter();
                            if(params != null) {
                                awxClient.createSurvey(
                                        jobTemplate.getId(),
                                        new Survey(
                                            "Survey",
                                        "Survey for "+jobTemplate.getName(),
                                                params
                                        )
                                );
                                awxClient.enableSurvey(jobTemplate.getId());
                            }
                        } catch (AwxProjectUpdateFailedException | SSLException | JsonProcessingException e) {
                            LOG.error("Failed to clone AWX Project from " + awxCapabilityAction.getAwxRepo() + " - " + awxCapabilityAction.getAwxBranch());
                        }
                    }
                });



        capabilityJpaRepository.save(capability);

        if (capability.getHealthCheck() != null) {
            singleHostCapabilitiesConsulClient.addSingleHostCapabilityWithHealthCheckToAllConsulNodes(
                    new ConsulCredential(),
                    capability,
                    false,
                    new HashMap<>()
            );
        }
    }

    private Optional<org.eclipse.slm.common.awx.model.ExecutionEnvironment> createExecutionEnvironment(Capability capability)
            throws ResourceNotCreatedException, JsonProcessingException, ResourceRuntimeException {
        if (capability.getExecutionEnvironment() != null) {

            var executionName = capability.getName() + "-EE";
            LOG.info("Create or Update Execution Environment with name " + executionName);

            var executionEnvironment = capability.getExecutionEnvironment();

            var organisationName = "Service Lifecycle Management";
            var resultOrganisations = this.awxClient.getOrganizationByName(organisationName);
            Organization organization = null;

            for (Organization org : resultOrganisations.getResults()) {
                if (org.getName().equals(organisationName)) {
                    organization = org;
                    break;
                }
            }

            if (organization == null) {
                throw new ResourceRuntimeException("Organization \"" + organisationName + "\" not found");
            }

            Credential credential = createRegistryCredential(capability, executionEnvironment, organization);

            var ee = this.awxClient.createOrUpdateExecutionEnvironment(new ExecutionEnvironmentCreate(
                    executionName,
                    executionEnvironment.getDescription(),
                    organization.getId(),
                    executionEnvironment.getImage(),
                    "false",
                    credential != null ? credential.getId() : null,
                    executionEnvironment.getPull().getPrettyName()
            ));

            if (ee.isEmpty()) {
                throw new ResourceNotCreatedException("Could not create Execution Environment for capability" + executionName);
            }
            return ee;
        }
        return Optional.empty();
    }

    private Credential createRegistryCredential(Capability capability, ExecutionEnvironment executionEnvironment, Organization organization)
            throws ResourceNotCreatedException {
        var newCredentials = executionEnvironment.getRegistryCredential();
        Credential credential = null;
        if (newCredentials != null) {

            if (newCredentials.getId() != null){
                credential = this.awxClient.getCredentialById(newCredentials.getId());

                if (credential == null) {
                    throw new ResourceNotCreatedException("Could not find Credential with Id "+newCredentials.getId()+" for Execution-Environment for capability: " + capability.getName());
                }
            }else{
                credential = this.awxClient.createCredential(new CredentialDTOApiCreate(
                        capability.getName() + "-Credential",
                        Objects.requireNonNullElse(newCredentials.getDescription(), ""),
                        organization.getId(),
                        17,
                        new HashMap<String, Object>() {{
                            put("host", newCredentials.getAuthenticationURL());
                            put("username", newCredentials.getUsername());
                            put("password", newCredentials.getPassword());
                            put("verify_ssl", newCredentials.getVerifySSL());
                        }}
                ));

                if (credential == null) {
                    throw new ResourceNotCreatedException("Could not create Credential for Execution-Environment for capability: " + capability.getName());
                }
            }
        }
        return credential;
    }

    public boolean deleteCapability(UUID capabilityId) throws ConsulLoginFailedException {
        var capabilityOptional = this.capabilityJpaRepository.findById(capabilityId);
        if (capabilityOptional.isPresent()) {
            deleteCapability(capabilityOptional.get());
            return true;
        }
        else {
            return false;
        }
    }

    private void deleteCapability(Capability capability)
            throws ConsulLoginFailedException {
        this.singleHostCapabilitiesConsulClient.removeCapabilityServiceFromAllConsulNodes(
                new ConsulCredential(),
                capability
        );
        this.capabilityJpaRepository.delete(capability);

        capability.getActions().forEach((capabilityActionType, capabilityAction) -> {
            if (capabilityAction.getActionClass().equals(AwxAction.class.getSimpleName())) {
                AwxAction awxCapabilityAction = (AwxAction) capabilityAction;
                awxClient.deleteJobTemplate(
                        awxCapabilityAction.getAwxRepo(),
                        awxCapabilityAction.getAwxBranch(),
                        awxCapabilityAction.getPlaybook()
                );

                awxClient.deleteProject(
                    awxCapabilityAction.getAwxRepo(),
                    awxCapabilityAction.getAwxBranch()
                );
            }
        });
        LOG.info("Capability with id '" + capability.getId()  + "' deleted");
    }

    public List<CapabilityServiceDTO> getAllCapabilityServices(JwtAuthenticationToken jwtAuthenticationToken) throws ResourceNotFoundException {
        var consulCredential = new ConsulCredential(jwtAuthenticationToken);

        try {
            var capabilityServicesOfResource = this.capabilitiesConsulClient.getCapabilityServices(consulCredential);
            var dtos = CapabilityServiceMapper.INSTANCE.toDtoList(capabilityServicesOfResource);

            return dtos;
        }
        catch (Exception e) {
            LOG.error("Failed to get all capability services: " + e);
            return List.of();
        }
    }

    public List<CapabilityServiceDTO> getCapabilityServicesOfResource(UUID resourceId) throws ResourceNotFoundException {
        try {
            var capabilityServicesOfResource = this.capabilitiesConsulClient.getCapabilityServicesOfResource(resourceId);
            var dtos = CapabilityServiceMapper.INSTANCE.toDtoList(capabilityServicesOfResource);

            return dtos;
        }
        catch (Exception e) {
            LOG.error("Failed to get capability services of resource: " + resourceId, e);
            return List.of();
        }
    }

    public boolean isCapabilityInstalledOnResource(UUID resourceId, UUID capabilityId) {
        var capabilityServicesOfResource = this.getCapabilityServicesOfResource(resourceId);
        var capabilityOptional = capabilityServicesOfResource.stream()
                .filter(cs -> cs.getCapabilityId().equals(capabilityId))
                .findAny();

        return capabilityOptional.isPresent();
    }

    //region ICapabilitiesService
    @Override
    public List<UUID> getCapabilityServiceIdsOfResource(UUID resourceId) throws ResourceNotFoundException {
        var capabilityServiceIds = this.getCapabilityServicesOfResource(resourceId)
                .stream()
                .map(CapabilityServiceDTO::getId)
                .toList();

        return capabilityServiceIds;
    }

    @Override
    public boolean isResourceClusterMember(UUID resourceId) throws ResourceNotFoundException {
        try {
            var multihostCapabilities = this.multiHostCapabilitiesConsulClient.getMultiHostCapabilityServicesOfResource(new ConsulCredential(), resourceId);

            return multihostCapabilities.size() > 0;

        } catch (ConsulLoginFailedException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion ICapabilitiesService
}
