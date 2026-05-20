package org.eclipse.slm.service_management.service.client;

import org.eclipse.slm.common.parent.client.AbstractApiClient;
import org.eclipse.slm.common.restclient.feign.auth.AuthRequestInterceptor;
import org.eclipse.slm.service_management.features.service_offerings.api.categories.ServiceOfferingCategoriesRestApi;
import org.eclipse.slm.service_management.features.service_offerings.api.categories.ServiceOfferingCategoriesRestApiConfig;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.ServiceOfferingRestApi;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.ServiceOfferingRestApiConfig;
import org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOfferingVersionsRestApi;
import org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOfferingVersionsRestApiConfig;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.ServiceRepositoriesRestApi;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.ServiceRepositoriesRestApiConfig;
import org.eclipse.slm.service_management.features.service_offerings.api.vendors.ServiceVendorsRestApi;
import org.eclipse.slm.service_management.features.service_offerings.api.vendors.ServiceVendorsRestApiConfig;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;

public class ServiceManagementClient extends AbstractApiClient {

    private final ServiceOfferingCategoriesRestApi serviceOfferingCategoriesRestApi;
    private final ServiceOfferingRestApi serviceOfferingRestApi;
    private final ServiceOfferingVersionsRestApi serviceOfferingVersionsRestApi;
    private final ServiceVendorsRestApi serviceVendorsRestApi;
    private final ServiceRepositoriesRestApi serviceRepositoriesRestApi;

    public ServiceManagementClient(String serviceManagementBaseUrl,
                                   ObjectFactory<HttpMessageConverters> messageConverters,
                                   AuthRequestInterceptor authRequestInterceptor) {
        super(serviceManagementBaseUrl, messageConverters, authRequestInterceptor);

        var serviceCategoriesRestApiBaseUrl = this.baseUrl + ServiceOfferingCategoriesRestApiConfig.BASE_PATH;
        this.serviceOfferingCategoriesRestApi = this.buildFeignClient(ServiceOfferingCategoriesRestApi.class, serviceCategoriesRestApiBaseUrl);
        var serviceOfferingRestApiBaseUrl = this.baseUrl + ServiceOfferingRestApiConfig.BASE_PATH;
        this.serviceOfferingRestApi = this.buildFeignClient(ServiceOfferingRestApi.class, serviceOfferingRestApiBaseUrl);
        var serviceOfferingVersionsRestApiBaseUrl = this.baseUrl + ServiceOfferingVersionsRestApiConfig.BASE_PATH;
        this.serviceOfferingVersionsRestApi = this.buildFeignClient(ServiceOfferingVersionsRestApi.class, serviceOfferingVersionsRestApiBaseUrl);
        var serviceVendorsRestApiBaseUrl = this.baseUrl + ServiceVendorsRestApiConfig.BASE_PATH;
        this.serviceVendorsRestApi = this.buildFeignClient(ServiceVendorsRestApi.class, serviceVendorsRestApiBaseUrl);
        var serviceRepositoriesRestApiBaseUrl = this.baseUrl + ServiceRepositoriesRestApiConfig.BASE_PATH;
        this.serviceRepositoriesRestApi = this.buildFeignClient(ServiceRepositoriesRestApi.class, serviceRepositoriesRestApiBaseUrl);
    }

    public ServiceOfferingCategoriesRestApi serviceCategories() {
        return this.serviceOfferingCategoriesRestApi;
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

