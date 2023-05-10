import axios from 'axios'
import logRequestError from '@/api/restApiHelper'

class LocationsApi {
  API_URL = '/resource-management/resources/locations'

  async getLocations() {
    return axios
      .get(this.API_URL)
      .catch((error) => {
        console.debug(error)
        return []
      })
      .then(response => {
        return response.data
      })

  }
}

export default new LocationsApi()
