import axios from 'axios'
import logRequestError from '@/api/restApiHelper'
import {LocationRestControllerApi, ProfilerRestControllerApi} from "@/api/resource-management/client";

class ProfilerRestApi {
  API_URL = '/resource-management/resources/profiler'

  api = new ProfilerRestControllerApi(undefined, "/resource-management")
  realm = 'fabos'

  async getProfiler () {
    return this.api.getProfiler(this.realm)
      .then(response => {
        if (typeof response.data !== 'undefined') {
          return response.data
        }
      })
      .catch(logRequestError)
  }

  async runProfiler () {
    return this.api.runProfiler1(this.realm)
      .catch(logRequestError)
  }
}

export default new ProfilerRestApi()