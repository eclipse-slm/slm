import ResourcesRestApi from '@/api/resource-management/resourcesRestApi'

export default {
  state: {
    resources: [],
  },

  getters: {
    overviewResources: (state) => {
      if (state.resources === undefined) { return [] } else { return state.resources }
    },
  },

  actions: {
    async getResourcesOverview (context) {
      const resources = await ResourcesRestApi.getResources()
      context.commit('SET_OVERVIEW_RESOURCES', resources)
    },
  },

  mutations: {
    SET_OVERVIEW_RESOURCES (state, resources) {
      state.resources = resources
    },
  },
}
