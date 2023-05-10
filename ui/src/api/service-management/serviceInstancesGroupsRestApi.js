import axios from 'axios'
import logRequestError from '@/api/restApiHelper.js'

class ServiceInstancesGroupsRestApi {
    API_URL = '/service-management/services/instances/groups'

    async getServiceInstanceGroups () {
        return axios
            .get(this.API_URL + '/')
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async deleteServiceInstanceGroup (serviceId) {
        return axios
            .delete(`${this.API_URL}/${serviceId}`)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async createOrUpdateServiceInstanceGroup(serviceInstanceGroupId, serviceInstanceGroup) {
        return axios
            .put(`${this.API_URL}/${serviceInstanceGroupId}`, serviceInstanceGroup)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }
}

export default new ServiceInstancesGroupsRestApi()
