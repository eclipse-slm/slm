import axios from 'axios'
import logRequestError from '@/api/restApiHelper'

class ProfilerRestApi {
  API_URL = '/resource-management/resources/profiler'

  async getProfiler () {
    return axios
      .get(this.API_URL)
      .then(response => {
        if (typeof response.data !== 'undefined') {
          return response.data
        }
      })
      .catch(logRequestError)
  }

  async runProfiler () {
    return axios
      .post(this.API_URL+'/execute')
      .catch(logRequestError)
  }
}

export default new ProfilerRestApi()