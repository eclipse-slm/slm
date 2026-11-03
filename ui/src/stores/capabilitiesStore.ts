import ApiState from '@/api/apiState'
import {defineStore} from "pinia";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import {CapabilityServiceDTO, GetCapabilities200ResponseInner, ResourceDTO} from "@/api/resource-management/client";

export class CapabilityInUseError extends Error {
    constructor(public installCount: number) {
        super(`Capability is installed ${installCount} time(s)`)
        this.name = 'CapabilityInUseError'
    }
}

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

        installCountOfCapability: (state) => (capabilityId: string) => {
            return state.capabilityServices.filter(capabilityService => capabilityService.capabilityId === capabilityId).length
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

        async createCapability(body: GetCapabilities200ResponseInner) {
            await ResourceManagementClient.capabilityApi.createCapability(body)
            await this.getCapabilities()
        },

        async deleteCapability(capabilityId: string) {
            // Fetch directly (not via getCapabilityServices, which swallows errors) so a failed fetch aborts the delete
            const response = await ResourceManagementClient.capabilityApi.getAllCapabilityServices()
            if (!Array.isArray(response.data)) {
                throw new Error("Could not verify where the capability is installed")
            }
            this.capabilityServices = response.data

            const installCount = this.installCountOfCapability(capabilityId)
            if (installCount > 0) {
                throw new CapabilityInUseError(installCount)
            }

            await ResourceManagementClient.capabilityApi.deleteCapability(capabilityId)
            await this.getCapabilities()
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

