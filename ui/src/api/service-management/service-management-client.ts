import {
    ServiceCategoriesApi,
    ServiceInstanceGroupsApi,
    ServiceInstancesApi,
    ServiceManagementVariablesApi,
    ServiceOfferingsApi,
    ServiceOfferingVersionsApi,
    ServiceRepositoriesApi,
    ServiceVendorsApi,
    UsersApi
} from "@/api/service-management/client";


class ServiceManagementClient {

    api = "/service-management";

    serviceInstancesGroupsApi = new ServiceInstanceGroupsApi(undefined, this.api);
    serviceInstancesApi = new ServiceInstancesApi(undefined, this.api);
    variablesApi = new ServiceManagementVariablesApi(undefined, this.api);
    serviceOfferingsApi = new ServiceOfferingsApi(undefined, this.api);
    serviceOfferingVersionsApi = new ServiceOfferingVersionsApi(undefined, this.api);
    serviceVendorsApi = new ServiceVendorsApi(undefined, this.api);
    usersApi = new UsersApi(undefined, this.api);
    serviceCategoriesApi = new ServiceCategoriesApi(undefined, this.api);
    serviceRepositoriesApi = new ServiceRepositoriesApi(undefined, this.api);

}


export default new ServiceManagementClient();