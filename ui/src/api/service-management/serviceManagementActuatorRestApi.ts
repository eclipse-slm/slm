import axios from 'axios'
import logRequestError from '@/api/restApiHelper.js'

class ServiceManagementActuatorRestApi {
    API_URL = '/service-management/actuator'

    async getInfo () {
        return axios
            .get(this.API_URL + '/info')
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getHealth () {
        return axios
            .get(this.API_URL + '/health')
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }
}

export default new ServiceManagementActuatorRestApi()
