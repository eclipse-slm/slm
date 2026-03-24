package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.apis.*;
import org.eclipse.slm.common.consul.client.auth.ConsulAuthentication;
import org.eclipse.slm.common.restclient.feign.FeignClientFactory;
import org.eclipse.slm.common.restclient.feign.auth.BearerTokenAuthRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Consul Client which provides basic Consul client functionality for the specific Consul clients.
 */
class AbstractConsulClient {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractConsulClient.class);

    protected String consulUrl;
    protected ConsulAuthentication consulAuthentication;

    protected String consulDatacenter = null;

    protected ConsulCatalogApiClient consulCatalogApiClient;
    protected ConsulAclAuthMethodsApiClient consulAclAuthMethodsApiClient;
    protected ConsulAclPoliciesApiClient consulAclPoliciesRulesApiClient;
    protected ConsulAclRolesApiClient consulAclRolesApiClient;
    protected ConsulAclBindingRulesApiClient consulAclBindingRulesApiClient;
    protected ConsulHealthApiClient consulHealthApiClient;
    protected ConsulKvStoreApiClient consulKvStoreApiClient;

    /**
     * Creates a new AbstractConsulClient instance.
     * @param consulUrl             The URL of the Consul server.
     * @param consulAuthentication  The Consul authentication details.
     */
    public AbstractConsulClient(String consulUrl, ConsulAuthentication consulAuthentication) {
        this.consulUrl = consulUrl;
        this.consulAuthentication = consulAuthentication;

        var authRequestInterceptor = new BearerTokenAuthRequestInterceptor(consulAuthentication.getConsulToken());
        this.consulCatalogApiClient = FeignClientFactory.createClient(ConsulCatalogApiClient.class, consulUrl, authRequestInterceptor);
        this.consulAclAuthMethodsApiClient = FeignClientFactory.createClient(ConsulAclAuthMethodsApiClient.class, consulUrl, authRequestInterceptor);
        this.consulAclPoliciesRulesApiClient = FeignClientFactory.createClient(ConsulAclPoliciesApiClient.class, consulUrl, authRequestInterceptor);
        this.consulAclRolesApiClient = FeignClientFactory.createClient(ConsulAclRolesApiClient.class, consulUrl, authRequestInterceptor);
        this.consulAclBindingRulesApiClient = FeignClientFactory.createClient(ConsulAclBindingRulesApiClient.class, consulUrl, authRequestInterceptor);
        this.consulHealthApiClient = FeignClientFactory.createClient(ConsulHealthApiClient.class, consulUrl, authRequestInterceptor);
        this.consulKvStoreApiClient = FeignClientFactory.createClient(ConsulKvStoreApiClient.class, consulUrl, authRequestInterceptor);
    }
}
