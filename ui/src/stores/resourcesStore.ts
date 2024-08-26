import ApiState from '@/api/apiState'
import {app} from "@/main";
import {defineStore} from "pinia";
import {useProviderStore} from "@/stores/providerStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";

interface ResourcesStoreState{
    apiStateResources_: number,

    resources_: any[],
    aas: any[],
    locations_: any[],
    profiler_: any[],
    resourceConnectionTypes_: any[],
    clusters_: any[],

    allDevices_: {},

    selectedProject_: string,
    selectedResource_: any,
    selectedResourceType_: any,
    selectedResourceForDelete_: any,
    selectedClusterForDelete_: any,
    selectedClusterForScale_: any,
    availableResourceTypes_: any[],
    availableDeploymentCapabilities_: any[],
    availableBaseConfigurationCapabilities_: any[],
    availableClusterTypes_: any[],
    availableCapabilities_: any[],

    search_: string
}

export const useResourcesStore = defineStore('resourcesStore', {

    persist: true,
    state:():ResourcesStoreState => ({
        apiStateResources_: ApiState.INIT,

        resources_: [],
        aas: [],
        locations_: [],
        profiler_: [],
        resourceConnectionTypes_: [],
        clusters_: [],

        allDevices_: {},

        selectedProject_: 'fabos',
        selectedResource_: null,
        selectedResourceType_: null,
        selectedResourceForDelete_: null,
        selectedClusterForDelete_: null,
        selectedClusterForScale_: null,
        availableResourceTypes_: [],
        availableDeploymentCapabilities_: [],
        availableBaseConfigurationCapabilities_: [],
        availableClusterTypes_: [],
        availableCapabilities_: [],

        search_: ''
    }),
    getters: {
        apiStateResources(state){
            return state.apiStateResources_
        },

        resources: (state) => {
            if (state.resources_ === undefined) {
                return []
            } else {
                return state.resources_.filter(prop => {
                    return true
                })
            }
        },

        resourceAAS: (state) => {
            if (state.aas === undefined) {
                return []
            }   else {
                return state.aas
            }
        },

        profiler: (state) => {
            if (state.profiler_ === undefined) {
                return []
            } else {
                return state.profiler_
            }
        },

        locations: (state) => {
            return state.locations_
        },

        resourceConnectionTypes: (state) => {
            if (state.resourceConnectionTypes_ === undefined) {
                return []
            } else {
                return state.resourceConnectionTypes_
            }
        },

        resourceById: (state) => (id) => {
            return state.resources_.find(resource => resource.id === id)
        },

        nonClusterResources: (state) => {
            if (state.resources_ === undefined) {
                return []
            } else {
                return state.resources_.filter(prop => {
                    return !prop.clusterMember
                })
            }
        },

        clusters: (state) => {
            return state.clusters_
        },

        clusterById: (state) => (id) => {
            return state.clusters_.find(cluster => cluster.id === id)
        },

        selectedResource: (state) => {
            return state.selectedResource_
        },

        selectedResourceType: (state) => {
            return state.selectedResourceType_
        },

        selectedResourceForDelete: (state) => {
            return state.selectedResourceForDelete_
        },

        selectedClusterForDelete: (state) => {
            return state.selectedClusterForDelete_
        },

        selectedClusterForScale: (state) => {
            return state.selectedClusterForScale_
        },

        availableResourceTypes: (state) => {
            return state.availableResourceTypes_
        },

        availableClusterTypes: (state) => {
            return state.availableClusterTypes_
        },

        availableCapabilities: (state) => {

            return state.availableCapabilities_
        },
        availableSingleHostCapabilities: (state) => {
            return state.availableCapabilities_.filter(cap => {
                return cap.cluster === false
            })
        },
        availableSingleHostCapabilitiesNoDefault: (state) => {
            return state.availableCapabilities_.filter(cap => {
                return cap.clusterMemberTypes === undefined
            })
        },
        availableBaseConfigurationCapabilities: (state) => {
            return state.availableCapabilities_.filter(cap => {
                return cap.capabilityClass === "BaseConfigurationCapability"
            })
        },

        virtualMachines: (state) => {
            return state.resources_.filter(function (resource) {
                return resource.resourceType === 'VirtualMachine'
            })
        },

        dockerHosts: (state) => {
            return state.resources_.filter(function (resource) {
                return resource.resourceType === 'DockerHost'
            })
        },

        search: (state) => {
            return state.search_
        },
    },

    actions: {

        setResources(resources){
            if (this.resources_ !== undefined) {
                const markedForDeleteResources = this.resources_.filter((res) => res.markedForDelete === true)

                // Assure markedForDelete Property is not overwritten by polling
                for (const markedForDeleteResource of markedForDeleteResources) {
                    const resourceToUpdate = resources.find((res) => res.id === markedForDeleteResource.id)
                    if (resourceToUpdate !== undefined) {
                        resourceToUpdate.markedForDelete = true
                    }
                }
                this.resources_ = resources
            }
        },

        setClusters(clusters: any[] | undefined = undefined){
            if (clusters !== undefined) {
                this.clusters_ = clusters
            } else {
                this.clusters_ = []
            }
        },

        setAvailableResourceTypes(resources: any[]){
            const availableResourceTypes: any[] = []

            resources.forEach(item => {
                if (!(item.resourceType in availableResourceTypes)) {
                    availableResourceTypes.push(item.resourceType)
                }
            })

            this.availableResourceTypes_ = availableResourceTypes
        },

        setAvailableClusterTypes(types: any[] | undefined = undefined){
            if (types === undefined) {
                this.availableClusterTypes_ = []
            } else {
                this.availableClusterTypes_ = types
            }
        },

        setAvailableDeploymentCapabilities(capabilities: any[] | undefined = undefined){
            if (capabilities === undefined) {
                this.availableCapabilities_ = []
            } else {
                // Sort alphabetically
                this.availableCapabilities_ = capabilities.sort((a, b) => {
                    return a.name.localeCompare(b.name)
                })
            }
        },

        setResourceMarkedForDelete(resource){
            const filteredRes = this.resources_.find(obj => obj.id === resource.id)
            filteredRes.markedForDelete = true
        },

        async getResourcesFromBackend () {
            if (this.apiStateResources_ === ApiState.INIT) {
                this.apiStateResources_ = ApiState.LOADING;
            } else {
                this.apiStateResources_ = ApiState.UPDATING;
            }

            return await ResourceManagementClient.resourcesApi.getResources()
                .then(
                    response => {
                        if (response.data){
                            /*response.data.forEach(resource => {
                                // MetricsRestApi.getMetricsOfResource(resource.id).then(metrics => {
                                //     resource.metrics = metrics
                                // })
                            })*/
                            this.setResources(response.data)
                            this.setAvailableResourceTypes(response.data);
                            this.apiStateResources_ = ApiState.LOADED;
                        }

                    })
                .catch(e => {
                    console.debug(e)
                    this.setResources([]);
                    this.setAvailableResourceTypes([]);
                    this.apiStateResources_ = ApiState.ERROR;
                })
        },

        async getResourceAASFromBackend () {
            if (this.apiStateResources_ === ApiState.INIT) {
                this.apiStateResources_ = ApiState.LOADING;
            } else {
                this.apiStateResources_ = ApiState.UPDATING;
            }


            return await ResourceManagementClient.aasApi.getResourceAASDescriptors().then(
                response => {
                    this.aas = response.data;
                    this.apiStateResources_ = ApiState.LOADED;
                }
            ).catch(logRequestError)
        },

        async getLocations() {
            return await  ResourceManagementClient.locationApi.getLocations()
                .then(
                    result => {
                        if (result.data)
                            this.locations_ = result.data;
                    }
                )
                .catch(e => {
                    console.debug(e)
                    this.locations_ = [];
                })
        },

        async getProfiler() {
            return await  ResourceManagementClient.profilerApi.getProfiler()
                .then(
                    response => {

                        if (response.data){
                            this.profiler_ = response.data;
                        }
                    }
                )
                .catch(e => {
                    console.debug(e)
                    this.profiler_ = [];
                })
        },

        async getResourceConnectionTypes() {
            return await ResourceManagementClient.resourcesApi.getResourceConnectionTypes()
                .then(
                    response => {
                        if(response.data){
                            this.resourceConnectionTypes_ = response.data;
                        }
                    }
                )
                .catch(e => {
                    console.debug(e)
                    this.resourceConnectionTypes_ = [];
                })
        },

        async getCluster () {

            try{
                const response = await ResourceManagementClient.clusterApi.getClusterResources();
                const clusters: any[] = []
                if (response.data && response.data.length > 0) {
                    for (const i in response.data) {
                        clusters.push({
                            id: response[i].id,
                            name: response[i].metaData['cluster_name'] ? response[i].metaData['cluster_name'] : response[i].name,
                            project: 'fabos',
                            clusterType: response[i].clusterType,
                            clusterMemberTypes: response[i].clusterMemberTypes,
                            nodeCount: response[i].nodes.length > 0? response[i].nodes.length: 'externally managed',
                            nodes: response[i].nodes,
                            memberMapping: response[i].memberMapping,
                            isManaged: response[i].managed,
                            metaData: response[i].metaData,
                            namespace: response[i].metaData['namespace'] ? response[i].metaData['namespace'] : '-',
                            username: response[i].metaData['cluster_user'] ? response[i].metaData['cluster_user'] : '-',
                            status: response[i].capabilityService.status
                        })
                    }

                    this.setClusters(clusters)
                }
                return clusters;
            }catch (e) {
                logRequestError(e)
                return [];
            }

        },

        async getClusterTypes () {
            return await ResourceManagementClient.clusterApi.getClusterTypes()
                .then(response => {
                    if(response.data && response.data.length > 0) {
                        this.setAvailableClusterTypes(response.data)
                    }
                }).catch(logRequestError)
        },

        async getDeploymentCapabilities () {
            return await ResourceManagementClient.capabilityApi.getCapabilities()
                .then(response => {
                    if(response){
                        this.setAvailableDeploymentCapabilities(response.data);
                    }
                })
        },

        updateResourcesStore () {
            // assure token gets refreshed
            app.config.globalProperties.$keycloak.keycloak.updateToken(1000000)
                .then(refreshed => {
                    this.getCluster().then();
                    this.getResourcesFromBackend().then();
                    this.getResourceAASFromBackend().then();
                    this.getResourceConnectionTypes().then();
                    useProviderStore().getVirtualResourceProviders().then();
                    const providerStore = useProviderStore();
                    providerStore.getServiceHosters().then();
                    this.getProfiler().then();
                })
        },

        async setSelectedResource (resource) {
            if (resource.passwordType === 'otp') {
                const otp = await this.getOtp(resource)
                resource.password = otp
            }

            this.selectedResource_ = resource;
        },

        async getOtp (resource) {
            const project = this.selectedProject_
            const ip = resource.ip
            const username = 'vfk'

            return '';
        },

        async updateOtp () {
            const otp = await this.getOtp(this.selectedResource_);
            this.selectedResource_.password = otp;
        },
    },
});

