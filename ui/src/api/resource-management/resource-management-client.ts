import {
    CapabilitiesApi,
    CapabilityProvidersApi,
    ClustersApi,
    DiscoveryApi,
    JobsApi,
    LocationApi,
    MetricsApi,
    ProfilerApi,
    ResourcesAASApi,
    ResourcesApi,
    ResourceTypesApi, SubmodelTemplatesApi,
    UpdatesApi,
} from "@/api/resource-management/client";


class ResourceManagementClient{

    apiUrl = "/resource-management";

    resourcesApi = new ResourcesApi(undefined, this.apiUrl);
    aasApi = new ResourcesAASApi(undefined, this.apiUrl);
    submodelTemplatesRestControllerApi = new SubmodelTemplatesApi(undefined, this.apiUrl);
    clusterApi = new ClustersApi(undefined, this.apiUrl)
    jobApi = new JobsApi(undefined, this.apiUrl);
    locationApi = new LocationApi(undefined, this.apiUrl);
    metricsApi = new MetricsApi(undefined, this.apiUrl);
    profilerApi = new ProfilerApi(undefined, this.apiUrl);
    capabilityProvidersApi = new CapabilityProvidersApi(undefined, this.apiUrl);
    capabilityApi = new CapabilitiesApi(undefined, this.apiUrl)
    submodelsApi = new ResourcesAASApi(undefined, this.apiUrl);
    discoveryApi = new DiscoveryApi(undefined, this.apiUrl);
    resourcesSubmodelRepositoryApi = new ResourcesAASApi(undefined, this.apiUrl);
    resourcesUpdatesApi = new UpdatesApi(undefined, this.apiUrl);
    resourceTypesApi = new ResourceTypesApi(undefined, this.apiUrl);
}

export default new ResourceManagementClient()