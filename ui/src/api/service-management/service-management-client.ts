import {
    ServiceCategoriesRestControllerApi,
    ServiceInstancesGroupsRestControllerApi,
    ServiceInstancesRestControllerApi,
    ServiceManagementVariablesRestControllerApi,
    ServiceOfferingRestControllerApi,
    ServiceOfferingVersionsRestControllerApi,
    ServiceRepositoriesRestControllerApi,
    ServiceVendorsRestControllerApi,
    UsersRestControllerApi
} from "@/api/service-management/client";


class ServiceManagementClient {

    api = "/service-management";

    serviceInstancesGroupsApi = new ServiceInstancesGroupsRestControllerApi(undefined, this.api);
    serviceInstancesApi = new ServiceInstancesRestControllerApi(undefined, this.api);
    variablesApi = new ServiceManagementVariablesRestControllerApi(undefined, this.api);
    serviceOfferingsApi = new ServiceOfferingRestControllerApi(undefined, this.api);
    serviceOfferingVersionsApi = new ServiceOfferingVersionsRestControllerApi(undefined, this.api);
    serviceVendorsApi = new ServiceVendorsRestControllerApi(undefined, this.api);
    usersApi = new UsersRestControllerApi(undefined, this.api);
    serviceCategoriesApi = new ServiceCategoriesRestControllerApi(undefined, this.api);
    serviceRepositoriesApi = new ServiceRepositoriesRestControllerApi(undefined, this.api);

}


export default new ServiceManagementClient();