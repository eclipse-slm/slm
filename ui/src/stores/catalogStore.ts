import ApiState from '@/api/apiState.js'
import CatalogRestApi from "@/api/catalog/catalogRestApi";
import {defineStore} from 'pinia'

interface CatalogStoreState{
    apiState: number,
    aasSubmodelTemplates: [],
}


export const useCatalogStore = defineStore('catalogStore', {
    persist: true,

    state: (): CatalogStoreState => ({
        apiState: ApiState.INIT,
        aasSubmodelTemplates: []
    }),

    getters:{
    },

    actions: {
        async getSubmodelTemplates () {
            await CatalogRestApi.getAASSubmodelTemplates()
                .then(
                    receivedAasSubmodelTemplates => {
                        this.aasSubmodelTemplates = receivedAasSubmodelTemplates;
                    })
        },

        async updateStore () {
            if (this.apiState === ApiState.INIT) {
                this.apiState = ApiState.LOADING;
            } else {
                this.apiState = ApiState.UPDATING;
            }

            return Promise.all([
                this.getSubmodelTemplates(),
            ]).then(() => {
                this.apiState = ApiState.LOADED;
                console.log("catalogStore updated")
            }).catch((e) => {
                this.apiState = ApiState.ERROR;
                console.log("Failed to update catalogStore: ", e)
            });
        },
    }

});
