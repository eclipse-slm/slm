import ApiState from '@/api/apiState.js'
import {UUID} from "vue-uuid";
import {defineStore} from "pinia";
import logRequestError from "@/api/restApiHelper";
import ServiceManagementClient from "@/api/service-management/service-management-client";


interface ServiceOfferingsStoreState{
    apiState: number

    serviceVendors: any[],

    serviceOfferings: any[],
    serviceOfferingCategories: any[],
    serviceOfferingDeploymentTypes: any[],

    serviceManagementSystemVariables: any[],
    serviceManagementDeploymentVariables: any[],
}

export const useServiceOfferingsStore = defineStore('serviceOfferingStore', {
    persist: true,

    state:():ServiceOfferingsStoreState => ({
        apiState: ApiState.INIT,
        serviceVendors: [],

        serviceOfferings: [],
        serviceOfferingCategories: [],
        serviceOfferingDeploymentTypes: [],

        serviceManagementSystemVariables: [],
        serviceManagementDeploymentVariables: [],
    }),
    getters: {
        serviceVendorById: (state) => (id) => {
            return state.serviceVendors.find(serviceVendor => serviceVendor.id === id)
        },

        serviceOfferingById: (state) => (id) => {
            return state.serviceOfferings.find(service => service.id === id)
        },

        serviceOfferingCoverImage: (state) => (id: UUID) => {
            const serviceOffering = state.serviceOfferings.find(service => service.id === id)
            return serviceOffering?.coverImage
        },

        serviceOfferingCategoryNameById: (state) => (id) => {
            const foundCategory = state.serviceOfferingCategories.find(serviceCategory => serviceCategory.id === id)
            if (foundCategory) {
                return foundCategory.name
            } else {
                console.warn("Category with id '" + id + "' unknown")
                return id
            }
        },

        serviceOfferingDeploymentTypePrettyName: (state) => (deploymentType) => {
            const foundDeploymentType = state.serviceOfferingDeploymentTypes
                .find(deploymentTypeEntry => deploymentTypeEntry.value === deploymentType)
            if (foundDeploymentType) {
                return foundDeploymentType.prettyName
            } else {
                console.warn("Pretty name of deployment type '" + deploymentType + "' unknown")
                return ''
            }
        },

        valueOfTemplateVariable: (state) => (templateVariableKey) => {
            return state.serviceManagementSystemVariables.filter(tv => tv.key === templateVariableKey)[0].value
        },

    },

    actions: {
        setServiceOfferingCoverImage(o: { serviceOfferingId, coverImage }){
            const serviceOffering = this.serviceOfferings.find(serviceOffering => serviceOffering.id === o.serviceOfferingId)
            if(serviceOffering){
                serviceOffering.coverImage = o.coverImage
            }
        },

        setDeploymentTypes (deploymentTypeValues) {
            this.serviceOfferingDeploymentTypes = []
            if (Array.isArray(deploymentTypeValues)) {
                deploymentTypeValues.forEach((deploymentType) => {
                    switch (deploymentType) {
                        case 'DOCKER_CONTAINER':
                            this.serviceOfferingDeploymentTypes.push({
                                value: deploymentType,
                                prettyName: 'Docker Container'
                            })
                            break
                        case 'DOCKER_COMPOSE':
                            this.serviceOfferingDeploymentTypes.push({
                                value: deploymentType,
                                prettyName: 'Docker Compose'
                            })
                            break
                        case 'KUBERNETES':
                            this.serviceOfferingDeploymentTypes.push({value: deploymentType, prettyName: 'Kubernetes'})
                            break
                        case 'CODESYS':
                            this.serviceOfferingDeploymentTypes.push({value: deploymentType, prettyName: 'Codesys'})
                            break
                        default:
                            this.serviceOfferingDeploymentTypes.push({
                                value: deploymentType,
                                prettyName: deploymentType
                            })
                            break
                    }
                })
            }
        },

        async getVariables () {
            await ServiceManagementClient.variablesApi.getSystemVariables()
                .then(
                    response => {
                        this.serviceManagementSystemVariables = response.data;
                    }).catch(logRequestError)

            await ServiceManagementClient.variablesApi.getDeploymentVariables()
                .then(
                    response => {
                        this.serviceManagementDeploymentVariables = response.data;
                    }).catch(logRequestError)
        },

        async getServiceOfferingCategories () {
            await ServiceManagementClient.serviceCategoriesApi.getServiceCategories()
                .then(
                    response => {
                        this.serviceOfferingCategories = response.data;
                    }).catch(logRequestError);
        },

        async getServiceOfferingDeploymentTypes () {
            await ServiceManagementClient.serviceOfferingsApi.getDeploymentTypes()
                .then(
                    response => {
                        this.setDeploymentTypes(response.data)
                    }).catch(logRequestError)
        },

        async getServiceOfferings () {
            await ServiceManagementClient.serviceOfferingsApi.getServiceOfferings()
                .then(
                    response => {
                        this.serviceOfferings = response.data
                    }).catch(logRequestError)
        },

        async getServiceOfferingImages (serviceOfferingId: UUID) {
            const coverImage = this.serviceOfferingCoverImage(serviceOfferingId)
            if (coverImage == null) {
                return ServiceManagementClient.serviceOfferingsApi.getServiceOfferingCover(`${serviceOfferingId}`)
                    .then(
                        response => {
                            if (response.data && response.data !== ''){
                                let coverImage = response.data;
                                if (coverImage != null && coverImage !== '') {
                                    if (!coverImage.startsWith('data:')) {
                                        coverImage = 'data:image/jpeg;base64,' + coverImage
                                    }
                                }
                                this.setServiceOfferingCoverImage({ serviceOfferingId, coverImage })
                                return coverImage
                            }
                        }).catch(logRequestError)
            } else {
                return coverImage
            }
        },

        async getServiceVendors () {
            return await ServiceManagementClient.serviceVendorsApi.getServiceVendors()
                .then(
                    response => {
                        this.serviceVendors = response.data
                    }).catch(logRequestError)
        },
        async updateStore () {
            if (this.apiState === ApiState.INIT) {
                this.apiState = ApiState.LOADING;
            } else {
                this.apiState = ApiState.UPDATING;
            }

            return Promise.all([
                this.getVariables(),
                this.getServiceVendors(),
                this.getServiceOfferingCategories(),
                this.getServiceOfferingDeploymentTypes(),
                this.getServiceOfferings(),
            ]).then(() => {
                this.apiState = ApiState.LOADED;
                console.log("serviceOfferingsStore updated")
            }).catch((e) => {
                this.apiState = ApiState.ERROR;
                console.log("Failed to update serviceOfferingsStore: ", e)
            });
        },
    },
    
});
