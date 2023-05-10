import ResourceProvidersRestApi from "@/api/resource-management/resourceProvidersRestApi";

export default {

  state: {
    virtualResourceProviders: [],
    serviceHosters: []
  },

  getters: {
    virtualResourceProviders: (state) => {
      return state.virtualResourceProviders
    },
    serviceHosters: (state) => {
      return state.serviceHosters
    }
  },

  actions: {
    async getVirtualResourceProviders (context) {
      return await ResourceProvidersRestApi.getVirtualResourceProviders()
        .then(response => {
          context.commit('SET_VIRTUAL_RESOURCE_PROVIDERS', response)
        })
    },
    async getServiceHosters (context) {
      return await ResourceProvidersRestApi.getServiceHosters()
        .then(response => {
          context.commit('SET_SERVICE_HOSTERS', response)
        })
    }
  },

  mutations: {
    SET_VIRTUAL_RESOURCE_PROVIDERS (state, providers) {
      state.virtualResourceProviders = providers
    },
    SET_SERVICE_HOSTERS (state, hosters) {
      state.serviceHosters = hosters
    },
  }
}
