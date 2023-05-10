import axios from 'axios'

class MetricsRestApi {
    API_URL = '/resource-management/metrics'

    async getMetricsOfResource (resourceId) {
        return axios.get(`${this.API_URL}/${resourceId}`)
            .then(response => {
                return response.data
            })
    }
}

export default new MetricsRestApi()
