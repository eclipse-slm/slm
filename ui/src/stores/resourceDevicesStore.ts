import ApiState from '@/api/apiState'
import {defineStore} from "pinia";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import jp from "jsonpath";
import { Base64 } from "js-base64";

interface ResourceDevicesStoreState{
    apiState: number,

    resources: any[],
    resourceTypes: any[],
    resourceAasDescriptors: any[],
    resourceAASValues: {}
    resourceConnectionTypes: any[],
    locations: any[],
    profiler: any[],

    firmwareUpdateInformationOfResources: {}
    firmwareUpdateJobsOfResources: {}
}

export const useResourceDevicesStore = defineStore('resourceDevicesStore', {
    persist: true,
    state:():ResourceDevicesStoreState => ({
        apiState: ApiState.INIT,

        resources: [],
        resourceTypes: [],
        resourceAasDescriptors: [],
        resourceAASValues: [],
        resourceConnectionTypes: [],
        locations: [],
        profiler: [],

        firmwareUpdateInformationOfResources: {},
        firmwareUpdateJobsOfResources: {}
    }),
    getters: {
        resourceById: (state) => (resourceId) => {
            return state.resources.find(resource => resource.id === resourceId)
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

        firmwareUpdateInformationOfResource: (state) => (resourceId) => {
            if (state.firmwareUpdateInformationOfResources[resourceId]) {
                return state.firmwareUpdateInformationOfResources[resourceId]
            } else {
                return {}
            }
        },

        firmwareUpdateJobsOfResource: (state) => (resourceId) => {
            if (state.firmwareUpdateJobsOfResources[resourceId]) {
                return state.firmwareUpdateJobsOfResources[resourceId]
            } else {
                return []
            }
        },
    },

    actions: {
        addOrUpdateResource(resource) {
            if (this.resources === undefined) {
                this.resources = [];
            }
            const index = this.resources.findIndex(r => r.id === resource.id);
            if (index !== -1) {
                this.resources = this.resources.filter(r => r.id !== resource.id);
            }
            this.resources.push(resource);
        },
        deleteResource(resourceId: string) {
            this.resources = this.resources.filter(resource => resource.id !== resourceId)
        },

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

        async getResourceById (resourceId: string) {
            return await ResourceManagementClient.resourcesApi.getResource(resourceId)
                .then(
                    response => {
                        if (response.data){
                            const resource = response.data
                            this.addOrUpdateResource(resource)
                            return this.getFirmwareUpdateInformationOfResource(resource.id);
                        }

                    })
                .catch(e => {
                    console.log(e)
                    this.setResources([]);
                })
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

        getResourceSubmodelByIdShort (resourceId, submodelIdShort) {
            try {
                var submodel =this.resourceAASValues[resourceId][submodelIdShort]
                return submodel
            } catch (e) {
                return "N/A"
            }
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

        async getFirmwareUpdateInformationOfResource(resourceId: string) {
            return await ResourceManagementClient.resourcesUpdatesApi.getUpdateInformationOfResource(resourceId).then(
                    response => {
                        this.firmwareUpdateInformationOfResources[resourceId] = response.data;
                    }
                ).catch(logRequestError)
        },

        async getFirmwareUpdateJobsOfResource(resourceId: string) {
            return await ResourceManagementClient.resourcesUpdatesApi.getFirmwareUpdateJobsOfResource(resourceId).then(
                response => {
                    this.firmwareUpdateJobsOfResources[resourceId] = response.data;
                    return response.data;
                }
            ).catch(logRequestError)
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

        async getRemoteConnectionTypes() {
            return await ResourceManagementClient.resourcesApi.getRemoteConnectionTypes()
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

        async getResourceTypes() {
            return await ResourceManagementClient.resourceTypesApi.getResourceTypes()
                .then(
                    response => {
                        if (response.data){
                            this.resourceTypes = response.data;
                        }
                    }
                )
                .catch(e => {
                    console.debug(e)
                    this.profiler = [];
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
                this.getRemoteConnectionTypes(),
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

