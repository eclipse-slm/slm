import axios from 'axios'

class AASRestApi {
    API_URL = '/resource-management/aas'

    async getResourceAAS() {
        return axios.get(`${this.API_URL}/`)
          .then(response => {
              return response.data;
          });
    }

    async getSubmodelTemplateInstancesBySemanticId (submodelTemplateSemanticId) {
        return axios.get(`${this.API_URL}/submodels/templates/${submodelTemplateSemanticId}/instances`)
            .then(response => {
                return response.data;
            });
    }

    async getSubmoduleTemplateInstanceOfAas (submodelTemplateSemanticId, aasId) {
        return axios.get(`${this.API_URL}/submodels/templates/${submodelTemplateSemanticId}/instances`, {
            params: {
                filterByAasId: aasId
            }
        })
        .then(response => {
            return response.data;
        });
    }
}

export default new AASRestApi()
