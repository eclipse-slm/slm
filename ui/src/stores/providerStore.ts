import ResourceProvidersRestApi from "@/api/resource-management/resourceProvidersRestApi";
import {defineStore} from "pinia";

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
      return await ResourceProvidersRestApi.getVirtualResourceProviders()
          .then(response => {
            if(response){
              this.virtualResourceProviders_ = response;
            }
          })
    },
    async getServiceHosters () {
      return await ResourceProvidersRestApi.getServiceHosters()
          .then(response => {
            if(response){
              this.serviceHosters_ = response;
            }
          })
    }
  },

});
