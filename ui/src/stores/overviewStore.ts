import {defineStore} from "pinia";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";

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
      try{
        const resources = await ResourceManagementClient.resourcesApi.getResources();
        if (resources.data){
          this.resources = resources.data;
        }
      }catch (e) {
        logRequestError(e)
      }

    },
  },
});
