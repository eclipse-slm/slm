import {
    CapabilitiesRestControllerApi,
    CapabilityProvidersRestControllerApi,
    ClustersRestControllerApi,
    JobsRestControllerApi,
    LocationRestControllerApi,
    MetricsRestControllerApi,
    ProfilerRestControllerApi,
    ResourcesRestControllerApi,
    SubmodelsRestControllerApi,
    SubmodelTemplatesRestControllerApi
} from "@/api/resource-management/client";


class ResourceManagementClient{

    apiUrl = "/resource-management";

    resourcesApi = new ResourcesRestControllerApi(undefined, this.apiUrl);
    aasApi = new SubmodelTemplatesRestControllerApi(undefined, this.apiUrl);
    clusterApi = new ClustersRestControllerApi(undefined, this.apiUrl)
    jobApi = new JobsRestControllerApi(undefined, this.apiUrl);
    locationApi = new LocationRestControllerApi(undefined, this.apiUrl);
    metricsApi = new MetricsRestControllerApi(undefined, this.apiUrl);
    profilerApi = new ProfilerRestControllerApi(undefined, this.apiUrl);
    capabilityProvidersApi = new CapabilityProvidersRestControllerApi(undefined, this.apiUrl);
    capabilityApi = new CapabilitiesRestControllerApi(undefined, this.apiUrl)
    submodelsApi = new SubmodelsRestControllerApi(undefined, this.apiUrl);

}

export default new ResourceManagementClient()