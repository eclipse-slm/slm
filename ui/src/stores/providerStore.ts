import {defineStore} from "pinia";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import ApiState from "@/api/apiState";

interface ProviderStoreState{
  apiState: number,
  virtualResourceProviders: any[],
  serviceHosters: any[]
}

export const useProviderStore = defineStore('providerStore', {
  persist: true,

  state:():ProviderStoreState => ({
    apiState: ApiState.INIT,
    virtualResourceProviders: [],
    serviceHosters: []
  }),

  getters: {
  },

  actions: {
    async getVirtualResourceProviders () {
      return await ResourceManagementClient.capabilityProvidersApi.getVirtualResourceProviders()
          .then(response => {
            if(response.data){
              this.virtualResourceProviders = response.data;
            }
          }).catch(logRequestError)
    },
    async getServiceHosters () {
      return await ResourceManagementClient.capabilityProvidersApi.getServiceHosters()
          .then(response => {
            if(response.data){
              this.serviceHosters = response.data;
            }
          }).catch(logRequestError)
    },
    async updateStore () {
      if (this.apiState === ApiState.INIT) {
        this.apiState = ApiState.LOADING;
      } else {
        this.apiState = ApiState.UPDATING;
      }

      return Promise.all([
        this.getVirtualResourceProviders(),
        this.getServiceHosters()
      ]).then(() => {
        this.apiState = ApiState.LOADED;
        console.log("providerStore updated")
      }).catch((e) => {
        this.apiState = ApiState.ERROR;
        console.log("Failed to update providerStore: ", e)
      });
    },
  },

});
