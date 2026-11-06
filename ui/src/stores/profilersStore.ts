import ApiState from '@/api/apiState'
import {defineStore} from "pinia";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import {Profiler, ProfilerDTOApi} from "@/api/resource-management/client";

interface ProfilersStoreState {
    apiState: number,

    profilers: Profiler[],
}

export const useProfilersStore = defineStore('profilersStore', {
    persist: true,
    state: (): ProfilersStoreState => ({
        apiState: ApiState.INIT,

        profilers: [],
    }),

    actions: {
        async getProfilers() {
            return await ResourceManagementClient.profilerApi.getProfiler()
                .then(response => {
                    if (response) {
                        const receivedProfilers = response.data;
                        if (receivedProfilers === undefined) {
                            this.profilers = []
                        } else {
                            // Sort alphabetically
                            this.profilers = receivedProfilers.sort((a, b) => {
                                return a.name.localeCompare(b.name)
                            })
                        }
                    }
                })
        },

        async createProfiler(body: ProfilerDTOApi) {
            await ResourceManagementClient.profilerApi.createProfiler(body)
            await this.reloadAfterChange()
        },

        async updateProfiler(profilerId: string, body: ProfilerDTOApi) {
            await ResourceManagementClient.profilerApi.updateProfiler(profilerId, body)
            await this.reloadAfterChange()
        },

        async deleteProfiler(profilerId: string) {
            await ResourceManagementClient.profilerApi.deleteProfiler(profilerId)
            await this.reloadAfterChange()
            // A successful delete is authoritative even if the list reload failed or returned stale data.
            this.profilers = this.profilers.filter(profiler => profiler.id !== profilerId)
        },

        // The change already succeeded; a failed reload must not make it look failed (a retry would duplicate it)
        async reloadAfterChange() {
            try {
                await this.getProfilers()
            } catch (e) {
                console.error('Failed to reload profilers after a successful change', e)
            }
        },

        async updateStore() {
            if (this.apiState === ApiState.INIT) {
                this.apiState = ApiState.LOADING;
            } else {
                this.apiState = ApiState.UPDATING;
            }

            return this.getProfilers().then(() => {
                this.apiState = ApiState.LOADED;
                console.log("profilersStore updated")
            }).catch((e) => {
                this.apiState = ApiState.ERROR;
                console.log("Failed to update profilersStore: ", e)
            });
        },
    },
});
