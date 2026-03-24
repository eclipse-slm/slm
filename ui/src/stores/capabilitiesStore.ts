import ApiState from '@/api/apiState'
import {defineStore} from "pinia";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import {CapabilityServiceDTO, ResourceDTO} from "@/api/resource-management/client";

interface CapabilitiesStoreState{
    apiState: number,

    capabilities: any[],
    capabilityServices: CapabilityServiceDTO[],
}

export const useCapabilitiesStore = defineStore('capabilitiesStore', {
    persist: true,
    state:():CapabilitiesStoreState => ({
        apiState: ApiState.INIT,

        capabilities: [],
        capabilityServices: [],
    }),
    getters: {
        availableSingleHostCapabilities: (state) => {
            const singleHostCaps = state.capabilities.filter(cap => {
                return !cap.type.includes("SCALE")
            })
            return singleHostCaps
        },
        availableBaseConfigurationCapabilities: (state) => {
            return state.capabilities.filter(cap => {
                return cap.capabilityClass === "BaseConfigurationCapability"
            })
        },

        capabilityById: (state) => (capabilityId) => {
            return state.capabilities.find(capability => capability.id === capabilityId)
        },

        capabilityServiceById: (state) => (capabilityServiceId: string) => {
            return state.capabilityServices.find(capabilityService => capabilityService.id === capabilityServiceId)
        },

        capabilityOfCapabilityService: (state) => (capabilityServiceId) => {
            const capabilityService = state.capabilityServices.find(capabilityService => capabilityService.id === capabilityServiceId)

            const capability = state.capabilities.find(capability => capability.id === capabilityService.capabilityId)

            return capability;
        },

        isCapabilityInstalledOnResource: (state) => (resource: ResourceDTO, capabilityId) => {
            for (const capabilityServiceId of resource.capabilityServiceIds) {
                const capabilityService = state.capabilityServices.find(capabilityService => capabilityService.id === capabilityServiceId)

                if (capabilityService?.capabilityId === capabilityId) {
                    return true;
                }
            }

            return false;
        }
    },

    actions: {
        async getCapabilities () {
            return await ResourceManagementClient.capabilityApi.getCapabilities()
                .then(response => {
                    if(response){
                        const receivedCapabilities = response.data;
                        if (receivedCapabilities === undefined) {
                            this.capabilities = []
                        } else {
                            // Sort alphabetically
                            this.capabilities = receivedCapabilities.sort((a, b) => {
                                return a.name.localeCompare(b.name)
                            })
                        }
                    }
                })
        },

        async getCapabilityServices() {
            return await ResourceManagementClient.capabilityApi.getAllCapabilityServices()
                .then(
                    response => {
                        if (response.data){
                            this.capabilityServices = response.data;
                        }
                    }
                )
                .catch(e => {
                    console.debug(e)
                    this.capabilityServices = [];
                })
        },

        async updateStore () {
            if (this.apiState === ApiState.INIT) {
                this.apiState = ApiState.LOADING;
            } else {
                this.apiState = ApiState.UPDATING;
            }

            return Promise.all([
                this.getCapabilities(),
                this.getCapabilityServices(),
            ]).then(() => {
                this.apiState = ApiState.LOADED;
                console.log("capabilitiesStore updated")
            }).catch((e) => {
                this.apiState = ApiState.ERROR;
                console.log("Failed to update capabilitiesStore: ", e)
            });
        },
    },
});

