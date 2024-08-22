import logRequestError from '@/api/restApiHelper'
import {CapabilityProvidersRestControllerApi} from "@/api/resource-management/client";

class ResourceProvidersApi {
    API_URL = '/resource-management/resources'

    api = new CapabilityProvidersRestControllerApi(undefined, "/resource-management")
    realm = 'fabos'

    async getVirtualResourceProviders() {
        return this.api.getVirtualResourceProviders(this.realm)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }

    async getServiceHosters() {
        return this.api.getServiceHosters(this.realm)
            .then(response => {
                return response.data
            })
            .catch(logRequestError)
    }
}

export default new ResourceProvidersApi()
