import axios from 'axios'
import logRequestError from '@/api/restApiHelper'

class SubmodelsApi {
  API_URL = '/resource-management/resources'

  async getResourceSubmodels(resourceId) {
    return axios.get(
      this.API_URL + `/${resourceId}/submodels`,
    )
      .then(response => {
        return response.data
      })
      .catch(logRequestError)
  }

  async deleteSubmodel(resourceId, submodelIdShort) {
    return axios
      .delete(this.API_URL + `/${resourceId}/submodels/${submodelIdShort}`, {})
      .then(response => {
        return response.data
      })
      .catch(logRequestError)
  }

  async addSubmodels(resourceId, file) {
    const formData = new FormData()
    formData.append('aasx', file, file.name)
    return axios.post(this.API_URL + `/${resourceId}/submodels`, formData).then(response => {
      return response.data
    }).catch(logRequestError)
  }

}

export default new SubmodelsApi()
