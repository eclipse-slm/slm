import axios from 'axios'
import logRequestError from '@/api/restApiHelper'

class ServiceVendorsRestApi {
    API_URL = '/service-management/services/vendors'

    async getVendors () {
        return axios
            .get(this.API_URL, {
                params: {
                    withImage: true,
                },
            })
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async createServiceVendor (serviceVendor) {
        return axios
            .post(this.API_URL, serviceVendor)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async createOrUpdateServiceVendorWithId (serviceVendor) {
        return axios
            .put(this.API_URL + `/${serviceVendor.id}`, serviceVendor)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async deleteServiceVendorById (serviceVendorId) {
        return axios
            .delete(this.API_URL + `/${serviceVendorId}`)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async getDevelopersOfServiceVendor (serviceVendorId) {
        return axios
            .get(this.API_URL + `/${serviceVendorId}/developers`)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async removeDeveloperFromServiceVendor (serviceVendorId, userId) {
        return axios
            .delete(this.API_URL + `/${serviceVendorId}/developers/${userId}`)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async addDeveloperToServiceVendor (serviceVendorId, userId) {
        return axios
            .put(this.API_URL + `/${serviceVendorId}/developers/${userId}`)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getRepositoriesOfServiceVendor (serviceVendorId) {
        return axios
            .get(this.API_URL + `/${serviceVendorId}/repositories`)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async addRepositoryToServiceVendor (serviceVendorId, repository) {
        console.log(repository.id)
        if (repository.id == null || repository.id == '') {
            return axios
                .post(this.API_URL + `/${serviceVendorId}/repositories`, repository)
                .then(response => {
                    return response
                })
        }
        else {
            return axios
                .put(this.API_URL + `/${serviceVendorId}/repositories/${repository.id}`, repository)
                .then(response => {
                    return response
                })
        }
    }

    async deleteRepositoryOfServiceVendor (serviceVendorId, repositoryId) {
        return axios
            .delete(this.API_URL + `/${serviceVendorId}/repositories/${repositoryId}`)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }
}

export default new ServiceVendorsRestApi()
