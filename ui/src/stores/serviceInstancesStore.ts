import ApiState from '@/api/apiState.js'
import {UUID} from "vue-uuid";
import {defineStore} from "pinia";
import logRequestError from "@/api/restApiHelper";
import ServiceManagementClient from "@/api/service-management/service-management-client";


interface ServiceInstancesStoreState{
    apiState: number,
    services: any[],
    servicesMarkedForDelete: any[],
    serviceInstanceGroups: any[],
}

export const useServiceInstancesStore = defineStore('serviceInstancesStore', {
    persist: true,
    state:():ServiceInstancesStoreState => ({
        apiState: ApiState.INIT,
        services: [],
        servicesMarkedForDelete: [],
        serviceInstanceGroups: [],
    }),
    getters: {
        serviceInstanceGroupById: (state) => (id) => {
            return state.serviceInstanceGroups.find(group => group.id === id)
        },
    },

    actions: {
        setServices (services) {
            this.services = services
            this.servicesMarkedForDelete.forEach(serviceMarkedForDelete => {
                const filteredServices = this.services.find(service => service.id === serviceMarkedForDelete.id)
                if (filteredServices) {
                    filteredServices.markedForDelete = true
                    console.log('marked for delete')
                }
            })
        },

        setServiceMarkedForDelete (serviceToDelete) {
            if (this.servicesMarkedForDelete.indexOf(serviceToDelete) === -1) { this.servicesMarkedForDelete.push(serviceToDelete) }
            this.services.forEach(service => {
                if (service.id === serviceToDelete.id) {
                    // Vue.set(service, 'markedForDelete', true)
                    console.info("Service '" + service.id + "' marked for delete")
                }
            })
        },

        async getServices () {
            await ServiceManagementClient.serviceInstancesApi.getServicesOfUser()
                .then(
                    response => {
                        this.setServices(response.data)
                    }).catch(logRequestError)
        },

        async getServiceInstanceGroups () {
            await ServiceManagementClient.serviceInstancesGroupsApi.getServiceInstanceGroups()
                .then(
                    response => {
                        if(response.data){
                            this.serviceInstanceGroups = response.data
                        }
                    }).catch(logRequestError)
        },
        async updateStore () {
            if (this.apiState === ApiState.INIT) {
                this.apiState = ApiState.LOADING;
            } else {
                this.apiState = ApiState.UPDATING;
            }

            return Promise.all([
                this.getServiceInstanceGroups(),
                this.getServices(),
            ]).then(() => {
                this.apiState = ApiState.LOADED;
                console.log("serviceInstancesStore updated")
            }).catch((e) => {
                this.apiState = ApiState.ERROR;
                console.log("Failed to update serviceInstancesStore: ", e)
            });
        },
    },
});
