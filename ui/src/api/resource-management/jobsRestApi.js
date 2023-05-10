import axios from 'axios'
import logRequestError from '@/api/restApiHelper'

class JobsApi {
    API_URL = '/resource-management/jobs'

    async getJobs () {
        return axios
            .get(this.API_URL)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }
}

export default new JobsApi()
