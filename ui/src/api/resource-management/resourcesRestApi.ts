import logRequestError from '@/api/restApiHelper'
import {
    CapabilitiesRestControllerApi,
    ClustersRestControllerApi,
    ResourcesRestControllerApi
} from "@/api/resource-management/client";

class ResourcesApi {
    API_URL = '/resource-management/resources'

    api = new ResourcesRestControllerApi(undefined, "/resource-management")
    clusterApi = new ClustersRestControllerApi(undefined, "/resource-management")
    capabilityApi = new CapabilitiesRestControllerApi(undefined, "/resource-management")
    realm = 'fabos'

//region Resources
    async getResourceConnectionTypes() {

        return this.api.getResourceConnectionTypes('fabos')
            .then(response => {
                if (typeof response.data !== 'undefined') {
                    return response.data
                }
            })
            .catch((error) => {
                console.debug(error)
                return []
            })
            .catch(logRequestError)
    }

    async getResources() {
        return this.api.getResources('fabos')
            .then(response => {
                if (typeof response.data !== 'undefined') {
                    return response.data
                }
            })
            .catch((error) => {
                console.debug(error)
                return []
            })
            .catch(logRequestError)
    }

    async addExistingResource(
        resourceHostname,
        resourceIp,
        resourceLocation,
        resourceUsername,
        resourcePassword,
        resourceType,
        resourceAccessAvailable,
        resourceConnectionType,
        resourceConnectionPort,
        resourceBaseConfigurationId
    ) {

        return this.api.addExistingResource(
            resourceHostname, resourceIp, this.realm, resourceUsername, resourcePassword, resourceConnectionType,
            resourceConnectionPort, resourceBaseConfigurationId,
        )
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    /**
     * DELETE /{resourceId}
     * @param resourceId    Id of the resource which should be deleted.
     */
    async deleteResource(resourceId) {
        return this.api.deleteResource(resourceId, this.realm)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    //endregion


    //region Capabilities
    async getDeploymentCapabilities() {
        return this.capabilityApi.getCapabilities(this.realm)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async createClusterResource(selectedNetwork, selectedClusterType, selectedClusterMembers) {
        return this.clusterApi.createClusterResource(this.realm, selectedClusterMembers)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async addCapabilityToSingleHost(resourceId, capabilityId, skipInstall, configParameters) {

        return this.capabilityApi.createCapability(this.realm, configParameters, {
            params: {
                resourceId: resourceId,
                capabilityId,
                skipInstall
            }
        })
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async removeCapabilityFromSingleHost(resourceId, capabilityId) {

        return this.capabilityApi.removeCapabilityFromSingleHost(resourceId, capabilityId, this.realm, {
            params: {
                resourceId,
                capabilityId,
            },
        })
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    //endregion
    getOtp(project, ip, username) {
        return Promise.resolve('');
    }
}

export default new ResourcesApi()
