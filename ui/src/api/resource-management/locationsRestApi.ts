import {LocationRestControllerApi} from "@/api/resource-management/client";

class LocationsApi {
    API_URL = '/resource-management/resources/locations'

    api = new LocationRestControllerApi(undefined, "/resource-management")
    realm = 'fabos'

    async getLocations() {
        return this.api.getLocations(this.realm)
            .then(response => {
                return response.data
            })
            .catch((error) => {
                console.debug(error)
                return []
            })


    }
}

export default new LocationsApi()
