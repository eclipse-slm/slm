import axios from 'axios'
import {SubmodelsRestControllerApi, SubmodelTemplatesRestControllerApi} from "@/api/resource-management/client";

class AASRestApi {
    API_URL = '/resource-management/aas'

    api = new SubmodelTemplatesRestControllerApi(undefined, "/resource-management")
    realm = 'fabos'


    constructor() {
    }

    async getResourceAAS() {
        return this.api.getResourceAASDescriptors(this.realm).then( response => {
            return response.data;
        });
        /*return axios.get(`${this.API_URL}/`)
          .then(response => {
              return response.data;
          });*/
    }

    async getSubmodelTemplateInstancesBySemanticId (submodelTemplateSemanticId) {
        return this.api.getSubmodelTemplateInstancesBySemanticId(submodelTemplateSemanticId, this.realm)
            .then(response => {
                return response.data;
            });
    }

    async getSubmoduleTemplateInstanceOfAas (submodelTemplateSemanticId, aasId) {
        return this.api.getSubmodelTemplateInstancesBySemanticId(submodelTemplateSemanticId, this.realm, aasId)
        .then(response => {
            return response.data;
        });
    }
}

export default new AASRestApi()
