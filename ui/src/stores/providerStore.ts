import {defineStore} from "pinia";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";

interface ProviderStoreState{
  virtualResourceProviders_: any[],
  serviceHosters_: any[]
}

export const useProviderStore = defineStore('providerStore', {
  persist: true,
  state:():ProviderStoreState => ({
    virtualResourceProviders_: [],
    serviceHosters_: []
  }),
  getters: {
    virtualResourceProviders: (state) => {
      return state.virtualResourceProviders_
    },
    serviceHosters: (state) => {
      return state.serviceHosters_
    }
  },

  actions: {
    async getVirtualResourceProviders () {
      return await ResourceManagementClient.capabilityProvidersApi.getVirtualResourceProviders()
          .then(response => {
            if(response.data){
              this.virtualResourceProviders_ = response.data;
            }
          }).catch(logRequestError)
    },
    async getServiceHosters () {
      return await ResourceManagementClient.capabilityProvidersApi.getServiceHosters()
          .then(response => {
            if(response.data){
              this.serviceHosters_ = response.data;
            }
          }).catch(logRequestError)
    }
  },

});
