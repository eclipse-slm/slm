import axios from 'axios'
import logRequestError from '@/api/restApiHelper.js'

class ServiceInstancesRestApi {
    API_URL = '/service-management/services/instances'

    async getServiceInstances () {
        return axios
            .get(this.API_URL + '/')
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async deleteServiceInstance (serviceId) {
        return axios
            .delete(`${this.API_URL}/${serviceId}`)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getAvailableVersionsForServiceInstance (serviceInstanceId) {
        return axios
            .get(`${this.API_URL}/${serviceInstanceId}/versions`)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async changeServiceInstanceVersion(serviceInstanceId, targetServiceOfferingVersionId) {
        return axios
            .post(`${this.API_URL}/${serviceInstanceId}/versions`, null,{
                params: {
                    targetServiceOfferingVersionId: targetServiceOfferingVersionId
                }
            })
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getServiceInstanceDetails(serviceInstanceId) {
        return axios
            .get(`${this.API_URL}/${serviceInstanceId}/details`)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async setGroupsForServiceInstance(serviceInstanceId, groupIds) {
        return axios
            .put(`${this.API_URL}/${serviceInstanceId}/groups`, groupIds)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }
}

export default new ServiceInstancesRestApi()
