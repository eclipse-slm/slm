import axios from 'axios'
import logRequestError from '@/api/restApiHelper.js'

class CatalogRestApi {
    API_URL = '/slm-catalog/catalog'

    async getAASSubmodelTemplates () {
        return axios
            .get(this.API_URL + '/aas/submodels/templates')
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }
}

export default new CatalogRestApi()
