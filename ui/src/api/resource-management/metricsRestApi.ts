import {MetricsRestControllerApi} from "@/api/resource-management/client";

class MetricsRestApi {
    API_URL = '/resource-management/metrics'

    api = new MetricsRestControllerApi(undefined, "/resource-management")
    realm = 'fabos'

    async getMetricsOfResource(resourceId) {
        return this.api.getMetric(resourceId, this.realm)
            .then(response => {
                return response.data
            })
    }
}

export default new MetricsRestApi()
