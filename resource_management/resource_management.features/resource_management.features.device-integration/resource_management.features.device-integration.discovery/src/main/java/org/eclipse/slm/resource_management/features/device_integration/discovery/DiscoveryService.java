package org.eclipse.slm.resource_management.features.device_integration.discovery;

import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceRuntimeException;
import org.eclipse.slm.resource_management.common.resources.ResourcesManager;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver.DriverRegistryClient;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.exceptions.DriverNotFoundException;
import org.eclipse.slm.resource_management.features.device_integration.discovery.driver.DiscoveryDriverClientFactory;
import org.eclipse.slm.resource_management.features.device_integration.discovery.driver.DiscoveryJobListener;
import org.eclipse.slm.resource_management.features.device_integration.discovery.dto.DiscoveredResourceDTO;
import org.eclipse.slm.resource_management.features.device_integration.discovery.mapper.DiscoveredResourceMapper;
import org.eclipse.slm.resource_management.features.device_integration.discovery.messaging.DiscoveryJobEventType;
import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveryJob;
import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveryRequest;
import org.eclipse.slm.resource_management.features.device_integration.discovery.persistence.DiscoveryJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DiscoveryService implements DiscoveryJobListener {

    private final static Logger LOG = LoggerFactory.getLogger(DiscoveryService.class);

    private final DiscoveryJobRepository discoveryJobRepository;

    private final DriverRegistryClient driverRegistryClient;

    private final DiscoveryDriverClientFactory discoveryDriverClientFactory;

    private final DiscoveryEventMessageSender discoveryEventMessageSender;

    private final Map<UUID, JwtAuthenticationToken> jobIdToTokenMap = new HashMap<>();

    private final Map<UUID, String> jobIdToDriverId = new HashMap<>();

    private final ResourcesManager resourcesManager;

    public DiscoveryService(DiscoveryJobRepository discoveryJobRepository,
                            DriverRegistryClient driverRegistryClient,
                            DiscoveryDriverClientFactory discoveryDriverClientFactory,
                            DiscoveryEventMessageSender discoveryEventMessageSender,
                            ResourcesManager resourcesManager) {
        this.discoveryJobRepository = discoveryJobRepository;
        this.driverRegistryClient = driverRegistryClient;
        this.discoveryDriverClientFactory = discoveryDriverClientFactory;
        this.discoveryEventMessageSender = discoveryEventMessageSender;
        this.resourcesManager = resourcesManager;
    }

    public UUID discover(JwtAuthenticationToken jwtAuthenticationToken, String driverId, DiscoveryRequest discoveryRequest)
            throws DriverNotFoundException {
        var driverInfo = this.driverRegistryClient.getRegisteredDriver(driverId);
        var driverClient = this.discoveryDriverClientFactory.createDriverClient(driverInfo);
        var discoveryJob = driverClient.discover(this, discoveryRequest);

        jobIdToTokenMap.put(discoveryJob.getId(), jwtAuthenticationToken);
        jobIdToDriverId.put(discoveryJob.getId(), driverId);

        return discoveryJob.getId();
    }

    public Collection<DiscoveredResourceDTO> getDiscoveredResources(JwtAuthenticationToken jwtAuthenticationToken,
                                                                    boolean removeDuplicate,
                                                                    boolean onlyLatestJobs,
                                                                    boolean includeIgnored,
                                                                    boolean includedOnboarded) {
        var allDiscoveryJobs = this.discoveryJobRepository.findAll();
        var discoveryJobsGroupedByDriverId = allDiscoveryJobs.stream().collect(Collectors.groupingBy(DiscoveryJob::getDriverId));

        List<DiscoveryJob> discoveryJobs;
        if (onlyLatestJobs) {
            discoveryJobs = new ArrayList<>();
            for (var discoveryJobOfDriver : discoveryJobsGroupedByDriverId.entrySet()) {
                var discoveryJobsOfDriver = discoveryJobOfDriver.getValue();
                var latestDiscoveryJob = discoveryJobsOfDriver.stream().max(Comparator.comparing(DiscoveryJob::getStartDate)).orElse(null);
                discoveryJobsOfDriver.clear();
                if (latestDiscoveryJob != null) {
                    discoveryJobs.add(latestDiscoveryJob);
                }
            }
        }
        else {
            discoveryJobs = allDiscoveryJobs;
        }


        if (removeDuplicate) {
            var discoveredResourceDTOs = new HashMap<UUID, DiscoveredResourceDTO>();

            for (var discoveryJob : discoveryJobs) {
                for (var discoveredResource : discoveryJob.getDiscoveryResult()) {
                    var discoveredResourceDTO = DiscoveredResourceMapper.INSTANCE.toDto(discoveredResource);
                    this.completeDiscoveredResourceDTO(jwtAuthenticationToken, discoveredResourceDTO, discoveryJob);
                    if ((includedOnboarded || !discoveredResourceDTO.isOnboarded())
                            && (includeIgnored || !discoveredResourceDTO.isIgnored())) {
                        discoveredResourceDTOs.put(discoveredResourceDTO.getResourceId(), discoveredResourceDTO);
                    }
                }
            }

            return discoveredResourceDTOs.values();
        }
        else {
            var discoveredResourceDTOs = new ArrayList<DiscoveredResourceDTO>();

            for (var discoveryJob : discoveryJobs) {
                var discoveredResourceDTOsOfJob = DiscoveredResourceMapper.INSTANCE.toDtoList(discoveryJob.getDiscoveryResult());
                for (var discoveredResourceDTO : discoveredResourceDTOsOfJob) {
                    this.completeDiscoveredResourceDTO(jwtAuthenticationToken, discoveredResourceDTO, discoveryJob);
                    if ((includedOnboarded || !discoveredResourceDTO.isOnboarded())
                            && (includeIgnored || !discoveredResourceDTO.isIgnored())) {
                        discoveredResourceDTOs.add(discoveredResourceDTO);
                    }
                }
            }

            return discoveredResourceDTOs;

        }

    }

    private void completeDiscoveredResourceDTO(JwtAuthenticationToken jwtAuthenticationToken,
                                                                DiscoveredResourceDTO discoveredResourceDTO,
                                                                DiscoveryJob discoveryJob) {
        discoveredResourceDTO.setDiscoveryJobId(discoveryJob.getId());
        discoveredResourceDTO.setResultId(discoveryJob.getId() + ":" + discoveredResourceDTO.getResourceId());

        try {
            var existingResource = resourcesManager.getResourceByIdOrThrow(jwtAuthenticationToken, discoveredResourceDTO.getResourceId());
            if (existingResource != null) {
                discoveredResourceDTO.setOnboarded(true);
            }
        } catch (ResourceNotFoundException ignored) {
        }
        catch (Exception e) {
            LOG.error("Failed to get resource with id {} to check if it is onboarded", discoveredResourceDTO.getResourceId(), e);
        }
    }

    @Override
    public void onDiscoveryCompleted(DiscoveryJob completedDiscoveryJob) {
        var jwtAuthenticationToken = jobIdToTokenMap.get(completedDiscoveryJob.getId());
        jobIdToTokenMap.remove(completedDiscoveryJob.getId());
        var driverInfo = jobIdToDriverId.get(completedDiscoveryJob.getId());
        jobIdToDriverId.remove(completedDiscoveryJob.getId());

        this.discoveryEventMessageSender.sendMessage(completedDiscoveryJob, DiscoveryJobEventType.CHANGED);
    }

    public void onboard(JwtAuthenticationToken jwtAuthenticationToken, String resultId)
    {
        var discoveryJobId = resultId.split(":")[0];
        var resourceIdString = resultId.split(":")[1];
        var resourceId = UUID.fromString(resourceIdString);

        var discoveryJobOptional = this.discoveryJobRepository.findById(UUID.fromString(discoveryJobId));
        if (discoveryJobOptional.isPresent()) {
            var discoveryJob = discoveryJobOptional.get();
            var discoveredResourceOptional = discoveryJob.getDiscoveryResult().stream().filter(discoveredResource -> discoveredResource.getResourceId().equals(resourceId)).findFirst();
            if (discoveredResourceOptional.isPresent()) {
                var discoveredResource = discoveredResourceOptional.get();

                var digitalNameplateV3 = new DigitalNameplateV3.Builder(
                        discoveredResource.getId(),
                        discoveredResource.getManufacturerName(),
                        discoveredResource.getProductName(),
                        "N/A"
                );
                digitalNameplateV3.firmwareVersion(discoveredResource.getFirmwareVersion());

                resourcesManager.addResource(jwtAuthenticationToken,
                        discoveredResource.getResourceId(),
                        discoveredResource.getId(),
                        discoveredResource.getIpAddress(),
                        discoveredResource.getIpAddress(),
                        discoveredResource.getFirmwareVersion(),
                        discoveryJob.getDriverId(),
                        digitalNameplateV3.build());

                resourcesManager.setConnectionParametersOfResource(resourceId, discoveredResource.getConnectionParameters());
            }
        }
    }

    public void ignore(String resultId, boolean ignored) {
        String discoveryJobId = resultId.split(":")[0];
        String resourceId = resultId.split(":")[1];

        var discoveryJobOptional = this.discoveryJobRepository.findById(UUID.fromString(discoveryJobId));
        if (discoveryJobOptional.isPresent()) {
            var discoveryJob = discoveryJobOptional.get();
            var discoveredResourceOptional = discoveryJob.getDiscoveryResult().stream().filter(discoveredResource -> discoveredResource.getResourceId().equals(UUID.fromString(resourceId))).findFirst();
            if (discoveredResourceOptional.isPresent()) {
                var discoveredResource = discoveredResourceOptional.get();
                discoveredResource.setIgnored(ignored);
                this.discoveryJobRepository.save(discoveryJob);
            }
        }
    }
}
