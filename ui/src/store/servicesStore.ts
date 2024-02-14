import ServiceOfferingsRestApi from '@/api/service-management/serviceOfferingsRestApi'
import ServiceInstancesRestApi from '@/api/service-management/serviceInstancesRestApi'
import ServiceVendorsRestApi from '@/api/service-management/serviceVendorsRestApi'
import ApiState from '@/api/apiState.js'
import Vue from 'vue'
import ServiceOffering from "@/model/serviceOffering";
import {UUID} from "vue-uuid";
import ServiceManagementTemplatesRestApi from "@/api/service-management/serviceManagementVariablesRestApi";
import ServiceInstancesGroupsRestApi from "@/api/service-management/serviceInstancesGroupsRestApi";

export default {
    state: {
        apiStateServices: {
            serviceOfferings: ApiState.INIT,
            serviceVendors: ApiState.INIT,
            serviceOfferingCategories: ApiState.INIT,
            serviceOfferingDeploymentTypes: ApiState.INIT,
            services: ApiState.INIT,
            serviceInstanceGroups: ApiState.INIT
        },

        serviceVendors: [],

        serviceOfferings: [],
        serviceOfferingCategories: [],
        serviceOfferingDeploymentTypes: [],

        services: [],
        servicesMarkedForDelete: [],

        serviceInstanceGroups: [],

        serviceManagementSystemVariables: [],
        serviceManagementDeploymentVariables: [],
    },

    getters: {
        apiStateServices: (state) => {
            return state.apiStateServices
        },

        serviceVendors: (state) => {
            return state.serviceVendors
        },
        serviceVendorById: (state) => (id) => {
            return state.serviceVendors.find(serviceVendor => serviceVendor.id === id)
        },

        serviceOfferings: (state) => {
            return state.serviceOfferings
        },

        serviceOfferingById: (state) => (id) => {
            return state.serviceOfferings.find(service => service.id === id)
        },

        serviceOfferingCoverImage: (state) => (id: UUID) => {
            const serviceOffering = state.serviceOfferings.find(service => service.id === id)
            return serviceOffering?.coverImage
        },

        serviceOfferingCategories: (state) => {
            return state.serviceOfferingCategories
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

        serviceOfferingDeploymentTypes: (state) => {
            return state.serviceOfferingDeploymentTypes
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

        services: (state) => {
            return state.services
        },

        serviceManagementSystemVariables: (state) => {
            return state.serviceManagementSystemVariables
        },

        valueOfTemplateVariable: (state) => (templateVariableKey) => {
            return state.serviceManagementSystemVariables.filter(tv => tv.key === templateVariableKey)[0].value
        },

        serviceManagementDeploymentVariables: (state) => {
            return state.serviceManagementDeploymentVariables
        },

        serviceInstanceGroups: (state) => {
            return state.serviceInstanceGroups
        },
        serviceInstanceGroupById: (state) => (id) => {
            return state.serviceInstanceGroups.find(group => group.id === id)
        },
    },

    mutations: {
        SET_API_STATE_SERVICE_OFFERINGS (state, apiState) {
            state.apiStateServices.serviceOfferings = apiState
        },
        SET_API_STATE_SERVICE_VENDORS (state, apiState) {
            state.apiStateServices.serviceVendors = apiState
        },
        SET_API_STATE_SERVICE_OFFERINGS_CATEGORIES (state, apiState) {
            state.apiStateServices.serviceOfferingCategories = apiState
        },
        SET_API_STATE_SERVICE_OFFERINGS_DEPLOYMENT_TYPES (state, apiState) {
            state.apiStateServices.serviceOfferingDeploymentTypes = apiState
        },
        SET_API_STATE_SERVICES (state, apiState) {
            state.apiStateServices.services = apiState
        },
        SET_API_STATE_SERVICE_INSTANCE_GROUPS (state, apiState) {
            state.apiStateServices.serviceInstanceGroups = apiState
        },

        SET_CATEGORIES (state, categories) {
            state.serviceOfferingCategories = categories
        },

        SET_SERVICE_OFFERINGS (state, offerings: ServiceOffering[]) {
            state.serviceOfferings = offerings
        },

        SET_SERVICE_OFFERING_COVER_IMAGE (state, { serviceOfferingId, coverImage }) {
            const serviceOffering = state.serviceOfferings.find(serviceOffering => serviceOffering.id === serviceOfferingId)
            serviceOffering.coverImage = coverImage
        },

        SET_DEPLOYMENT_TYPES (state, deploymentTypeValues) {
            state.serviceOfferingDeploymentTypes = []
            deploymentTypeValues.forEach((deploymentType) => {
                switch (deploymentType) {
                    case 'DOCKER_CONTAINER':
                        state.serviceOfferingDeploymentTypes.push({ value: deploymentType, prettyName: 'Docker Container' })
                        break
                    case 'DOCKER_COMPOSE':
                        state.serviceOfferingDeploymentTypes.push({ value: deploymentType, prettyName: 'Docker Compose' })
                        break
                    case 'KUBERNETES':
                        state.serviceOfferingDeploymentTypes.push({ value: deploymentType, prettyName: 'Kubernetes' })
                        break
                    case 'CODESYS':
                        state.serviceOfferingDeploymentTypes.push({ value: deploymentType, prettyName: 'Codesys' })
                        break
                    default:
                        state.serviceOfferingDeploymentTypes.push({ value: deploymentType, prettyName: deploymentType })
                        break
                }
            })
        },

        SET_SERVICES (state, services) {
            state.services = services
            state.servicesMarkedForDelete.forEach(serviceMarkedForDelete => {
                const filteredServices = state.services.find(service => service.id === serviceMarkedForDelete.id)
                if (filteredServices) {
                    filteredServices.markedForDelete = true
                    console.log('marked for delete')
                }
            })
        },

        SET_SERVICE_MARKED_FOR_DELETE (state, serviceToDelete) {
            if (state.servicesMarkedForDelete.indexOf(serviceToDelete) === -1) { state.servicesMarkedForDelete.push(serviceToDelete) }
            state.services.forEach(service => {
                if (service.id === serviceToDelete.id) {
                    Vue.set(service, 'markedForDelete', true)
                    console.info("Service '" + service.id + "' marked for delete")
                }
            })
        },

        SET_SERVICE_MANAGEMENT_SYSTEM_VARIABLES (state, systemVariables) {
            state.serviceManagementSystemVariables = systemVariables
        },

        SET_SERVICE_MANAGEMENT_DEPLOYMENT_VARIABLES (state, deploymentVariables) {
            state.serviceManagementDeploymentVariables = deploymentVariables
        },

        SET_SERVICE_INSTANCE_GROUPS (state, groups) {
            state.serviceInstanceGroups = groups
        }
    },

    actions: {
        async initServiceStore (context) {
            await ServiceManagementTemplatesRestApi.getSystemVariables()
                .then(
                    templateVariables => {
                        context.commit('SET_SERVICE_MANAGEMENT_SYSTEM_VARIABLES', templateVariables)
                    })

            await ServiceManagementTemplatesRestApi.getDeploymentVariables()
                .then(
                    templateVariables => {
                        context.commit('SET_SERVICE_MANAGEMENT_DEPLOYMENT_VARIABLES', templateVariables)
                    })
        },

        async getServiceOfferingCategories (context) {
            context.commit('SET_API_STATE_SERVICE_OFFERINGS_CATEGORIES', ApiState.LOADING)
            await ServiceOfferingsRestApi.getServiceCategories()
                .then(
                    response => {
                        context.commit('SET_CATEGORIES', response)
                        context.commit('SET_API_STATE_SERVICE_OFFERINGS_CATEGORIES', ApiState.LOADED)
                    })
        },

        async getServiceOfferingDeploymentTypes (context) {
            context.commit('SET_API_STATE_SERVICE_OFFERINGS_DEPLOYMENT_TYPES', ApiState.LOADING)
            await ServiceOfferingsRestApi.getDeploymentTypes()
                .then(
                    response => {
                        context.commit('SET_DEPLOYMENT_TYPES', response)
                        context.commit('SET_API_STATE_SERVICE_OFFERINGS_DEPLOYMENT_TYPES', ApiState.LOADED)
                    })
        },

        async getServiceOfferings (context) {
            context.commit('SET_API_STATE_SERVICE_OFFERINGS', ApiState.LOADING)
            await ServiceOfferingsRestApi.getOfferings()
                .then(
                    serviceOfferings => {
                        context.commit('SET_SERVICE_OFFERINGS', serviceOfferings)
                        context.commit('SET_API_STATE_SERVICE_OFFERINGS', ApiState.LOADED)
                    })
        },

        async getServiceOfferingImages (context, serviceOfferingId: UUID) {
            const coverImage = context.getters.serviceOfferingCoverImage(serviceOfferingId)
            if (coverImage == null) {
                return ServiceOfferingsRestApi.getServiceOfferingCoverImage(serviceOfferingId)
                    .then(
                        coverImage => {
                            if (coverImage != null && coverImage !== '') {
                                if (!coverImage.startsWith('data:')) {
                                    coverImage = 'data:image/jpeg;base64,' + coverImage
                                }
                            }
                            context.commit('SET_SERVICE_OFFERING_COVER_IMAGE', { serviceOfferingId, coverImage })
                            return coverImage
                        })
            } else {
                return coverImage
            }
        },

        async getServiceVendors (context) {
            context.commit('SET_API_STATE_SERVICE_VENDORS', ApiState.LOADING)
            return await ServiceVendorsRestApi.getVendors()
                .then(
                    response => {
                        context.state.serviceVendors = response
                        context.commit('SET_API_STATE_SERVICE_VENDORS', ApiState.LOADED)
                    })
        },

        async getServices (context) {
            context.commit('SET_API_STATE_SERVICES', ApiState.LOADING)
            await ServiceInstancesRestApi.getServiceInstances()
                .then(
                    response => {
                        context.commit('SET_SERVICES', response)
                    })
            context.commit('SET_API_STATE_SERVICES', ApiState.LOADED)
        },

        updateServicesStore (context) {
            context.dispatch('getServices')
            // assure token gets refreshed
            Vue.prototype.$keycloak.keycloak.updateToken(1000000)
                .then(refreshed => {
                    context.dispatch('getServices')
                })
        },

        async getServiceInstanceGroups (context) {
            context.commit('SET_API_STATE_SERVICE_INSTANCE_GROUPS', ApiState.LOADING)
            await ServiceInstancesGroupsRestApi.getServiceInstanceGroups()
                .then(
                    response => {
                        context.commit('SET_SERVICE_INSTANCE_GROUPS', response)
                        context.commit('SET_API_STATE_SERVICE_INSTANCE_GROUPS', ApiState.LOADED)
                    })
        },
    },
}
