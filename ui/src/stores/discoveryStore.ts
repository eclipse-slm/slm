import ApiState from '@/api/apiState'
import {defineStore} from "pinia";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import {DiscoveryJob} from "@/api/resource-management/client";

interface DiscoveryStoreState{
    apiState: number,
    drivers: any[],
    discoveredResources: any[],
    discoveryJobs: DiscoveryJob[],
}

export const useDiscoveryStore = defineStore('discoveryStore', {
    persist: true,

    state:():DiscoveryStoreState => ({
        apiState: ApiState.INIT,
        drivers: [],
        discoveredResources: [],
        discoveryJobs: [],
    }),

    getters: {
        discoveredResourceByResultId: (state) => (resultId) => {
            return state.discoveredResources.find(discoveredResource => discoveredResource.resultId === resultId)
        },

        discoveryJobById: (state) => (discoveryJobId): DiscoveryJob | undefined => {
            return state.discoveryJobs.find(discoveryJob => discoveryJob.id === discoveryJobId)
        },
    },

    actions: {
        async updateDiscoveryStore () {
            console.log("Update discovery store")
            this.getDrivers().then();
            this.getDiscoveredResources().then();
        },

        async getDrivers () {

            return await ResourceManagementClient.discoveryApi.getRegisteredDrivers().then(
                response => {
                    this.drivers = response.data;
                }
            ).catch(logRequestError)
        },

        async getDiscoveredResources (removeDuplicate = false, onlyLatestJobs = false, includeIgnored = false) {
            return await ResourceManagementClient.discoveryApi.getDiscoveredResources(removeDuplicate, onlyLatestJobs, includeIgnored).then(
                response => {
                    this.discoveredResources = response.data;
                }
            ).catch(logRequestError)
        },

        async getDiscoveryJobs () {

            return await ResourceManagementClient.discoveryApi.getDiscoveryJobs().then(
                response => {
                    this.discoveryJobs = response.data;
                }
            ).catch(logRequestError)
        },

        async updateStore () {
            if (this.apiState === ApiState.INIT) {
                this.apiState = ApiState.LOADING;
            } else {
                this.apiState = ApiState.UPDATING;
            }

            return Promise.all([
                this.getDrivers(),
                this.getDiscoveredResources(),
                this.getDiscoveryJobs(),
            ]).then(() => {
                this.apiState = ApiState.LOADED;
                console.log("discoveryStore updated")
            }).catch((e) => {
                this.apiState = ApiState.ERROR;
                console.log("Failed to update discoveryStore: ", e)
            });
        },
    },
});

