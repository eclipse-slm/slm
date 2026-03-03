import {
     CredentialsApi,
} from "@/api/platform-management/client";


class PlatformManagementClient{

    apiUrl = "/platform-management";

    credentialsApi = new CredentialsApi(undefined, this.apiUrl);
}

export default new PlatformManagementClient()