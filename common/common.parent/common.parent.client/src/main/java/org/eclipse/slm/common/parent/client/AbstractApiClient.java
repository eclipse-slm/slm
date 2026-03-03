package org.eclipse.slm.common.parent.client;

import feign.Feign;
import feign.Logger;
import feign.slf4j.Slf4jLogger;
import org.eclipse.slm.common.restclient.feign.ResponseErrorDecoder;
import org.eclipse.slm.common.restclient.feign.auth.AuthRequestInterceptor;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

import org.slf4j.LoggerFactory;

public abstract class AbstractApiClient {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractApiClient.class);

    protected final String baseUrl;
    protected final ObjectFactory<HttpMessageConverters> messageConverters;
    protected final AuthRequestInterceptor authRequestInterceptor;

    public AbstractApiClient(String baseUrl,
                             ObjectFactory<HttpMessageConverters> messageConverters,
                             AuthRequestInterceptor authRequestInterceptor) {
        this.baseUrl= baseUrl;
        this.messageConverters = messageConverters;
        this.authRequestInterceptor = authRequestInterceptor;
    }

    protected  <T> T buildFeignClient(Class<T> feignClientClass, String baseUrl) {
        Feign.Builder builder = Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(new SpringEncoder(messageConverters))
                .decoder(new ResponseEntityDecoder(new SpringDecoder(messageConverters)))
                .logger(new Slf4jLogger(feignClientClass))
                .logLevel(Logger.Level.FULL)
                .errorDecoder(new ResponseErrorDecoder())
                .requestInterceptor(authRequestInterceptor);
        T client = builder.target(feignClientClass, baseUrl);
        LOG.debug("Feign Client client for {} with base URL '{}' create", feignClientClass.getSimpleName(), baseUrl);
        return client;
    }

}
