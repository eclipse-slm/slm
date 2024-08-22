import logRequestError from '@/api/restApiHelper'
import {SubmodelsRestControllerApi} from "@/api/resource-management/client";

class SubmodelsApi {
    API_URL = '/resource-management/resources'

    api = new SubmodelsRestControllerApi(undefined, "/resource-management")
    realm = 'fabos'

    async getResourceSubmodels(resourceId) {
        return this.api.getResourceSubmodels(resourceId, this.realm)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async deleteSubmodel(resourceId, submodelIdShort) {
        return this.api.deleteSubmodel(resourceId, submodelIdShort, this.realm)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async addSubmodels(resourceId, file) {

        return this.api.addSubmodels(resourceId, this.realm, {aasx: file})
            .then(response => {
                return response.data
            }).catch(logRequestError)
    }

}

export default new SubmodelsApi()
