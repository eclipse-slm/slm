import axios from 'axios'
import logRequestError from '@/api/restApiHelper'

class ResourcesApi {
    API_URL = '/resource-management/resources'


    //region Resources
    async getResourceConnectionTypes() {
      return axios
        .get(this.API_URL+ '/connection-types')
        .catch((error) => {
          console.debug(error)
          return []
        })
        .then(response => {
          if (typeof response.data !== 'undefined') {
            return response.data
          }
        })
        .catch(logRequestError)
    }
    async getResources () {
        return axios
            .get(this.API_URL)
            .catch((error) => {
                console.debug(error)
                return []
              })
            .then(response => {
                if (typeof response.data !== 'undefined') {
                  return response.data
                }
              })
            .catch(logRequestError)
    }
    async addExistingResource (
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
        return axios
            .put(
                this.API_URL, {},
                {
                    params: {
                        resourceHostname: resourceHostname,
                        resourceIp: resourceIp,
                        resourceLocation: resourceLocation,
                        resourceUsername: resourceUsername,
                        resourcePassword: resourcePassword,
                        resourceConnectionType: resourceConnectionType,
                        resourceConnectionPort: resourceConnectionPort,
                        resourceBaseConfiguration: resourceBaseConfigurationId
                    },
                })
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }
    /**
     * DELETE /{resourceId}
     * @param resourceId    Id of the resource which should be deleted.
     */
    async deleteResource (resourceId) {
        return axios
            .delete(this.API_URL + '/' + resourceId, { })
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }
    //endregion


    //region Capabilities
    async getDeploymentCapabilities () {
    return axios
      .get(this.API_URL + '/capabilities')
      .then(response => {
        return response.data
      })
      .catch(logRequestError)
  }
    async createClusterResource (selectedNetwork, selectedClusterType, selectedClusterMembers) {
    return axios
      .post(
        this.API_URL + '/clusters',
        selectedClusterMembers,
        {
          params: {
            clusterType: selectedClusterType,
          },
        })
      .then(response => {
        return response.data
      })
      .catch(logRequestError)
  }
    async addCapabilityToSingleHost (resourceId, capabilityId, skipInstall, configParameters) {
    return axios.put(
      this.API_URL + `/${resourceId}/capabilities`,
      configParameters,
      {
        params: {
          resourceId: resourceId,
          capabilityId,
          skipInstall
        },
      })
      .then(response => {
        return response.data
      })
      .catch(logRequestError)
  }
    async removeCapabilityFromSingleHost (resourceId, capabilityId) {
    return axios.delete(
      this.API_URL + `/${resourceId}/capabilities`,
      {
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
}

export default new ResourcesApi()
