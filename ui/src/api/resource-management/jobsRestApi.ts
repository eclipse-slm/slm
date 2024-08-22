import axios from 'axios'
import logRequestError from '@/api/restApiHelper'
import {ClustersRestControllerApi, JobsRestControllerApi} from "@/api/resource-management/client";

class JobsApi {
    API_URL = '/resource-management/jobs';

    api = new JobsRestControllerApi(undefined, "/resource-management");
    realm = 'fabos';

    async getJobs () {
        return this.api.getJobs(this.realm)
            .then(response => {
                let data = response.data;
                return <any>data;
            })
            .catch(logRequestError)
    }
}

export default new JobsApi()
