import axios from 'axios'
import logRequestError from '@/api/restApiHelper'

class ResourceProvidersApi {
    API_URL = '/resource-management/resources'

    async getVirtualResourceProviders () {
        return axios
            .get(this.API_URL + '/providers/virtual-resource-provider')
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getServiceHosters () {
        return axios
          .get(this.API_URL + '/providers/service-hoster')
          .then(response => {
              return response.data
          })
          .catch(logRequestError)
    }
}

export default new ResourceProvidersApi()
