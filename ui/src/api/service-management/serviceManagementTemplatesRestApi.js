import axios from 'axios'
import logRequestError from '@/api/restApiHelper.js'

class serviceManagementTemplatesRestApi {
    API_URL = '/service-management/templates'

    async getTemplateVariables () {
        return axios
            .get(`${this.API_URL}/variables`)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

}

export default new serviceManagementTemplatesRestApi()
