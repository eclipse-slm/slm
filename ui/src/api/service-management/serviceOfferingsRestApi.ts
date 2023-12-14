import axios, {AxiosResponse} from 'axios'
import logRequestError from '@/api/restApiHelper.js'
import {UUID} from "vue-uuid";
import ServiceOffering from "@/model/serviceOffering"
import {AnyObject} from "chart.js/dist/types/basic";
import ServiceOfferingGitRepository from "@/model/serviceOfferingGitRepository";

class ServiceOfferingsRestApi {
    API_URL = '/service-management/services/offerings'

    async getOfferings (withImage = false, serviceVendorIdFilter = null){
        return axios
            .get(this.API_URL + '/', {
                params: {
                    withImage: withImage,
                    filterByServiceVendorId: serviceVendorIdFilter,
                },
            })
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getServiceOfferingById (serviceOfferingId: UUID) {
        return axios
            .get(this.API_URL + `/${serviceOfferingId}`, {
                params: {
                    withImage: true,
                },
            })
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getServiceOfferingCoverImage (serviceOfferingId: UUID) {
        return axios
            .get(this.API_URL + `/${serviceOfferingId}/cover`,
            )
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async deleteServiceOffering (serviceOfferingId: UUID) {
        return axios
            .delete(this.API_URL + `/${serviceOfferingId}`)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async addServiceOffering (serviceOffering: ServiceOffering) {
        return axios
            .post(this.API_URL + '/', serviceOffering)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async updateServiceOffering (serviceOffering: ServiceOffering) {
        return axios
            .put(this.API_URL + `/${serviceOffering.id}`, serviceOffering)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async addServiceOfferingViaGit (serviceOfferingGitRepository: ServiceOfferingGitRepository) {
        return axios
            .post(this.API_URL + '/git', serviceOfferingGitRepository)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async getDeploymentTypes () {
        return axios
            .get(this.API_URL + '/deploymenttypes')
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getServiceCategories () {
        return axios
            .get(this.API_URL + '/categories')
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async createServiceCategory (serviceCategory: AnyObject) {
        return axios
            .post(this.API_URL + '/categories', serviceCategory)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async createOrUpdateServiceCategoryWithId (serviceCategory: AnyObject) {
        return axios
            .put(this.API_URL + '/categories', serviceCategory)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }

    async deleteServiceCategory (serviceCategoryId: bigint) {
        return axios
            .delete(this.API_URL + `/categories/${serviceCategoryId}`)
            .then(response => {
                return response
            })
            .catch(logRequestError)
    }
}

export default new ServiceOfferingsRestApi()
