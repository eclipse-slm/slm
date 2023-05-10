import ApiState from '@/api/apiState.js'
import CatalogRestApi from "@/api/catalog/catalogRestApi";

export default {
    state: {
        apiStateCatalog: ApiState.INIT,

        aasSubmodelTemplates: [],
    },

    getters: {
        apiStateCatalog: (state) => {
            return state.apiStateCatalog
        },

        aasSubmodelTemplates: (state) => {
            return state.aasSubmodelTemplates
        },
    },

    mutations: {
        SET_API_STATE_CATALOG (state, apiState) {
            state.apiStateCatalog = apiState
        },

        SET_AAS_SUBMODEL_TEMPLATES (state, aasSubmodelTemplates) {
            state.aasSubmodelTemplates = aasSubmodelTemplates
        }
    },

    actions: {
        async updateCatalogStore (context) {
            await CatalogRestApi.getAASSubmodelTemplates()
                .then(
                    aasSubmodelTemplates => {
                        context.commit('SET_AAS_SUBMODEL_TEMPLATES', aasSubmodelTemplates)
                    })
        },
    },
}
