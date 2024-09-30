import ApiState from '@/api/apiState.js'
import {UUID} from "vue-uuid";
import {app} from "@/main";
import {defineStore} from "pinia";
import logRequestError from "@/api/restApiHelper";
import ServiceManagementClient from "@/api/service-management/service-management-client";


interface ServiceStoreState{
    apiStateServices_: {
        serviceOfferings: number,
        serviceVendors: number,
        serviceOfferingCategories: number,
        serviceOfferingDeploymentTypes: number,
        services: number,
        serviceInstanceGroups: number
    },

    serviceVendors_: any[],

    serviceOfferings_: any[],
    serviceOfferingCategories_: any[],
    serviceOfferingDeploymentTypes_: any[],

    services_: any[],
    servicesMarkedForDelete_: any[],

    serviceInstanceGroups_: any[],

    serviceManagementSystemVariables_: any[],
    serviceManagementDeploymentVariables_: any[],
}

export const useServicesStore = defineStore('servicesStore', {
    persist: true,
    state:():ServiceStoreState => ({
        apiStateServices_: {
            serviceOfferings: ApiState.INIT,
            serviceVendors: ApiState.INIT,
            serviceOfferingCategories: ApiState.INIT,
            serviceOfferingDeploymentTypes: ApiState.INIT,
            services: ApiState.INIT,
            serviceInstanceGroups: ApiState.INIT
        },

        serviceVendors_: [],

        serviceOfferings_: [],
        serviceOfferingCategories_: [],
        serviceOfferingDeploymentTypes_: [],

        services_: [],
        servicesMarkedForDelete_: [],

        serviceInstanceGroups_: [],

        serviceManagementSystemVariables_: [],
        serviceManagementDeploymentVariables_: [],
    }),
    getters: {
        apiStateServices: (state) => {
            return state.apiStateServices_
        },

        serviceVendors: (state) => {
            return state.serviceVendors_
        },
        serviceVendorById: (state) => (id) => {
            return state.serviceVendors_.find(serviceVendor => serviceVendor.id === id)
        },

        serviceOfferings: (state) => {
            return state.serviceOfferings_
        },

        serviceOfferingById: (state) => (id) => {
            return state.serviceOfferings_.find(service => service.id === id)
        },

        serviceOfferingCoverImage: (state) => (id: UUID) => {
            const serviceOffering = state.serviceOfferings_.find(service => service.id === id)
            return serviceOffering?.coverImage
        },

        serviceOfferingCategories: (state) => {
            return state.serviceOfferingCategories_
        },

        serviceOfferingCategoryNameById: (state) => (id) => {
            const foundCategory = state.serviceOfferingCategories_.find(serviceCategory => serviceCategory.id === id)
            if (foundCategory) {
                return foundCategory.name
            } else {
                console.warn("Category with id '" + id + "' unknown")
                return id
            }
        },

        serviceOfferingDeploymentTypes: (state) => {
            return state.serviceOfferingDeploymentTypes_
        },

        serviceOfferingDeploymentTypePrettyName: (state) => (deploymentType) => {
            const foundDeploymentType = state.serviceOfferingDeploymentTypes_
                .find(deploymentTypeEntry => deploymentTypeEntry.value === deploymentType)
            if (foundDeploymentType) {
                return foundDeploymentType.prettyName
            } else {
                console.warn("Pretty name of deployment type '" + deploymentType + "' unknown")
                return ''
            }
        },

        services: (state) => {
            if(state?.services_ === undefined)
                return []
            return state.services_
        },

        serviceManagementSystemVariables: (state) => {
            return state.serviceManagementSystemVariables_
        },

        valueOfTemplateVariable: (state) => (templateVariableKey) => {
            return state.serviceManagementSystemVariables_.filter(tv => tv.key === templateVariableKey)[0].value
        },

        serviceManagementDeploymentVariables: (state) => {
            return state.serviceManagementDeploymentVariables_
        },

        serviceInstanceGroups: (state) => {
            return state.serviceInstanceGroups_
        },
        serviceInstanceGroupById: (state) => (id) => {
            return state.serviceInstanceGroups_.find(group => group.id === id)
        },
    },

    actions: {

        setServiceOfferingCoverImage(o: { serviceOfferingId, coverImage }){
            const serviceOffering = this.serviceOfferings_.find(serviceOffering => serviceOffering.id === o.serviceOfferingId)
            if(serviceOffering){
                serviceOffering.coverImage = o.coverImage
            }
        },

        setDeploymentTypes (deploymentTypeValues) {
            this.serviceOfferingDeploymentTypes_ = []
            deploymentTypeValues.forEach((deploymentType) => {
                switch (deploymentType) {
                    case 'DOCKER_CONTAINER':
                        this.serviceOfferingDeploymentTypes_.push({ value: deploymentType, prettyName: 'Docker Container' })
                        break
                    case 'DOCKER_COMPOSE':
                        this.serviceOfferingDeploymentTypes_.push({ value: deploymentType, prettyName: 'Docker Compose' })
                        break
                    case 'KUBERNETES':
                        this.serviceOfferingDeploymentTypes_.push({ value: deploymentType, prettyName: 'Kubernetes' })
                        break
                    case 'CODESYS':
                        this.serviceOfferingDeploymentTypes_.push({ value: deploymentType, prettyName: 'Codesys' })
                        break
                    default:
                        this.serviceOfferingDeploymentTypes_.push({ value: deploymentType, prettyName: deploymentType })
                        break
                }
            })
        },

        setServices (services) {
            this.services_ = services
            this.servicesMarkedForDelete_.forEach(serviceMarkedForDelete => {
                const filteredServices = this.services_.find(service => service.id === serviceMarkedForDelete.id)
                if (filteredServices) {
                    filteredServices.markedForDelete = true
                    console.log('marked for delete')
                }
            })
        },

        setServiceMarkedForDelete (serviceToDelete) {
            if (this.servicesMarkedForDelete_.indexOf(serviceToDelete) === -1) { this.servicesMarkedForDelete_.push(serviceToDelete) }
            this.services_.forEach(service => {
                if (service.id === serviceToDelete.id) {
                    // Vue.set(service, 'markedForDelete', true)
                    // app.config.globalProperties

                    console.info("Service '" + service.id + "' marked for delete")
                }
            })
        },
        
        async initServiceStore () {
            await ServiceManagementClient.variablesApi.getSystemVariables()
                .then(
                    response => {
                        this.serviceManagementSystemVariables_ = response.data;
                    }).catch(logRequestError)

            await ServiceManagementClient.variablesApi.getDeploymentVariables()
                .then(
                    response => {
                        this.serviceManagementDeploymentVariables_ = response.data;
                    }).catch(logRequestError)
        },

        async getServiceOfferingCategories () {
            this.apiStateServices_.serviceOfferingCategories = ApiState.LOADING
            await ServiceManagementClient.serviceCategoriesApi.getServiceCategories()
                .then(
                    response => {
                        this.serviceOfferingCategories_ = response.data;
                        this.apiStateServices_.serviceOfferingCategories = ApiState.LOADED
                    }).catch(logRequestError);
        },

        async getServiceOfferingDeploymentTypes () {
            this.apiStateServices_.serviceOfferingDeploymentTypes = ApiState.LOADING
            await ServiceManagementClient.serviceOfferingsApi.getDeploymentTypes()
                .then(
                    response => {
                        this.setDeploymentTypes(response.data)
                        this.apiStateServices_.serviceOfferingDeploymentTypes = ApiState.LOADED
                    }).catch(logRequestError)
        },

        async getServiceOfferings () {
            await ServiceManagementClient.serviceOfferingsApi.getServiceOfferings()
                .then(
                    response => {
                        this.serviceOfferings_ = response.data
                        this.apiStateServices_.serviceOfferings = ApiState.LOADED
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
            this.apiStateServices_.serviceVendors = ApiState.LOADING
            return await ServiceManagementClient.serviceVendorsApi.getServiceVendors()
                .then(
                    response => {
                        this.serviceVendors_ = response.data
                        this.apiStateServices_.serviceVendors = ApiState.LOADED
                    }).catch(logRequestError)
        },

        async getServices () {
            this.apiStateServices_.services = ApiState.LOADING
            await ServiceManagementClient.serviceInstancesApi.getServicesOfUser()
                .then(
                    response => {
                        this.setServices(response.data)
                    }).catch(logRequestError)
            this.apiStateServices_.services = ApiState.LOADED
        },

        updateServicesStore () {
            this.getServices().then()
            // assure token gets refreshed
            app.config.globalProperties.$keycloak.keycloak?.updateToken(1000000)
                .then(refreshed => {
                    this.getServices().then();
                })
        },

        async getServiceInstanceGroups () {
            this.apiStateServices_.serviceInstanceGroups = ApiState.LOADING
            await ServiceManagementClient.serviceInstancesGroupsApi.getServiceInstanceGroups()
                .then(
                    response => {
                        if(response.data){
                            this.serviceInstanceGroups_ = response.data
                            this.apiStateServices_.serviceInstanceGroups = ApiState.LOADED
                        }
                    }).catch(logRequestError)
        },
    },
    
});
