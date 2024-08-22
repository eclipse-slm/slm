import logRequestError from '@/api/restApiHelper'
import {ClustersRestControllerApi} from "@/api/resource-management/client";

class ClustersRestApi {
    API_URL = '/resource-management/resources/clusters'

    api = new ClustersRestControllerApi(undefined, "/resource-management")
    realm = 'fabos'

    async getClusterTypes() {
        return this.api.getClusterTypes(this.realm)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getCluster() {
        return this.api.getClusterResources(this.realm)
            .then(response => {
                if (typeof response.data !== 'undefined') {
                    return response.data
                }
            })
            .catch(logRequestError)
    }

    async createClusterResource(clusterTypeId, skipInstall, clusterMembers, configParameterValues) {

        let clusterCreateRequest = {
            clusterTypeId: clusterTypeId,
            skipInstall: skipInstall,
            clusterMembers: clusterMembers,
            configParameterValues: configParameterValues
        };


        return this.api.createClusterResource(this.realm, clusterCreateRequest)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async deleteClusterResource(clusterId) {
        return this.api.deleteClusterResource(clusterId, this.realm)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async addMemberToCluster(clusterUuid, resourceId) {
        return this.api.addClusterMember(clusterUuid, resourceId, this.realm)
            .then(response => {
                return response.data
            })
    }

    async removeMemberFromCluster(clusterUuid, resourceId) {
        return await this.api.removeClusterMember(clusterUuid, resourceId, this.realm)
            .then(response => {
                return response.data
            })
    }
}

export default new ClustersRestApi()
