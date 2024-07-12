import ApiState from '@/api/apiState.js'
import CatalogRestApi from "@/api/catalog/catalogRestApi";
import { defineStore } from 'pinia'

interface CatalogStoreState{
    apiStateCatalog_: number,
    aasSubmodelTemplates_: [],
}


export const useCatalogStore = defineStore('catalogStore', {
    persist: true,
    state: (): CatalogStoreState => ({
        apiStateCatalog_: ApiState.INIT,
        aasSubmodelTemplates_: []
    }),

    getters:{
        apiStateCatalog: (state) => {
            return state.apiStateCatalog_
        },

        aasSubmodelTemplates: (state) => {
            return state.aasSubmodelTemplates_
        },
    },

    actions: {
        async updateCatalogStore () {
            await CatalogRestApi.getAASSubmodelTemplates()
                .then(
                    aasSubmodelTemplates => {
                        this.aasSubmodelTemplates_ = aasSubmodelTemplates;
                    })
        },
    }


});
