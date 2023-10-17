import axios from 'axios'
import logRequestError from '@/api/restApiHelper.js'

class serviceManagementVariablesRestApi {
    API_URL = '/service-management/variables'

    async getSystemVariables () {
        return axios
            .get(`${this.API_URL}/system`)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getDeploymentVariables () {
        return axios
            .get(`${this.API_URL}/deployment`)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

}

export default new serviceManagementVariablesRestApi()
