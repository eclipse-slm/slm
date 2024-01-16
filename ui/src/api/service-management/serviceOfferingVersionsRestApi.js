import axios from 'axios'
import logRequestError from '@/api/restApiHelper.js'

class ServiceOfferingVersionsRestApi {
    API_URL = '/service-management/services/offerings'

    async getVersionsOfServiceOffering (serviceOfferingId) {
        return axios
            .get(`${this.API_URL}/${serviceOfferingId}/versions`)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getServiceOfferingVersionById (serviceOfferingId, serviceOfferingVersionId) {
        return axios
            .get(`${this.API_URL}/${serviceOfferingId}/versions/${serviceOfferingVersionId}`, {
                params: {
                    withImage: true,
                },
            })
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getServiceOfferingVersionMatchingResources (serviceOfferingId, serviceOfferingVersionId) {
        return axios
            .get(`${this.API_URL}/${serviceOfferingId}/versions/${serviceOfferingVersionId}/matching-resources`)
            .then(response => {
                return response.data
            })
    }

    async deleteServiceOfferingVersion (serviceOfferingId, serviceOfferingVersionId) {
        return axios
            .delete(`${this.API_URL}/${serviceOfferingId}/versions/${serviceOfferingVersionId}`)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async addServiceOfferingVersion (serviceOfferingVersion) {
        switch (serviceOfferingVersion.deploymentDefinition.deploymentType) {
            case 'Docker Container':
                serviceOfferingVersion.deploymentDefinition = 'DOCKER_CONTAINER'
                break
            case 'Docker Compose':
                serviceOfferingVersion.deploymentDefinition = 'DOCKER_COMPOSE'
                break
            case 'Kubernetes':
                serviceOfferingVersion.deploymentDefinition = 'KUBERNETES'
                break
            case 'Codesys':
                serviceOfferingVersion.deploymentDefinition = 'CODESYS'
                break
            default:
                break
        }

        return axios
            .post(`${this.API_URL}/${serviceOfferingVersion.serviceOfferingId}/versions`, serviceOfferingVersion)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async updateServiceOfferingVersion (serviceOfferingVersion) {
        switch (serviceOfferingVersion.deploymentDefinition.deploymentType) {
            case 'Docker Container':
                serviceOfferingVersion.deploymentDefinition.deploymentType = 'DOCKER_CONTAINER'
                break
            case 'Docker Compose':
                serviceOfferingVersion.deploymentDefinition.deploymentType = 'DOCKER_COMPOSE'
                break
            default:
                break
        }

        return axios
            .put(`${this.API_URL}/${serviceOfferingVersion.serviceOfferingId}/versions/${serviceOfferingVersion.id}`, serviceOfferingVersion)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async orderServiceOfferingVersion (serviceOfferingId, serviceOfferingVersionId, serviceOfferingVersionOrder, deploymentCapabilityServiceId) {
        return axios
            .post(`${this.API_URL}/${serviceOfferingId}/versions/${serviceOfferingVersionId}/order`, serviceOfferingVersionOrder, {
                params: {
                    deploymentCapabilityServiceId,
                },
            })
            .then(response => {
                return response
            })
    }

    async uploadFileForServiceOfferingVersion(serviceOfferingId, serviceOfferingVersionId, deploymentType, file) {
        let formData = new FormData();
        formData.append('file', file);
        return axios.post(`${this.API_URL}/${serviceOfferingId}/versions/${serviceOfferingVersionId}/file`, formData)
            .then(response => {
                return response
            });
    }

    async downloadFileForServiceOfferingVersion(serviceOfferingId, serviceOfferingVersionId,fileName){
        return axios.get(`${this.API_URL}/${serviceOfferingId}/versions/${serviceOfferingVersionId}/file/${fileName}`,
            {responseType: 'blob'});
    }

}

export default new ServiceOfferingVersionsRestApi()
