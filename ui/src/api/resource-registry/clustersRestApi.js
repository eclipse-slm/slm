import axios from 'axios'
import logRequestError from '@/api/restApiHelper'

class ClustersRestApi {
    API_URL = '/resource-management/resources/clusters'

    async getClusterTypes () {
      return axios
          .get(this.API_URL + '/types')
          .then(response => {
              return response.data
          })
          .catch(logRequestError)
    }

    async getCluster () {
        return axios
          .get(this.API_URL)
          .then(response => {
              if (typeof response.data !== 'undefined') {
                return response.data
              }
          })
          .catch(logRequestError)
    }

    async createClusterResource (clusterTypeId, skipInstall, clusterMembers, configParameterValues) {
        let clusterCreateRequest = {
            clusterTypeId,
            skipInstall,
            clusterMembers,
            configParameterValues
        }

        return axios
            .post(this.API_URL, clusterCreateRequest)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async deleteClusterResource (clusterId) {
        return axios
            .delete(`${this.API_URL}/${clusterId}`)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async addMemberToCluster (clusterUuid, resourceId) {
        return await axios.post(
            `${this.API_URL}/${clusterUuid}/members`,
            {},
            {
                params: {
                    resourceId,
                },
            })
            .then(response => {
                return response.data
            })
    }

    async removeMemberFromCluster (clusterUuid, resourceId) {
        return await axios.delete(
            `${this.API_URL}/${clusterUuid}/members`,
            {
                params: {
                    resourceId,
                },
            })
            .then(response => {
                return response.data
            })
    }
}

export default new ClustersRestApi()
