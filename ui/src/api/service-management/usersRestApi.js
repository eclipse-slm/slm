import axios from 'axios'
import logRequestError from '@/api/restApiHelper'

class ServiceVendorsRestApi {
    API_URL = '/service-management/services/users'

    async getUsers () {
        return axios
            .get(this.API_URL,
            )
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getServiceVendorsOfDeveloper (developerUserId) {
        return axios
            .get(this.API_URL + '/vendors/', {
                params: {
                    withImage: true,
                    developerUserId: developerUserId,
                },
            })
            .then(response => {
                return response.data
            })
        .catch(logRequestError)
    }
}

export default new ServiceVendorsRestApi()
