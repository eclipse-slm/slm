import ApiState from '@/api/apiState'
import {defineStore} from "pinia";
import {useProviderStore} from "@/stores/providerStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import jp from "jsonpath";
import { Base64 } from "js-base64";

interface ResourceDevicesStoreState{
    apiState: number,

    resources: any[],
    resourceAasDescriptors: any[],
    resourceAASValues: {}
    resourceConnectionTypes: any[],
    locations: any[],
    profiler: any[],

    availableCapabilities: any[],

    firmwareUpdateInformationOfResources: {}
}

export const useResourceDevicesStore = defineStore('resourceDevicesStore', {
    persist: true,
    state:():ResourceDevicesStoreState => ({
        apiState: ApiState.INIT,

        resources: [],
        resourceAasDescriptors: [],
        resourceAASValues: [],
        resourceConnectionTypes: [],
        locations: [],
        profiler: [],

        availableCapabilities: [],

        firmwareUpdateInformationOfResources: {},
    }),
    getters: {
        resourceById: (state) => (id) => {
            return state.resources.find(resource => resource.id === id)
        },

        availableSingleHostCapabilities: (state) => {
            return state.availableCapabilities.filter(cap => {
                return cap.cluster === false
            })
        },
        availableSingleHostCapabilitiesNoDefault: (state) => {
            return state.availableCapabilities.filter(cap => {
                return cap.clusterMemberTypes === undefined
            })
        },
        availableBaseConfigurationCapabilities: (state) => {
            return state.availableCapabilities.filter(cap => {
                return cap.capabilityClass === "BaseConfigurationCapability"
            })
        },
        nonClusterResources: (state) => {
            if (state.resources === undefined) {
                return []
            } else {
                return state.resources.filter(prop => {
                    return !prop.clusterMember
                })
            }
        },
    },

    actions: {
        setResources(newResources){
            try {
                if (this.resources !== undefined) {
                    const markedForDeleteResources = this.resources.filter((res) => res.markedForDelete === true)

                    // Assure markedForDelete Property is not overwritten by polling
                    for (const markedForDeleteResource of markedForDeleteResources) {
                        const resourceToUpdate = newResources.find((res) => res.id === markedForDeleteResource.id)
                        if (resourceToUpdate !== undefined) {
                            resourceToUpdate.markedForDelete = true
                        }
                    }
                }
            } catch (e) {
                console.log(e)
            }
            this.resources = newResources
        },

        setResourceMarkedForDelete(resource){
            const filteredRes = this.resources.find(obj => obj.id === resource.id)
            filteredRes.markedForDelete = true
        },

        async getResourcesFromBackend () {
            return await ResourceManagementClient.resourcesApi.getResources()
                .then(
                    response => {
                        if (response.data){
                            const resources = response.data
                            this.setResources(resources)
                            return this.getFirmwareUpdateInformationOfAllResources();
                        }

                    })
                .catch(e => {
                    console.log(e)
                    this.setResources([]);
                })
        },

        async getResourceAasValues () {
            return await Promise.all(this.resources.map(async (resource) => {
                await ResourceManagementClient.resourcesSubmodelRepositoryApi.getAllSubmodelsValueOnly(Base64.encode("Resource_" + resource.id))
                    .then(
                        response => {
                            this.resourceAASValues[resource.id] = response.data;
                        }
                    ).catch(logRequestError)
            }));
        },

        getSubmodelElementValueOfResourceSubmodel (resourceId, submodelIdShort, submodelElementJsonPath) {
            try {
                var value = jp.value(this.resourceAASValues[resourceId][submodelIdShort]["valuesOnlyMap"], submodelElementJsonPath);
                if (value == undefined) {
                    value = "N/A";
                }
                return value
            } catch (e) {
                return "N/A"
            }
        },

        async getFirmwareUpdateInformationOfAllResources() {
            return await Promise.all(this.resources.map(async (resource) => {
                await ResourceManagementClient.resourcesUpdatesApi.getUpdateInformationOfResource(resource.id).then(
                    response => {
                        this.firmwareUpdateInformationOfResources[resource.id] = response.data;
                    }
                ).catch(logRequestError)
            }));
        },

        getFirmwareUpdateInformationOfResource (resourceId) {
            return this.firmwareUpdateInformationOfResources[resourceId]
        },

        async getResourceAasDescriptors () {
            return await ResourceManagementClient.aasApi.getResourceAASDescriptors().then(
                response => {
                    this.resourceAasDescriptors = response.data;
                }
            ).catch(logRequestError)
        },

        async getLocations() {
            return await  ResourceManagementClient.locationApi.getLocations()
                .then(
                    result => {
                        if (result.data)
                            this.locations = result.data;
                    }
                )
                .catch(e => {
                    console.debug(e)
                    this.locations = [];
                })
        },

        async getProfiler() {
            return await  ResourceManagementClient.profilerApi.getProfiler()
                .then(
                    response => {
                        if (response.data){
                            this.profiler = response.data;
                        }
                    }
                )
                .catch(e => {
                    console.debug(e)
                    this.profiler = [];
                })
        },

        async getResourceConnectionTypes() {
            return await ResourceManagementClient.resourcesApi.getResourceConnectionTypes()
                .then(
                    response => {
                        if(response.data){
                            this.resourceConnectionTypes = response.data;
                        }
                    }
                )
                .catch(e => {
                    console.error(e)
                    this.resourceConnectionTypes = [];
                })
        },

        async getDeploymentCapabilities () {
            return await ResourceManagementClient.capabilityApi.getCapabilities()
                .then(response => {
                    if(response){
                        const receivedCapabilities = response.data;
                        if (receivedCapabilities === undefined) {
                            this.availableCapabilities = []
                        } else {
                            // Sort alphabetically
                            this.availableCapabilities = receivedCapabilities.sort((a, b) => {
                                return a.name.localeCompare(b.name)
                            })
                        }
                    }
                })
        },

        async updateStore () {
            if (this.apiState === ApiState.INIT) {
                this.apiState = ApiState.LOADING;
            } else {
                this.apiState = ApiState.UPDATING;
            }

            return Promise.all([
                this.getResourcesFromBackend(),
                this.getResourceAasDescriptors(),
                this.getResourceAasValues(),
                this.getLocations(),
                this.getProfiler(),
                this.getResourceConnectionTypes(),
                this.getDeploymentCapabilities(),
            ]).then(() => {
                this.apiState = ApiState.LOADED;
                console.log("resourceDevicesStore updated")
            }).catch((e) => {
                this.apiState = ApiState.ERROR;
                console.log("Failed to update resourceDevicesStore: ", e)
            });
        },
    },
});

