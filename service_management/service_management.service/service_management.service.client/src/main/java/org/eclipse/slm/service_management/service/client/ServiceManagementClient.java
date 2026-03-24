package org.eclipse.slm.service_management.service.client;

import org.eclipse.slm.common.parent.client.AbstractApiClient;
import org.eclipse.slm.common.restclient.feign.auth.AuthRequestInterceptor;
import org.eclipse.slm.service_management.service.app.service_categories.ServiceCategoriesRestApi;
import org.eclipse.slm.service_management.service.app.service_categories.ServiceCategoriesRestApiConfig;
import org.eclipse.slm.service_management.service.app.service_offerings.ServiceOfferingRestApi;
import org.eclipse.slm.service_management.service.app.service_offerings.ServiceOfferingRestApiConfig;
import org.eclipse.slm.service_management.service.app.service_offerings.ServiceOfferingVersionsRestApi;
import org.eclipse.slm.service_management.service.app.service_offerings.ServiceOfferingVersionsRestApiConfig;
import org.eclipse.slm.service_management.service.app.service_repositories.ServiceRepositoriesRestApi;
import org.eclipse.slm.service_management.service.app.service_vendors.ServiceVendorsRestApi;
import org.eclipse.slm.service_management.service.app.service_vendors.ServiceVendorsRestApiConfig;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;

public class ServiceManagementClient extends AbstractApiClient {

    private final ServiceCategoriesRestApi serviceCategoriesRestApi;
    private final ServiceOfferingRestApi serviceOfferingRestApi;
    private final ServiceOfferingVersionsRestApi serviceOfferingVersionsRestApi;
    private final ServiceVendorsRestApi serviceVendorsRestApi;
    private final ServiceRepositoriesRestApi serviceRepositoriesRestApi;

    public ServiceManagementClient(String serviceManagementBaseUrl,
                                   ObjectFactory<HttpMessageConverters> messageConverters,
                                   AuthRequestInterceptor authRequestInterceptor) {
        super(serviceManagementBaseUrl, messageConverters, authRequestInterceptor);

        var serviceCategoriesRestApiBaseUrl = this.baseUrl + ServiceCategoriesRestApiConfig.BASE_PATH;
        this.serviceCategoriesRestApi = this.buildFeignClient(ServiceCategoriesRestApi.class, serviceCategoriesRestApiBaseUrl);
        var serviceOfferingRestApiBaseUrl = this.baseUrl + ServiceOfferingRestApiConfig.BASE_PATH;
        this.serviceOfferingRestApi = this.buildFeignClient(ServiceOfferingRestApi.class, serviceOfferingRestApiBaseUrl);
        var serviceOfferingVersionsRestApiBaseUrl = this.baseUrl + ServiceOfferingVersionsRestApiConfig.BASE_PATH;
        this.serviceOfferingVersionsRestApi = this.buildFeignClient(ServiceOfferingVersionsRestApi.class, serviceOfferingVersionsRestApiBaseUrl);
        var serviceVendorsRestApiBaseUrl = this.baseUrl + ServiceVendorsRestApiConfig.BASE_PATH;
        this.serviceVendorsRestApi = this.buildFeignClient(ServiceVendorsRestApi.class, serviceVendorsRestApiBaseUrl);
        var serviceRepositoriesRestApiBaseUrl = this.baseUrl + ServiceVendorsRestApiConfig.BASE_PATH;
        this.serviceRepositoriesRestApi = this.buildFeignClient(ServiceRepositoriesRestApi.class, serviceRepositoriesRestApiBaseUrl);
    }

    public ServiceCategoriesRestApi serviceCategories() {
        return this.serviceCategoriesRestApi;
    }

    public ServiceOfferingRestApi serviceOfferings() {
        return this.serviceOfferingRestApi;
    }

    public ServiceOfferingVersionsRestApi serviceOfferingVersions() {
        return this.serviceOfferingVersionsRestApi;
    }

    public ServiceRepositoriesRestApi serviceRepositories() {
        return this.serviceRepositoriesRestApi;
    }

    public ServiceVendorsRestApi serviceVendors() {
        return this.serviceVendorsRestApi;
    }
}
