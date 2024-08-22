import ResourcesRestApi from '@/api/resource-management/resourcesRestApi'
import {defineStore} from "pinia";

interface OverviewStoreState{
  resources: any[],
}

export const useOverviewStore = defineStore('overviewStore', {
  persist: true,
  state:():OverviewStoreState => ({
    resources: []
  }),
  getters: {
    overviewResources: (state) => {
      if (state.resources === undefined) { return [] } else { return state.resources }
    },
  },
  actions: {
    async getResourcesOverview () {
      const resources = await ResourcesRestApi.getResources();
      if (resources){
        this.resources = resources;
      }
    },
  },
});
